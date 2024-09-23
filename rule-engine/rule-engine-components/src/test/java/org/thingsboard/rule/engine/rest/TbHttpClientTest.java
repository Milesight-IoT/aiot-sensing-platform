/*
 * Copyright © 2016-2023 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.thingsboard.rule.engine.rest;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockserver.integration.ClientAndServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.AsyncRestTemplate;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class TbHttpClientTest {

    EventLoopGroup eventLoop;
    TbHttpClient client;

    @Before
    public void setUp() throws Exception {
        client = mock(TbHttpClient.class);
        willCallRealMethod().given(client).getSharedOrCreateEventLoopGroup(any());
    }

    @After
    public void tearDown() throws Exception {
        if (eventLoop != null) {
            eventLoop.shutdownGracefully();
        }
    }

    @Test
    public void givenSharedEventLoop_whenGetEventLoop_ThenReturnShared() {
        eventLoop = mock(EventLoopGroup.class);
        assertThat(client.getSharedOrCreateEventLoopGroup(eventLoop), is(eventLoop));
    }

    @Test
    public void givenNull_whenGetEventLoop_ThenReturnShared() {
        eventLoop = client.getSharedOrCreateEventLoopGroup(null);
        assertThat(eventLoop, instanceOf(NioEventLoopGroup.class));
    }

    @Test
    public void testBuildSimpleUri() {
        Mockito.when(client.buildEncodedUri(any())).thenCallRealMethod();
        String url = "http://localhost:8080/";
        URI uri = client.buildEncodedUri(url);
        Assert.assertEquals(url, uri.toString());
    }

    @Test
    public void testBuildUriWithoutProtocol() {
        Mockito.when(client.buildEncodedUri(any())).thenCallRealMethod();
        String url = "localhost:8080/";
        assertThatThrownBy(() -> client.buildEncodedUri(url));
    }

    @Test
    public void testBuildInvalidUri() {
        Mockito.when(client.buildEncodedUri(any())).thenCallRealMethod();
        String url = "aaa";
        assertThatThrownBy(() -> client.buildEncodedUri(url));
    }

    @Test
    public void testBuildUriWithSpecialSymbols() {
        Mockito.when(client.buildEncodedUri(any())).thenCallRealMethod();
        String url = "http://192.168.1.1/data?d={\"a\": 12}";
        String expected = "http://192.168.1.1/data?d=%7B%22a%22:%2012%7D";
        URI uri = client.buildEncodedUri(url);
        Assert.assertEquals(expected, uri.toString());
    }

    @Test
    public void testProcessMessageWithJsonInUrlVariable() throws Exception {
        String host = "localhost";
        String path = "/api";
        String paramKey = "data";
        String paramVal = "[{\"test\":\"test\"}]";
        String successResponseBody = "SUCCESS";

        var server = setUpDummyServer(host, path, paramKey, paramVal, successResponseBody);

        String endpointUrl = String.format(
                "http://%s:%d%s?%s=%s",
                host, server.getPort(), path, paramKey, paramVal
        );
        String method = "GET";


        var config = new TbRestApiCallNodeConfiguration()
                .defaultConfiguration();
        config.setRequestMethod(method);
        config.setRestEndpointUrlPattern(endpointUrl);
        config.setUseSimpleClientHttpFactory(true);

        var asyncRestTemplate = new AsyncRestTemplate();

        var httpClient = new TbHttpClient(config, eventLoop);
        httpClient.setHttpClient(asyncRestTemplate);

        var msg = TbMsg.newMsg("GET", new DeviceId(EntityId.NULL_UUID), TbMsgMetaData.EMPTY, "{}");
        var successMsg = TbMsg.newMsg(
                "SUCCESS", msg.getOriginator(),
                msg.getMetaData(), msg.getData()
        );

        var ctx = mock(TbContext.class);
        when(ctx.transformMsg(
                eq(msg), eq(msg.getType()),
                eq(msg.getOriginator()),
                eq(msg.getMetaData()),
                eq(msg.getData())
        )).thenReturn(successMsg);

        var capturedData = ArgumentCaptor.forClass(String.class);

        when(ctx.transformMsg(
                eq(msg), eq(msg.getType()),
                eq(msg.getOriginator()),
                any(),
                capturedData.capture()
        )).thenReturn(successMsg);

        httpClient.processMessage(ctx, msg);

        Awaitility.await()
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> {
                    try {
                        verify(ctx, times(1)).tellSuccess(any());
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });

        verify(ctx, times(1)).tellSuccess(any());
        verify(ctx, times(0)).tellFailure(any(), any());
        Assert.assertEquals(successResponseBody, capturedData.getValue());
    }

    private ClientAndServer setUpDummyServer(String host, String path, String paramKey, String paramVal, String successResponseBody) {
        var server = startClientAndServer(host, 1080);
        createGetMethodExpectations(server, path, paramKey, paramVal, successResponseBody);
        return server;
    }

    private void createGetMethodExpectations(ClientAndServer server, String path, String paramKey, String paramVal, String successResponseBody) {
        server.when(
                request()
                        .withMethod("GET")
                        .withPath(path)
                        .withQueryStringParameter(paramKey, paramVal)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withBody(successResponseBody)
        );
    }

    @Test
    public void testHeadersToMetaData() {
        Map<String, List<String>> headers = new LinkedMultiValueMap<>();
        headers.put("Content-Type", List.of("binary"));
        headers.put("Set-Cookie", List.of("sap-context=sap-client=075; path=/", "sap-token=sap-client=075; path=/"));

        TbMsgMetaData metaData = new TbMsgMetaData();

        willCallRealMethod().given(client).headersToMetaData(any(), any());

        client.headersToMetaData(headers, metaData::putValue);

        Map<String, String> data = metaData.getData();

        Assertions.assertThat(data).hasSize(2);
        Assertions.assertThat(data.get("Content-Type")).isEqualTo("binary");
        Assertions.assertThat(data.get("Set-Cookie")).isEqualTo("[\"sap-context=sap-client=075; path=/\",\"sap-token=sap-client=075; path=/\"]");
    }

}