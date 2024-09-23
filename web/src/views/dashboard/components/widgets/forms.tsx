import DeviceStatus from './device-form.vue';
const deviceStatusType = {
    1: 'widget.totalDevices',
    2: 'widget.activeDevices',
    3: 'widget.inactiveDevices',
};
const widgetsKeys = {
    1: 'snapshotPreview',
    2: 'deviceStatus',
    3: 'alarmTable',
};

/** 不同类型组件的缩放限制 */
const widgetsLayoutLimit = {
    snapshotPreview: { isResizable: true, def: { w: 2, h: 2 }, maxW: 3, maxH: 3, minW: 2, minH: 2 },
    deviceStatus: { isResizable: false, def: { w: 1, h: 1 }, maxW: 1, maxH: 1, minW: 1, minH: 1 },
    alarmTable: { isResizable: true, def: { w: 3, h: 2 }, maxW: 4, maxH: 4, minW: 3, minH: 2 },
};

/** 不同类型组件的缩放限制 */
const widgetsLayoutLimit1 = {
    'Alarm table': { isResizable: true, def: { w: 3, h: 2 }, maxW: 4, maxH: 4, minW: 3, minH: 2 },
    'Snapshot preview': { isResizable: true, def: { w: 2, h: 2 }, maxW: 3, maxH: 3, minW: 2, minH: 2 },
    'Active devices': { isResizable: false, def: { w: 1, h: 1 }, maxW: 1, maxH: 1, minW: 1, minH: 1 },
    'Inactive devices': { isResizable: false, def: { w: 1, h: 1 }, maxW: 1, maxH: 1, minW: 1, minH: 1 },
    'Total devices': { isResizable: false, def: { w: 1, h: 1 }, maxW: 1, maxH: 1, minW: 1, minH: 1 },
};

const forms = {
    deviceStatus: {
        key: 'deviceStatus',
        component: DeviceStatus,
    },
};
const deviceStatus = { 'Active devices': 2, 'Inactive devices': 3, 'Total devices': 1 };
/*
    aliasType :Active devices、Inactive devices、Total devices、Snapshot preview、Alarm table
    根据组件别名，对应组件类型
*/
const getComponentType = aliasType => {
    // const deviceStatus = { 'Active devices': 2, 'Inactive devices': 3, 'Total devices': 1 };
    const snapshotPreview = 'Snapshot preview';
    const alarmTable = 'Alarm table';
    if (aliasType == snapshotPreview) {
        return { type: 1 };
    } else if (aliasType == alarmTable) {
        return { type: 3 };
    } else {
        return { type: 2, subType: deviceStatus[aliasType] };
    }
};

export {
    deviceStatusType,
    widgetsKeys,
    widgetsLayoutLimit,
    forms,
    getComponentType,
    deviceStatus,
    widgetsLayoutLimit1,
};
