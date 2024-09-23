<template>
    <div class="main" ref="dashboardRef" @scroll="scrollLayout" v-resize>
        <div class="display-grid" v-show="isEdit">
            <div v-for="item in count" :x="item" :key="item" class="grid-item" :style="itemStyle" />
        </div>
        <grid-layout
            v-model:layout="gridItems"
            :col-num="8"
            :cols="8"
            :row-height="rowHeight"
            :is-draggable="true"
            :is-resizable="true"
            :vertical-compact="false"
            :margin="[16, 16]"
            :use-css-transforms="true"
            @layout-updated="layoutUpdatedEvent"
            :key="rowHeight"
            v-if="isLoad"
        >
            <!-- 这里放置的网格布局内容 -->
            <grid-item
                v-for="item in gridItems"
                :x="item.x"
                :y="item.y"
                :w="item.w"
                :h="item.h"
                :i="item.i"
                :key="item.i"
                :max-w="item.maxW"
                :max-h="item.maxH"
                :min-h="item.minH"
                :min-w="item.minW"
                :is-resizable="item.isResizable"
                :static="!isEdit"
            >
                <!-- 编辑部件的按钮，编辑和删除 -->
                <div class="item-btn-panel" v-show="isEdit">
                    <i class="icon-edit" @click="editWidget(item)" v-show="item.type != 'Alarm table'" />
                    <i class="icon-delete" @click="delWidget(item)" />
                </div>
                <component
                    :is="item.component"
                    :data="item.data"
                    :total="total"
                    @search="searchAlarmTable"
                    :cmdId="item.cmdId"
                />
            </grid-item>
        </grid-layout>
    </div>
</template>

<script setup lang="ts">
import { $t } from '@/plugins/i18n';
import { ref, onBeforeMount, onMounted, computed, watch, shallowRef, nextTick } from 'vue';
import { GridLayout, GridItem } from 'vue3-grid-layout-next';
// 引入3个组件
import DeviceStatus from '../components/widgets/device-status.vue';
import SnapshotPreview from '../components/widgets/SnapshotPreview.vue';
import AlarmTable from '../components/widgets/alarm-table.vue';
import { updateWidget } from '@/api/dashboard';
import { emitter } from '@/utils/mitt';
import { getComponentType, widgetsLayoutLimit1 } from '@/views/dashboard/components/widgets/forms';
import { getDashboardById } from '@/api/dashboard';
import { useWebsocket } from './websocket';
import MsWebSocket from '@/utils/websocket/index';
import { getToken, formatToken, getRefreshToken } from '@/utils/auth';
import { message, confirmBox } from '@/utils/message';
const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return {};
        },
    },
    wsObject: {
        type: Object,
    },
});
const isLoad = ref(false);
let cmdId = 1;
const { sendAlarmTable, sendDeviceStatus } = useWebsocket();
onBeforeMount(() => {
    initWebsocket();
    //console.log('2.组件挂载页面之前执行----onBeforeMount')
    console.log(GridLayout);
});
const getItemStyle = detail => {
    //获取行高，初始化时获取行高，确保区域为正方形
    const width = dashboardRef.value?.clientWidth;
    if (!width) return;
    console.log(width, width - 8 * 16, (width - 8 * 16) / 8);
    const colWidth = (width - 8 * 16) / 8 - 2;
    rowHeight.value = colWidth;
    //itemStyle.value = { width: `${colWidth}px`, height: `${colWidth}px` };
    const newCount = parseInt((detail.height || dashboardRef.value.clientHeight) / colWidth + 1) * 8;
    if (count.value < count.value) count.value = newCount;
};
onMounted(() => {
    getItemStyle({ height: 40 });
});
emitter.on('resize', ({ detail }) => {
    nextTick(() => {
        getItemStyle(detail);
    });
});
watch(
    () => props.data,
    val => {
        dashboardData.value = val;
        console.log('layoutupdate-data');
        getLayoutData();
    },
);

const dashboardData = ref(props.data);
const itemStyle = computed(() => {
    return { width: `${rowHeight.value}px`, height: `${rowHeight.value}px` };
});
const dashboardRef = ref(null);
const rowHeight = ref(86);
const count = ref(40);
const isEdit = ref(false);
// widgets == col:x, row: y, sizeX: w, sizeY: h, id: i ,typeAlias:组件类型,【 layout固定得是x,y,w,h】

/** 定义网格中每个item的尺寸属性与伸缩能力，并赋予业务组件类型 */
const gridItems = ref([
    {
        x: 0,
        y: 0,
        w: 2,
        h: 2,
        i: 'd0ab7326-6015-4cc3-a326-e1bf022d5395',
        type: 'Alarm table',
        maxW: 3,
        maxH: 3,
        minW: 2,
        minH: 2,
        component: shallowRef(SnapshotPreview),
        isResizable: false,
        data: {},
        cmdId: 0,
    },
]);

const componentEnum = {
    'Active devices': shallowRef(DeviceStatus),
    'Inactive devices': shallowRef(DeviceStatus),
    'Total devices': shallowRef(DeviceStatus),
    'Snapshot preview': shallowRef(SnapshotPreview),
    'Alarm table': shallowRef(AlarmTable),
};

async function getLayoutData() {
    // 根据仪表板id查询仪表板对象
    //dashboardData.value = await getDashboardById(dashboardData.value.id.id);//重复拉取
    cancelItemData(gridItems.value);
    gridItems.value = setGridItems(dashboardData.value.configuration.widgets);
    console.log('ger--layoutupdate-data');
    getGridItemSendData(gridItems.value);
    isLoad.value = true;
}
/**
 * 保存后再次更新GridLayout
 */
async function saveUpdateLayoutData() {
    // 根据仪表板id查询仪表板对象
    dashboardData.value = await getDashboardById(dashboardData.value.id.id);
    cancelItemData(gridItems.value);
    gridItems.value = setGridItems(dashboardData.value.configuration.widgets);
    getGridItemSendData(gridItems.value);
}

/**
 * 组件布局对象(widgets是以id为作为key的一个)转换为GridLayout数据
 * @return gridItems
 */
function setGridItems(widgetObj: object) {
    if (!widgetObj) return [];
    return Object.keys(widgetObj).map(key => {
        const typeNum = getComponentType(widgetObj[key].typeAlias).type;
        const res = {
            // 布局尺寸属性
            x: widgetObj[key].col,
            y: widgetObj[key].row,
            w: widgetObj[key].sizeX,
            h: widgetObj[key].sizeY,
            // 组件类型与数据属性
            i: widgetObj[key].id,
            type: widgetObj[key].typeAlias || widgetObj[key].type,
            component: componentEnum[widgetObj[key].typeAlias],
            ...widgetsLayoutLimit1[widgetObj[key].typeAlias],
            data: {},
        };
        if (widgetObj[key].typeAlias == 'Snapshot preview') {
            res.sensingObjectImage = widgetObj[key].sensingChannel;
        }
        return res;
    });
}

//进入编辑状态
const editLayout = val => {
    isEdit.value = val;
    return isEdit.value;
};
/**
 * @description: 根据部件id获取部件信息，进行编辑
 * @param {*} id 部件id
 * @return {*}
 */
const emit = defineEmits(['edit-widget']);
const editWidget = item => {
    console.log('编辑部件', item);
    emit('edit-widget', item);
};
const delWidget = async del => {
    const isConfirm = await confirmBox($t('widget.deleteTips'), {
        title: $t('widget.sureDelete'),
    });
    if (isConfirm) {
        const idList = gridItems.value.map(item => item.i);
        const index = idList.indexOf(del.i);
        if (index != -1) {
            gridItems.value.splice(index, 1);
        }
    }
};
let layoutInfo = [];
const layoutUpdatedEvent = newLayout => {
    layoutInfo = newLayout;
    let rows = 0;
    if (newLayout.length) {
        const maxIdElement = newLayout.reduce((prev, current) => (prev.y > current.y ? prev : current));
        rows = maxIdElement.y + maxIdElement.h;
        const newCount = rows * 8;
        if (count.value < newCount) count.value = newCount;
        console.log('Updated layout: ', newLayout);
    }
};
const scrollLayout = () => {
    const rowH = rowHeight.value + 16;
    const newCount = dashboardRef.value.clientHeight / rowH;
    if (count.value < newCount) count.value = newCount;
};

function getGridItems() {
    return gridItems.value.map(item => {
        const res = {
            col: item.x,
            row: item.y,
            sizeX: item.w,
            sizeY: item.h,
            id: item.i,
            type: item.type,
        };
        if (item.type == 'Snapshot preview') {
            res.sensingObjectImage = item.sensingObjectImage;
        }
        return res;
    });
}

function getDefaultItem(editItem) {
    let res = {};
    if (editItem.type == 'Total devices' || editItem.type == 'Active devices' || editItem.type == 'Inactive devices') {
        console.log('componentEnum[editItem.type]', componentEnum[editItem.type]);
        res = {
            x: 0,
            y: 0,
            w: 1,
            h: 1,
            isResizable: false,
            i: '',
            type: editItem.type,
            component: componentEnum[editItem.type],
            ...widgetsLayoutLimit1[editItem.type],
            data: {},
        };
    } else if (editItem.type == 'Snapshot preview') {
        res = {
            x: 0,
            y: 0,
            w: 2,
            h: 2,
            isResizable: true,
            i: '',
            type: editItem.type,
            component: componentEnum[editItem.type],
            ...widgetsLayoutLimit1[editItem.type],
            data: {
                name: editItem.sensingObjectImage.name,
            },
        };
    } else if (editItem.type == 'Alarm table') {
        res = {
            x: 0,
            y: 0,
            w: 3,
            h: 2,
            isResizable: true,
            i: '',
            type: editItem.type,
            component: componentEnum[editItem.type],
            ...widgetsLayoutLimit1[editItem.type],
            data: {},
        };
    }
    return res;
}

// const addItemQueues = [];
/** 新增或编辑组件的信息后，更新Layout */
function editGridItem(editItem) {
    console.log('新增或编辑组件的信息后，更新Layout', editItem);

    if (editItem.i) {
        const index = gridItems.value.findIndex(item => item.i == editItem.i);
        const curCmdId = gridItems.value[index].cmdId;
        gridItems.value[index] = editItem;
        updateItemSendData(editItem, curCmdId);
    } else {
        const res = getDefaultItem(editItem);

        //const { x, y } = findAvailablePosition(layoutInfo, res.w, res.h);
        console.log(res);
        editItem = {
            ...res,
            ...editItem,
        };
        editItem.i = Date.now();
        editItem.isNew = true;
        console.log(editItem);
        gridItems.value.push(editItem);
        sendNewItemData(editItem);
    }
}

function findAvailablePosition(objects, newObjectWidth, newObjectHeight) {
    // 对已有对象按照 x 坐标进行排序
    const sortedObjects = objects.sort((a, b) => a.x - b.x);

    let currentX = 0;
    let currentY = 0;

    for (const object of sortedObjects) {
        // 检查当前位置是否可用
        if (currentX + newObjectWidth <= object.x) {
            // 找到可用位置
            return { x: currentX, y: currentY };
        }

        // 更新下一个位置的 x, y 坐标
        currentX = object.x + object.w;

        // 查找当前行中可以放置新对象的最低高度
        let maxHeightInRow = currentY;
        for (const obj of sortedObjects) {
            if (obj.y <= currentY + newObjectHeight && currentX >= obj.x && currentX <= obj.x + obj.w) {
                maxHeightInRow = Math.max(maxHeightInRow, obj.y + obj.h);
            }
        }

        // 检查当前行是否足够放置新对象
        if (maxHeightInRow + newObjectHeight <= currentY) {
            // 找到可用位置
            return { x: currentX, y: maxHeightInRow };
        }

        // 更新下一个位置的 y 坐标
        currentY = maxHeightInRow;
    }

    // 处理最后一个对象之后的位置
    return { x: currentX, y: currentY };
}

async function handleSave() {
    const widgets = getGridItems();
    const dashboard = { ...dashboardData.value };
    delete dashboard.configuration;
    const params = {
        dashboard,
        widgetDTOList: widgets,
    };
    await updateWidget(params);
    //getSnapSendData(gridItems.value);
}

/** Websocket Start */
const wsObject = ref(null);
let totalCount = 0;
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
            sendData();
        },
        handlerMessage,
    };
    wsObject.value = new MsWebSocket(wsOption);
}
/** 当Websocket完成连接并且gridItems也返回之后才能开始sendData */
function sendData() {
    if (cmdId !== 1) {
        cmdId = 1;
        getGridItemSendData(gridItems.value);
    }
}

const getDeviceSendData = type => {
    const deviceData = {
        'Active devices': {
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
        },
        'Inactive devices': {
            query: {
                entityFilter: {
                    type: 'entityType',
                    resolveMultiple: true,
                    entityType: 'DEVICE',
                },
                keyFilters: [
                    {
                        key: {
                            type: 'ATTRIBUTE',
                            key: 'active',
                        },
                        valueType: 'BOOLEAN',
                        predicate: {
                            operation: 'EQUAL',
                            value: {
                                defaultValue: false,
                                dynamicValue: null,
                            },
                            type: 'BOOLEAN',
                        },
                    },
                ],
            },
        },
        'Total devices': {
            query: {
                entityFilter: {
                    type: 'entityType',
                    resolveMultiple: true,
                    entityType: 'DEVICE',
                },
            },
        },
    };
    return deviceData[type];
};
/** websocket.send */
function getGridItemSendData(gridItems) {
    console.log('getGridItemSendData', gridItems);
    const itemCount = gridItems.length;
    if (!itemCount) return;
    const msAlarmDataCmds = [];
    const entityCountCmds = [];
    const msSnapshotPreviewCmd = [];
    for (let i = 0; i < itemCount; i++) {
        const item = gridItems[i];
        console.log();
        let query = {};
        switch (item.type) {
            case 'Alarm table':
                query = {
                    pageLink: {
                        //分页信息
                        page: 0,
                        pageSize: 10,
                        textSearch: null,
                        sortOrder: {
                            property: 'createdTime',
                            direction: 'DESC',
                        },
                    },
                };
                msAlarmDataCmds.push({ query, cmdId: i + cmdId });
                //item.data = { currentPage: 1 };
                break;
            case 'Snapshot preview':
                const [deviceId, abilityId] = item.sensingObjectImage?.deviceAbilityIds.split(',');
                query = {
                    sensingObjectId: item.sensingObjectImage?.id,
                    deviceId,
                    abilityId,
                };
                msSnapshotPreviewCmd.push({ query, cmdId: i + cmdId });
                break;
            default:
                entityCountCmds.push({ query: getDeviceSendData(item.type)?.query, cmdId: i + cmdId });
                break;
        }
        item.cmdId = i + cmdId;
    }
    if (cmdId == 1) {
        if (entityCountCmds.length) {
            totalCount = 1;
            entityCountCmds.push({ query: getDeviceSendData('Total devices')?.query, cmdId: 0 });
        }
    }
    cmdId += itemCount;
    const params = {
        msAlarmDataCmds,
        entityCountCmds,
        msSnapshotPreviewCmd,
    };

    wsObject.value.send(JSON.stringify(params));
    // Snapshot preview
    //getSnapSendData(gridItems);
}

function sendNewItemData(item) {
    const msAlarmDataCmds = [];
    const entityCountCmds = [];
    const msSnapshotPreviewCmd = [];
    let query = {};
    switch (item.type) {
        case 'Alarm table':
            query = {
                pageLink: {
                    //分页信息
                    page: 0,
                    pageSize: 10,
                    textSearch: null,
                    sortOrder: {
                        property: 'createdTime',
                        direction: 'DESC',
                    },
                },
            };
            msAlarmDataCmds.push({ query, cmdId: 1 + cmdId });
            break;
        case 'Snapshot preview':
            // const devices = item.sensingObjectImage?.sensingChannels.map(item => item.deviceId);
            // query = {
            //     deviceId: devices[0],
            //     abilityId: item.sensingObjectImage?.deviceAbilityIds,
            // };
            const [deviceId, abilityId] = item.sensingObjectImage?.deviceAbilityIds.split(',');
            query = {
                sensingObjectId: item.sensingObjectImage?.id,
                deviceId,
                abilityId,
            };
            msSnapshotPreviewCmd.push({ query, cmdId: 1 + cmdId });
            break;
        default:
            entityCountCmds.push({ query: getDeviceSendData(item.type)?.query, cmdId: 1 + cmdId });
            break;
    }
    if (!totalCount) {
        if (entityCountCmds.length) {
            totalCount = 1;
            entityCountCmds.push({ query: getDeviceSendData('Total devices')?.query, cmdId: 0 });
        }
    }
    cmdId += 1;
    const params = {
        msAlarmDataCmds,
        entityCountCmds,
        msSnapshotPreviewCmd,
    };
    item.cmdId = cmdId;

    wsObject.value.send(JSON.stringify(params));
}

function updateItemSendData(item, curCmdid) {
    const msAlarmDataCmds = [];
    const entityCountCmds = [];
    const msSnapshotPreviewCmd = [];
    let query = {};
    switch (item.type) {
        case 'Alarm table':
            query = {
                pageLink: {
                    //分页信息
                    page: 0,
                    pageSize: 10,
                    textSearch: null,
                    sortOrder: {
                        property: 'createdTime',
                        direction: 'DESC',
                    },
                },
            };
            msAlarmDataCmds.push({ query, cmdId: 1 + cmdId });
            //item.data = { currentPage: 1 };
            break;
        case 'Snapshot preview':
            const [deviceId, abilityId] = item.sensingObjectImage?.deviceAbilityIds.split(',');
            query = {
                sensingObjectId: item.sensingObjectImage?.id,
                deviceId,
                abilityId,
            };
            msSnapshotPreviewCmd.push({ query, cmdId: 1 + cmdId });
            break;
        default:
            entityCountCmds.push({ query: getDeviceSendData(item.type)?.query, cmdId: 1 + cmdId });
            break;
    }
    cmdId += 1;
    const params = {
        msAlarmDataCmds,
        entityCountCmds,
        msSnapshotPreviewCmd,
        entityDataUnsubscribeCmds: [{ cmdId: curCmdid }],
    };
    item.cmdId = cmdId;

    wsObject.value.send(JSON.stringify(params));
}

function cancelItemData(gridItems) {
    const itemCount = gridItems.length;
    if (!itemCount) return;
    const entityDataUnsubscribeCmds = [];
    for (let i = 0; i < itemCount; i++) {
        const item = gridItems[i];
        if (item.cmdId) entityDataUnsubscribeCmds.push({ cmdId: item.cmdId });
    }
    const params = {
        entityDataUnsubscribeCmds,
    };

    wsObject.value.send(JSON.stringify(params));
}

function getSnapSendData(gridItems) {
    if (gridItems.length == 0) return;
    for (let i = 0, il = gridItems.length; i < il; i++) {
        const item = gridItems[i];
        let param = {};
        // 根据对应的Type值，发送send
        if (item.type == 'Snapshot preview') {
            const devices = item.sensingObjectImage?.sensingChannels.map(item => item.deviceId);
            if (!devices) continue;
            param = {
                key: 'dashboard_snapshot_preview',
                tenantId: dashboardData.value.tenantId.id,
                deviceIds: devices,
            };
        }
        if (JSON.stringify(param) != '{}') {
            wsObject.value.send(JSON.stringify(param));
        }
    }
}

function searchAlarmTable(
    queryParams = {
        pageSize: 30,
        page: 0,
        sortProperty: 'createdTime',
    },
    curCmdid,
) {
    const itemIndex = gridItems.value.findIndex(item => item.cmdId == curCmdid);
    //let item = gridItems.value[itemIndex];
    console.log('searchAlarmTable', queryParams);
    const msAlarmDataCmds = [];
    let query = {
        pageLink: {
            //分页信息
            page: queryParams.page,
            pageSize: queryParams.pageSize,
            textSearch: null,
            sortOrder: {
                property: 'createdTime',
                direction: 'DESC',
            },
        },
    };
    //item.data.currentPage = queryParams.page + 1;
    msAlarmDataCmds.push({ query, cmdId: curCmdid });
    const params = {
        msAlarmDataCmds,
    };
    wsObject.value.send(JSON.stringify(params));
}

const total = ref(1);
function handlerMessage(data) {
    if (!data) return;
    // 属于DeviceStatus类别的
    if (data.cmdId) {
        const itemIndex = gridItems.value.findIndex(item => item.cmdId == data.cmdId);
        const item = gridItems.value[itemIndex];
        switch (item.type) {
            case 'Alarm table':
                //let pageNow = item.data?.currentPage || 1;
                item.data = data.data;
                //item.data.currenPage = pageNow;
                break;
            case 'Snapshot preview':
                const { tsKvEntry, errorMsg, deviceAbility, sensingObject } = data;
                const name = sensingObject?.name || item.sensingObjectImage.name;
                if (errorMsg) {
                    item.data = {
                        name,
                    };
                } else {
                    const img = tsKvEntry?.value;
                    const ts = tsKvEntry?.ts;
                    const extraInfo = deviceAbility?.extraInfo;
                    item.data = {
                        name,
                        img,
                        ts,
                        extraInfo,
                    };
                }

                break;
            default:
                const statusText = {
                    'Total devices': 1,
                    'Active devices': 2,
                    'Inactive devices': 3,
                };
                item.data = { ...data, type: statusText[item.type] };
                break;
        }
        console.log(data.cmdId, item.data);
    } else {
        total.value = data.count;
    }
}

defineExpose({ editLayout, getGridItems, handleSave, editGridItem });
</script>
<style scoped lang="scss">
.main {
    padding: 0 !important;
    height: auto !important;
    min-width: 1350px;
    //overflow: auto;
}
.vue-grid-item {
    border-radius: 5px;
    background: #fff;
    box-shadow: 2px 2px 4px #00000029;
}
.display-grid {
    display: flex;
    flex-wrap: wrap;
    margin-top: 8px;
    margin-left: 8px;
    position: absolute;
}
.grid-item {
    border: 1px dashed;
    margin: 8px;
    display: inline-block;
}
.item-btn-panel {
    z-index: 2;
    position: absolute;
    right: 10px;
    i {
        font-size: 24px;
        color: #707070;
        cursor: pointer;
    }
}
</style>
