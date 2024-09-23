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
package org.thingsboard.server.transport.lwm2m.rpc.sql;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.leshan.core.ResponseCode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.junit.Test;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.transport.lwm2m.rpc.AbstractRpcLwM2MIntegrationTest;

import static org.eclipse.leshan.core.LwM2mId.ACCESS_CONTROL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.OBJECT_INSTANCE_ID_0;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.RESOURCE_ID_0;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.RESOURCE_ID_14;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.RESOURCE_ID_3;
import static org.thingsboard.server.transport.lwm2m.utils.LwM2MTransportUtil.fromVersionedIdToObjectId;

@Slf4j
public class RpcLwm2mIntegrationObserveTest extends AbstractRpcLwM2MIntegrationTest {

    /**
     * ObserveReadAll&ObserveReadAll
     * @throws Exception
     */
    @Test
    public void testObserveReadAllNothingObservation_Result_CONTENT_Value_Count_0() throws Exception {
        String idVer_3_0_0 = objectInstanceIdVer_3 + "/" + RESOURCE_ID_0;
        sendRpcObserve("Observe", fromVersionedIdToObjectId(idVer_3_0_0));
        String actualResultBefore = sendRpcObserve("ObserveReadAll", null);
        ObjectNode rpcActualResultBefore = JacksonUtil.fromString(actualResultBefore, ObjectNode.class);
        assertEquals(ResponseCode.CONTENT.getName(), rpcActualResultBefore.get("result").asText());
        int cntObserveBefore = rpcActualResultBefore.get("value").asText().split(",").length;
        assertTrue(cntObserveBefore > 0);
        String actualResult = sendRpcObserve("ObserveCancelAll", null);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.CONTENT.getName(), rpcActualResult.get("result").asText());
        int cntObserveCancelAll = Integer.parseInt(rpcActualResult.get("value").asText());
        assertTrue(cntObserveCancelAll > 0);
        String actualResultAfter = sendRpcObserve("ObserveReadAll", null);
        ObjectNode rpcActualResultAfter = JacksonUtil.fromString(actualResultAfter, ObjectNode.class);
        assertEquals(ResponseCode.CONTENT.getName(), rpcActualResultAfter.get("result").asText());
        String expectResultAfter = "[]";
        assertEquals( expectResultAfter, rpcActualResultAfter.get("value").asText());
    }

    /**
     * Observe {"id":"/3/0/0"}
     * @throws Exception
     */
    @Test
    public void testObserveSingleResourceWithout_IdVer_1_0_Result_CONTENT_Value_SingleResource() throws Exception {
        String expectedId = objectInstanceIdVer_3 + "/" + RESOURCE_ID_0;
        String actualResult = sendRpcObserve("Observe", fromVersionedIdToObjectId(expectedId));
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.CONTENT.getName(), rpcActualResult.get("result").asText());
        assertTrue(rpcActualResult.get("value").asText().contains("LwM2mSingleResource"));
    }
    /**
     * Observe {"id":"/3_1.0/0/14"}
     * @throws Exception
     */
    @Test
    public void testObserveSingleResourceWith_IdVer_1_0_Result_CONTENT_Value_SingleResource() throws Exception {
        String expectedId = objectInstanceIdVer_3 + "/" + RESOURCE_ID_14;
        String actualResult = sendRpcObserve("Observe", expectedId);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.CONTENT.getName(), rpcActualResult.get("result").asText());
        assertTrue(rpcActualResult.get("value").asText().contains("LwM2mSingleResource"));
    }

    /**
     * Observe {"id":"/3_1.1/0/13"}
     * @throws Exception
     */
    @Test
    public void testObserveWithBadVersion_Result_BadRequest_ErrorMsg_BadVersionMustBe1_0() throws Exception {
        String expectedInstance = (String) expectedInstances.stream().filter(path -> !((String)path).contains("_")).findFirst().get();
        LwM2mPath expectedPath = new LwM2mPath(expectedInstance);
        int expectedResource = lwM2MTestClient.getLeshanClient().getObjectTree().getObjectEnablers().get(expectedPath.getObjectId()).getObjectModel().resources.entrySet().stream().findAny().get().getKey();
        String expectedId = "/" + expectedPath.getObjectId() + "_1.2" + "/" + expectedPath.getObjectInstanceId() + "/" + expectedResource;
        String actualResult = sendRpcObserve("Observe", expectedId);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.BAD_REQUEST.getName(), rpcActualResult.get("result").asText());
        String expected = "Specified resource id " + expectedId +" is not valid version! Must be version: 1.0";
        assertEquals(expected, rpcActualResult.get("error").asText());
    }

    /**
     * Not implemented Instance
     * Observe {"id":"/2/0"}
     * @throws Exception
     */
    @Test
    public void testObserveNoImplementedInstanceOnDevice_Result_NotFound() throws Exception {
        String objectInstanceIdVer = (String) expectedObjectIdVers.stream().filter(path -> ((String)path).contains("/" + ACCESS_CONTROL)).findFirst().get();
        String expected = objectInstanceIdVer + "/" + OBJECT_INSTANCE_ID_0;
        String actualResult = sendRpcObserve("Observe", expected);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.NOT_FOUND.getName(), rpcActualResult.get("result").asText());
    }

    /**
     * Not implemented Resource
     * Observe {"id":"/19_1.1/0/0"}
     * @throws Exception
     */
    @Test
    public void testObserveNoImplementedResourceOnDeviceValueNull_Result_BadRequest() throws Exception {
        String expected = objectIdVer_19 + "/" + OBJECT_INSTANCE_ID_0 + "/" + RESOURCE_ID_3;
        String actualResult = sendRpcObserve("Observe", expected);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        String expectedValue = "value MUST NOT be null";
        assertEquals(ResponseCode.BAD_REQUEST.getName(), rpcActualResult.get("result").asText());
        assertEquals(expectedValue, rpcActualResult.get("error").asText());
    }

    /**
     * Repeated request on Observe
     * Observe {"id":"/5/0/0"}
     * @throws Exception
     */
    @Test
    public void testObserveRSourceNotRead_Result_METHOD_NOT_ALLOWED() throws Exception {
        String expectedId = objectInstanceIdVer_5 + "/" + RESOURCE_ID_0;
        sendRpcObserve("Observe", expectedId);
        String actualResult = sendRpcObserve("Observe", expectedId);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.METHOD_NOT_ALLOWED.getName(), rpcActualResult.get("result").asText());
    }

    /**
     * Repeated request on Observe
     * Observe {"id":"/3/0/9"}
     * @throws Exception
     */
    @Test
    public void testObserveRepeatedRequestObserveOnDevice_Result_BAD_REQUEST_ErrorMsg_AlreadyRegistered() throws Exception {
        String idVer_3_0_0 = objectInstanceIdVer_3 + "/" + RESOURCE_ID_0;
        sendRpcObserve("Observe", fromVersionedIdToObjectId(idVer_3_0_0));
        sendRpcObserve("ObserveReadAll", null);
        String actualResult = sendRpcObserve("Observe", idVer_3_0_0);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.BAD_REQUEST.getName(), rpcActualResult.get("result").asText());
        String expected = "Observation is already registered!";
        assertEquals(expected, rpcActualResult.get("error").asText());
    }

    /**
     * ObserveReadAll
     * @throws Exception
     */
    @Test
    public void testObserveReadAll_Result_CONTENT_Value_Contains_Paths_Count_ObserveReadAll() throws Exception {
        String actualResultReadAll = sendRpcObserve("ObserveReadAll", null);
        ObjectNode rpcActualResultReadAll = JacksonUtil.fromString(actualResultReadAll, ObjectNode.class);
        assertEquals(ResponseCode.CONTENT.getName(), rpcActualResultReadAll.get("result").asText());
        String actualValuesReadAll = rpcActualResultReadAll.get("value").asText();
        log.warn("ObserveReadAll:  [{}]", actualValuesReadAll);
        assertEquals(2, actualValuesReadAll.split(",").length);
    }


    /**
     * ObserveCancel {"id":"/3/0/3"}
     * ObserveCancel {"id":"/5/0/3"}
     */
    @Test
    public void testObserveCancelOneResource_Result_CONTENT_Value_Count_1() throws Exception {
        sendRpcObserve("ObserveCancelAll", null);
        String expectedId_3_0_3 = objectInstanceIdVer_3 + "/" + RESOURCE_ID_3;
        String expectedId_5_0_3 = objectInstanceIdVer_5 + "/" + RESOURCE_ID_3;
        sendRpcObserve("Observe", expectedId_3_0_3);
        sendRpcObserve("Observe", expectedId_5_0_3);
        String actualResult = sendRpcObserve("ObserveCancel", expectedId_3_0_3);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.CONTENT.getName(), rpcActualResult.get("result").asText());
        assertEquals("1", rpcActualResult.get("value").asText());
    }

    private String sendRpcObserve(String method, String params) throws Exception {
        return sendObserve(method, params, deviceId);
    }
}
