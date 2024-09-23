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
import org.eclipse.leshan.core.ResponseCode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.junit.Test;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.transport.lwm2m.rpc.AbstractRpcLwM2MIntegrationTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.OBJECT_INSTANCE_ID_0;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.OBJECT_INSTANCE_ID_1;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.OBJECT_INSTANCE_ID_2;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.RESOURCE_ID_0;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.RESOURCE_ID_14;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.RESOURCE_ID_15;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.RESOURCE_ID_9;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.RESOURCE_ID_NAME_3_14;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.RESOURCE_INSTANCE_ID_2;

public class RpcLwm2mIntegrationWriteTest extends AbstractRpcLwM2MIntegrationTest {


    /**
     * update SingleResource:
     * WriteReplace {"id":"3/0/14","value":"+12"}
     * {"result":"CHANGED"}
     */
    @Test
    public void testWriteReplaceValueSingleResourceById_Result_CHANGED() throws Exception {
        String expectedPath = objectInstanceIdVer_3 + "/" + RESOURCE_ID_14;
        String expectedValue = "+12";
        String actualResult = sendRPCWriteStringById("WriteReplace", expectedPath, expectedValue);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.CHANGED.getName(), rpcActualResult.get("result").asText());
        actualResult = sendRPCReadById(expectedPath);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        String actualValues = rpcActualResult.get("value").asText();
        String expected = "LwM2mSingleResource [id=" + RESOURCE_ID_14 + ", value=" + expectedValue + ", type=STRING]";
        assertTrue(actualValues.contains(expected));
    }

    /**
     * key
     * WriteReplace {"key":"timezone","value":"+10"}
     * {"result":"CHANGED"}
     */
    @Test
    public void testWriteReplaceValueSingleResourceByKey_Result_CHANGED() throws Exception {
        String expectedKey = RESOURCE_ID_NAME_3_14;
        String expectedValue = "+09";
        String actualResult = sendRPCWriteByKey("WriteReplace", expectedKey, expectedValue);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.CHANGED.getName(), rpcActualResult.get("result").asText());
        actualResult = sendRPCReadByKey(expectedKey);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        String actualValues = rpcActualResult.get("value").asText();
        String expected = "LwM2mSingleResource [id=" + RESOURCE_ID_14 + ", value=" + expectedValue + ", type=STRING]";
        assertTrue(actualValues.contains(expected));
    }


    /**
     * id
     * WriteReplace {"id": "/19_1.1/0/0","value": {"0":"0000ad45675600", "15":"1525ad45675600cdef"}}
     * {"result":"CHANGED"}
     */
    @Test
    public void testWriteReplaceValueMultipleResource_Result_CHANGED_Value_Multi_Instance_Resource_must_in_Json_format() throws Exception {
        String expectedPath = objectIdVer_19 + "/" + OBJECT_INSTANCE_ID_0 + "/" + RESOURCE_ID_0;
        int resourceInstanceId0 = 0;
        int resourceInstanceId15 = 15;
        String expectedValue0 = "0000ad45675600";
        String expectedValue15 = "1525ad45675600cdef";
        String expectedValue = "{\"" + resourceInstanceId0 + "\":\"" + expectedValue0 + "\", \"" + resourceInstanceId15 + "\":\"" + expectedValue15 + "\"}";
        String actualResult = sendRPCWriteObjectById("WriteReplace", expectedPath, expectedValue);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.CHANGED.getName(), rpcActualResult.get("result").asText());
        String expectedPath0 = expectedPath + "/" + resourceInstanceId0;
        String expectedPath15 = expectedPath + "/" + resourceInstanceId15;
        actualResult = sendRPCReadById(expectedPath0);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        String actualValues = rpcActualResult.get("value").asText();
        String expected = "LwM2mResourceInstance [id=" + resourceInstanceId0 + ", value=" + expectedValue0.length()/2 + "Bytes, type=OPAQUE]";
        assertTrue(actualValues.contains(expected));
        actualResult = sendRPCReadById(expectedPath15);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        actualValues = rpcActualResult.get("value").asText();
        expected = "LwM2mResourceInstance [id=" + resourceInstanceId15 + ", value=" + expectedValue15.length()/2 + "Bytes, type=OPAQUE]";
        assertTrue(actualValues.contains(expected));
    }

    /**
     * bad: singleResource, operation="R" - only read
     * WriteReplace {"id":"/3/0/9","value":90}
     * {"result":"METHOD_NOT_ALLOWED"}
     */
    @Test
    public void testWriteReplaceValueSingleResourceR_ById_Result_CHANGED() throws Exception {
        String expectedPath = objectInstanceIdVer_3 + "/" + RESOURCE_ID_9;
        Integer expectedValue = 90;
        String actualResult = sendRPCWriteObjectById("WriteReplace", expectedPath, expectedValue);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.METHOD_NOT_ALLOWED.getName(), rpcActualResult.get("result").asText());
    }

    /**
     * ids
     * WriteUpdate  {"id":"/3/0","value":{"14":"+5","15":"Kiyv/Europe"}}
     * {"result":"CHANGED"}
     */
    @Test
    public void testWriteUpdateValueSingleResourceById_Result_CHANGED() throws Exception {
        String expectedPath = objectInstanceIdVer_3;
        String expectedValue14 = "+5";
        String expectedValue15 = "Kiyv/Europe";
        String expectedValue = "{\"" + RESOURCE_ID_14 + "\":\"" + expectedValue14 + "\",\"" + RESOURCE_ID_15 + "\":\"" + expectedValue15 + "\"}";
        String actualResult = sendRPCWriteObjectById("WriteUpdate", expectedPath, expectedValue);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.CHANGED.getName(), rpcActualResult.get("result").asText());
        String expectedPath14 = objectInstanceIdVer_3 + "/" + RESOURCE_ID_14;
        String expectedPath15 = objectInstanceIdVer_3 + "/" + RESOURCE_ID_15;
        actualResult = sendRPCReadById(expectedPath14);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        String actualValues = rpcActualResult.get("value").asText();
        String expected = "LwM2mSingleResource [id=" + RESOURCE_ID_14 + ", value=" + expectedValue14 + ", type=STRING]";
        assertTrue(actualValues.contains(expected));
        actualResult = sendRPCReadById(expectedPath15);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        actualValues = rpcActualResult.get("value").asText();
        expected = "LwM2mSingleResource [id=" + RESOURCE_ID_15 + ", value=" + expectedValue15 + ", type=STRING]";
        assertTrue(actualValues.contains(expected));
    }

    /**
     * id
     * WriteUpdate {"id": "/19_1.1/0","value": {"0":{"0":"00ad456756", "25":"25ad456756"}}}
     * {"result":"CHANGED"}
     */
    @Test
    public void testWriteUpdateValueMultipleResourceById_Result_CHANGED() throws Exception {
        String expectedPath = objectIdVer_19 + "/" + OBJECT_INSTANCE_ID_0;
        int resourceInstanceId0 = 0;
        int resourceInstanceId25 = 25;
        String expectedValue0 = "00ad45675600";
        String expectedValue25 = "25ad45675600cdef";
        String expectedValue = "{\"" + RESOURCE_ID_0 + "\":{\"" + resourceInstanceId0 + "\":\"" + expectedValue0 + "\", \"" + resourceInstanceId25 + "\":\"" + expectedValue25 + "\"}}";
        String actualResult = sendRPCWriteObjectById("WriteUpdate", expectedPath, expectedValue);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.CHANGED.getName(), rpcActualResult.get("result").asText());
        String expectedPath0 = expectedPath + "/" + RESOURCE_ID_0 + "/" + resourceInstanceId0;
        String expectedPath25 =expectedPath + "/" + RESOURCE_ID_0 + "/" + resourceInstanceId25;
        actualResult = sendRPCReadById(expectedPath0);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        String actualValues = rpcActualResult.get("value").asText();
        String expected = "LwM2mResourceInstance [id=" + resourceInstanceId0 + ", value=" + expectedValue0.length()/2 + "Bytes, type=OPAQUE]";
        assertTrue(actualValues.contains(expected));
        actualResult = sendRPCReadById(expectedPath25);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        actualValues = rpcActualResult.get("value").asText();
        expected = "LwM2mResourceInstance [id=" + resourceInstanceId25 + ", value=" + expectedValue25.length()/2 + "Bytes, type=OPAQUE]";
        assertTrue(actualValues.contains(expected));
    }

    /**
     * ResourceInstance + KeySingleResource + IdSingleResource
     * WriteComposite {"nodes":{"/19/1/0/2":"00001234", "UtfOffset":"+04", "/3/0/15":"Kiyv/Europe"}}
     * {"result":"CHANGED"}
     */
    @Test
    public void testWriteCompositeValueSingleResourceResourceInstanceByIdKey_Result_CHANGED() throws Exception {
        int resourceInstanceId2 = 2;
        String expectedPath19_1_0_2 = objectIdVer_19 + "/" + OBJECT_INSTANCE_ID_1 + "/" + RESOURCE_ID_0 + "/" + resourceInstanceId2;
        String expectedValue19_1_0_2 = "00001234";
        String expectedKey3_0_14 = RESOURCE_ID_NAME_3_14;
        String expectedValue3_0_14 = "+04";
        String expectedPath3_0_15 = objectInstanceIdVer_3 + "/" + RESOURCE_ID_15;
        String expectedValue3_0_15 = "Kiyv/Europe";
        String nodes = "{\"" + expectedPath19_1_0_2 + "\":\"" + expectedValue19_1_0_2 + "\", \"" + expectedKey3_0_14 +
                "\":\"" + expectedValue3_0_14 + "\", \"" + expectedPath3_0_15 + "\":\"" + expectedValue3_0_15 + "\"}";
        String actualResult = sendCompositeRPC(nodes);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.CHANGED.getName(), rpcActualResult.get("result").asText());
        actualResult = sendRPCReadById(expectedPath19_1_0_2);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        String actualValues = rpcActualResult.get("value").asText();
        String expected = "LwM2mResourceInstance [id=" + resourceInstanceId2 + ", value=" + expectedValue19_1_0_2.length()/2 + "Bytes, type=OPAQUE]";
        assertTrue(actualValues.contains(expected));
        actualResult = sendRPCReadByKey(expectedKey3_0_14);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        actualValues = rpcActualResult.get("value").asText();
        expected = "LwM2mSingleResource [id=" + RESOURCE_ID_14 + ", value=" + expectedValue3_0_14 + ", type=STRING]";
        assertTrue(actualValues.contains(expected));
        actualResult = sendRPCReadById(expectedPath3_0_15);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        actualValues = rpcActualResult.get("value").asText();
        expected = "LwM2mSingleResource [id=" + RESOURCE_ID_15 + ", value=" + expectedValue3_0_15 + ", type=STRING]";
        assertTrue(actualValues.contains(expected));
    }

    /**
     * multipleResource == error
     * bad - cannot be used for value Json, only primitive: SingleResource, ResourceInstance (for Json: WriteUpdate, WriteReplace)
     * WriteComposite {"nodes":{"/19/0/0":{"0":"abcd5678", "10":"abcd5678"}}}
     */
    @Test
    public void testWriteCompositeValueSingleMultipleResourceByIdKey_Result_BAD_REQUEST_WriteComposite_operation_for_SingleResources_or_and_ResourceInstance() throws Exception {
        String nodes = "{\"/19/0/0\":{\"0\":\"abcd5678\", \"10\":\"abcd5678\"}}";
        String actualResult = sendCompositeRPC(nodes);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.BAD_REQUEST.getName(), rpcActualResult.get("result").asText());
        String actualValues = rpcActualResult.get("error").asText();
        String expectedNodes = nodes.replaceAll("\"", "").replaceAll(":", "=");
        String expected = String.format("nodes: %s is not validate value. " +
                "The WriteComposite operation is only used for SingleResources or/and ResourceInstance.", expectedNodes);
        assertEquals(expected, actualValues);
    }


    /**
     * update_resourceInstances&update_singleResource
     * new ResourceInstance if Resource is Multiple & Resource Single
     *  - WriteReplace  {"id":"/19_1.2/1/0","value":{"2":ddff12"}}
     *  - WriteReplace {"key":"UtfOffset","value":"+04"}
     *  - WriteReplace {"id":"/3/0/15","value":"Kiyv/Europe"}
     * WriteComposite {"nodes":{"/19_1.1/1/0/2":"00001234", "UtfOffset":"+04", "/3/0/15":"Kiyv/Europe"}}}
     * {"result":"CHANGED"}
     */
    @Test
    public void testWriteCompositeCreateResourceInstanceUpdateSingleResourceByIdKey_Result_CHANGED() throws Exception {
        String expectedPath19_1_0_2 = objectIdVer_19 + "/" + OBJECT_INSTANCE_ID_1 + "/" + RESOURCE_ID_0 + "/" + RESOURCE_INSTANCE_ID_2;
        String expectedValue19_1_0_2 = "00001234";
        String expectedKey3_0_14 = RESOURCE_ID_NAME_3_14;
        String expectedValue3_0_14 = "+04";
        String expectedPath3_0_15 = objectInstanceIdVer_3 + "/" + RESOURCE_ID_15;
        String expectedValue3_0_15 = "Kiyv/Europe";
        String nodes = "{\"" + expectedPath19_1_0_2 + "\":\"" + expectedValue19_1_0_2 + "\", \"" + expectedKey3_0_14 +
                "\":\"" + expectedValue3_0_14 + "\", \"" + expectedPath3_0_15 + "\":\"" + expectedValue3_0_15 + "\"}";
        String actualResult = sendCompositeRPC(nodes);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.CHANGED.getName(), rpcActualResult.get("result").asText());
        actualResult = sendRPCReadById(expectedPath19_1_0_2);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        String actualValues = rpcActualResult.get("value").asText();
        String expected = "LwM2mResourceInstance [id=" + RESOURCE_INSTANCE_ID_2 + ", value=" + expectedValue19_1_0_2.length()/2 + "Bytes, type=OPAQUE]";
        assertTrue(actualValues.contains(expected));
        actualResult = sendRPCReadByKey(expectedKey3_0_14);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        actualValues = rpcActualResult.get("value").asText();
        expected = "LwM2mSingleResource [id=" + RESOURCE_ID_14 + ", value=" + expectedValue3_0_14 + ", type=STRING]";
        assertTrue(actualValues.contains(expected));
        actualResult = sendRPCReadById(expectedPath3_0_15);
        rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        actualValues = rpcActualResult.get("value").asText();
        expected = "LwM2mSingleResource [id=" + RESOURCE_ID_15 + ", value=" + expectedValue3_0_15 + ", type=STRING]";
        assertTrue(actualValues.contains(expected));
    }

    /**
     * composite_not created_new_instance...
     * new ObjectInstance if Object is Multiple - bad
     *  - WriteReplace  {"id":"/19_1.2/2/0","value":{"2":ddff12"}}
     *  - WriteReplace {"key":"UtfOffset","value":"+04"}
     *  - WriteReplace {"id":"/3/0/15","value":"Kiyv/Europe"}
     * WriteComposite {"nodes":{"/19_1.1/1/0/2":"00001234", "UtfOffset":"+04", "/3/0/15":"Kiyv/Europe"}}}
     * {"result":"BAD_REQUEST","error":"object instance /19/2 not found"}
     */
    @Test
    public void testWriteCompositeCreateObjectInstanceUpdateSingleResourceByIdKey_Result_BAD_REQUEST() throws Exception {
        String expectedPath19_1_2_2 = objectIdVer_19 + "/" + OBJECT_INSTANCE_ID_2 + "/" + RESOURCE_ID_0 + "/" + RESOURCE_INSTANCE_ID_2;
        String expectedValue19_1_0_2 = "00001234";
        String expectedKey3_0_14 = RESOURCE_ID_NAME_3_14;
        String expectedValue3_0_14 = "+04";
        String expectedPath3_0_15 = objectInstanceIdVer_3 + "/" + RESOURCE_ID_15;
        String expectedValue3_0_15 = "Kiyv/Europe";
        String nodes = "{\"" + expectedPath19_1_2_2 + "\":\"" + expectedValue19_1_0_2 + "\", \"" + expectedKey3_0_14 +
                "\":\"" + expectedValue3_0_14 + "\", \"" + expectedPath3_0_15 + "\":\"" + expectedValue3_0_15 + "\"}";
        String actualResult = sendCompositeRPC(nodes);
        ObjectNode rpcActualResult = JacksonUtil.fromString(actualResult, ObjectNode.class);
        assertEquals(ResponseCode.BAD_REQUEST.getName(), rpcActualResult.get("result").asText());
        String expectedObjectId = pathIdVerToObjectId((String) expectedPath19_1_2_2);
        LwM2mPath expectedPathId = new LwM2mPath(expectedObjectId);
        String expected = "object instance " + "/" + expectedPathId.getObjectId() + "/" + expectedPathId.getObjectInstanceId() + " not found";
        String actual = rpcActualResult.get("error").asText();
        assertTrue(actual.equals(expected));
    }

    private String sendRPCWriteStringById(String method, String path, String value) throws Exception {
        String setRpcRequest = "{\"method\": \"" + method + "\", \"params\": {\"id\": \"" + path + "\", \"value\": \"" + value + "\" }}";
        return doPostAsync("/api/plugins/rpc/twoway/" + deviceId, setRpcRequest, String.class, status().isOk());
    }

    private String sendRPCWriteObjectById(String method, String path, Object value) throws Exception {
        String setRpcRequest = "{\"method\": \"" + method + "\", \"params\": {\"id\": \"" + path + "\", \"value\": " + value + " }}";
        return doPostAsync("/api/plugins/rpc/twoway/" + deviceId, setRpcRequest, String.class, status().isOk());
    }

    private String sendRPCReadById(String id) throws Exception {
        String setRpcRequest = "{\"method\": \"Read\", \"params\": {\"id\": \"" + id + "\"}}";
        return doPostAsync("/api/plugins/rpc/twoway/" + deviceId, setRpcRequest, String.class, status().isOk());
    }

    private String sendRPCWriteByKey(String method, String key, String value) throws Exception {
        String setRpcRequest = "{\"method\": \"" + method + "\", \"params\": {\"key\": \"" + key + "\", \"value\": \"" + value + "\" }}";
        return doPostAsync("/api/plugins/rpc/twoway/" + deviceId, setRpcRequest, String.class, status().isOk());
    }

    private String sendRPCReadByKey(String key) throws Exception {
        String setRpcRequest = "{\"method\": \"Read\", \"params\": {\"key\": \"" + key + "\"}}";
        return doPostAsync("/api/plugins/rpc/twoway/" + deviceId, setRpcRequest, String.class, status().isOk());
    }

    private String sendCompositeRPC(String nodes) throws Exception {
        String setRpcRequest = "{\"method\": \"WriteComposite\", \"params\": {\"nodes\":" + nodes + "}}";
        return doPostAsync("/api/plugins/rpc/twoway/" + deviceId, setRpcRequest, String.class, status().isOk());
    }
}