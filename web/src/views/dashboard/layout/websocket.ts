import { ref } from 'vue';
import { getToken, formatToken } from '@/utils/auth';
import MsWebSocket from '@/utils/websocket/index';

export function useWebsocket() {
    const wsObject = ref(null);
    function initWebsocket() {
        if (wsObject.value) return;
        const tokenInfo = getToken();
        let token = '';
        if (tokenInfo) {
            token = formatToken(tokenInfo.accessToken);
        }
        token = token.slice(7);
        const wsOption = {
            url: `/api/ws/plugins/telemetry?token=${token}`,
            handlerOpen: data => {
                sendData(wsObjectTmp);
            },
            handlerMessage: data => {
                if (!data) return;
                if (data.cmdId) {
                    // 属于DeviceStatus类别的
                }
                if (data.hasNext !== undefined) {
                    // 属于Alarm table类别的
                }
                // if (data)
            },
        };
        const wsObjectTmp = new MsWebSocket(wsOption);
        return wsObjectTmp;
    }
    function sendData(wsObjectTmp) {
        wsObject.value = wsObjectTmp;
        console.log(wsObject.value);
    }

    function sendAlarmTable(param) {
        return {
            pageSize: 200,
            page: 0,
            sortProperty: 'createdTime',
            key: 'dashboard_alarm_table',
            tenantId: '',
            ...param,
        };
        // wsObject.send(JSON.stringify(param));
    }
    const deviceStatus = { 'Active devices': 2, 'Inactive devices': 3, 'Total devices': 1 };
    function sendDeviceStatus() {
        let param = {};
        param = {
            entityCountCmds: [
                {
                    query: { entityFilter: { type: 'entityType', resolveMultiple: true, entityType: 'DEVICE' } },
                    cmdId: 1,
                },
                {
                    query: {
                        entityFilter: { type: 'entityType', resolveMultiple: true, entityType: 'DEVICE' },
                        keyFilters: [
                            {
                                key: { type: 'ATTRIBUTE', key: 'active' },
                                valueType: 'BOOLEAN',
                                predicate: {
                                    operation: 'EQUAL',
                                    value: { defaultValue: true, dynamicValue: null },
                                    type: 'BOOLEAN',
                                },
                            },
                        ],
                    },
                    cmdId: 2,
                },
                {
                    query: {
                        entityFilter: { type: 'entityType', resolveMultiple: true, entityType: 'DEVICE' },
                        keyFilters: [
                            {
                                key: { type: 'ATTRIBUTE', key: 'active' },
                                valueType: 'BOOLEAN',
                                predicate: { operation: 'EQUAL', value: { defaultValue: false }, type: 'BOOLEAN' },
                            },
                        ],
                    },
                    cmdId: 3,
                },
            ],
        };
        return param;
    }

    return {
        wsObject,
        initWebsocket,
        sendData,
        sendAlarmTable,
        sendDeviceStatus,
    };
}