<template>
    <div class="main">
        <header class="bg-[#fff] shadow-sm shadow-[rgba(0, 21, 41, 0.08)] dark:shadow-[#0d0d0d]">
            <div class="title">{{ dashBoardTitle }}</div>
            <div class="flex">
                <el-select
                    v-model="curDashboard"
                    @change="changeDashboard"
                    :loading="loading"
                    value-key="id"
                    class="mr-3"
                >
                    <el-option v-for="item in dashboardList" :key="item.value" :label="item.name" :value="item" />
                </el-select>
                <el-button class="!text-gray-700 !min-w-0 !w-10" @click="gotoList" type="text" v-if="!publicId">
                    <i class="icon-dashboards" />
                </el-button>
                <el-button class="!text-gray-700 !min-w-0 !w-2 mr-3" @click="changeContentFullScreen" type="text">
                    <i :class="isFullscreen ? 'icon-exit-fullscreen' : 'icon-fullscreen'" />
                </el-button>
            </div>
        </header>
        <div class="main-content">
            <GridLayout ref="gridLayoutRef" :data="layoutData" @edit-widget="showEditWidgetDialog" />
        </div>
        <div class="btn-bottom">
            <el-button round class="inter-btn widget-edit" v-show="!isEdit" @click="changeEdit()">
                <i class="icon-edit" />
            </el-button>
            <div class="widget-detail" v-show="isEdit">
                <el-button class="inter-btn" round @click="handleShowWidgetType">
                    <i class="icon-add" />
                </el-button>
                <el-button class="inter-btn" round @click="handleSave">
                    <i class="icon-selected" />
                </el-button>
                <el-button class="inter-btn" round @click="handleCancel()">
                    <i class="icon-eliminate" />
                </el-button>
            </div>
        </div>

        <!-- 新增组件或编辑组件的弹窗 -->
        <WidgetDialog
            v-model:visible="formDialogVisible"
            :descript="widgetInfo"
            :data="widgetData"
            @update="updateLayout"
        />
        <RightDialog @add="showAddWidgetDialog" />
    </div>
</template>

<script setup lang="ts">
import { $t } from '@/plugins/i18n';
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useNav } from '@/layout/hooks/useNav';
import { getSystemParams, getPublicDashboardList, getDashboardList, getDashboardById } from '@/api/dashboard';
import { emitter } from '@/utils/mitt';
import { useTags } from '@/layout/hooks/useTag';
import RightDialog from './components/rightDialog.vue';
import WidgetDialog from './components/widget-dialog.vue';
import GridLayout from './grid-layout.vue';
const { logout } = useNav();
const router = useRouter();
let publicId: string | string[] = '';
const getRouterData = async () => {
    const dashboardId = router.currentRoute.value.redirectedFrom?.params?.id || router.currentRoute.value.params.id;
    publicId = router.currentRoute.value.redirectedFrom?.query?.publicId || router.currentRoute.value.query?.publicId;
    if (publicId) {
        const sysParams = await getSystemParams();
        if (!sysParams?.allowedDashboardIds.includes(dashboardId)) {
            logout();
        }
    }
    return dashboardId;
};
const changeDashboard = val => {
    const dashboardId = val.id?.id;
    if (publicId) router.push({ path: `/dashboards/${dashboardId}`, query: { publicId } });
    else router.push({ path: `/dashboards/${dashboardId}` });
};
onMounted(async () => {
    console.log('onMounted');
    await getRouterData();
    await getOptionList();
    const dashboardId = router.currentRoute.value.params.id;
    let defaultItem;
    if (dashboardId == ':id') {
        defaultItem = dashboardList.value.find(item => item.createdTime == 253402271999000);
    } else {
        defaultItem = dashboardList.value.find(item => item.id.id == dashboardId);
    }
    getDashboardLayout(defaultItem);
});

const isEdit = ref(false);

const dashboardList = ref([]);
const curDashboard = ref({
    id: {
        id: '',
    },
});
const loading = ref(true);
async function getOptionList(
    queryParams = {
        pageSize: 30,
        page: 0,
        sortProperty: 'createdTime',
        sortOrder: 'DESC',
    },
) {
    let tmpData = [];
    let hasNextPage = true;
    while (hasNextPage) {
        const { data, hasNext } = await (publicId
            ? getPublicDashboardList(publicId, queryParams)
            : getDashboardList(queryParams));
        queryParams.page += 1;
        tmpData = tmpData.concat(data);
        hasNextPage = hasNext;
    }
    dashboardList.value = tmpData;
    loading.value = false;
}

const dashBoardTitle = ref();

const layoutData = ref();

/**
 * 切换仪表盘选择器，由于保存配置时需要传仪表盘对象，因此在此保存
 */
async function getDashboardLayout(object) {
    console.log('getDashboardLayout', object);
    curDashboard.value = object;
    dashBoardTitle.value = object.name;
    emitter.emit('updateBread', object.name);
    // 根据仪表板id查询仪表板对象
    const data = await getDashboardById(object.id.id);
    layoutData.value = data;
}

function gotoList() {
    router.push('/dashboards');
}

const isFullscreen = ref(false);
const { onContentFullScreen } = useTags();
function changeContentFullScreen() {
    isFullscreen.value = onContentFullScreen();
}

function handleShowWidgetType() {
    emitter.emit('showPanel', true);
}
//仪表板内组件编辑控制
const formDialogVisible = ref(false);
/** 组件数据信息，包含Dashboard对象 */
const widgetData = ref({});
/** 组件类型信息 */
const widgetInfo = ref({});

/**
 * 点击右侧新增后显示对应新增组件弹窗
 * @param param0
 */
function showAddWidgetDialog({ type }) {
    // 关闭右侧弹窗
    emitter.emit('showPanel', false);
    widgetData.value.id = '';
    if (type == 1) {
        // Snapshot
        widgetData.value = { sensingChannel: [] };
        widgetInfo.value = { type: 1, isNew: true };
        formDialogVisible.value = true;
    } else if (type == 3) {
        // 直接添加组件到仪表盘（该组件无弹窗配置）
        updateLayout({
            type: 'Alarm table',
        });
    } else if (type == 2) {
        // Device
        widgetInfo.value = { type: 2, isNew: true };
        widgetData.value = { type: 'Active devices' };
        formDialogVisible.value = true;
    }
}
function showEditWidgetDialog(item) {
    const { i, type } = item;
    widgetInfo.value = { type, i, isNew: false };
    // widgetData.value = {
    //     dashboard: layoutData.value,
    //     id: i,
    // };
    formDialogVisible.value = true;
    if (type == 'Snapshot preview') {
        widgetData.value = { ...item };
        widgetInfo.value = { type: 1, isNew: true };
        formDialogVisible.value = true;
    } else if (type == 'Alarm table') {
        // Alarm table无法编辑
    } else {
        widgetInfo.value = { type: 2, isNew: false };
        widgetData.value = item;
        formDialogVisible.value = true;
    }
}

/** 新增或编辑组件的信息后，更新Layout */
function updateLayout(widgetItem) {
    console.log('index updateLayout widgetItem', widgetItem);
    gridLayoutRef.value.editGridItem(widgetItem);
}

const gridLayoutRef = ref();
function changeEdit(editStatus?: boolean) {
    const val = editStatus == undefined ? !isEdit.value : editStatus;
    const res = gridLayoutRef.value.editLayout(val);
    isEdit.value = res;
}

/** 保存仪表盘组件数据 */
async function handleSave() {
    gridLayoutRef.value.handleSave();
    changeEdit(false);
}
async function handleCancel() {
    changeEdit(false);
    // 根据仪表板id查询仪表板对象
    const data = await getDashboardById(curDashboard.value.id.id);
    layoutData.value = data;
    console.log(data);
}
</script>
<style scoped lang="scss">
.main {
    padding: 0 !important;
    height: auto !important;
}
.main {
    // position: relative;
    padding: 0 !important;
    header {
        width: 100%;
        height: 37px;
        padding: 0 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .main-content {
        // background: #ebebeb;
        // box-shadow: 0 3px 3px rgb(0 0 0 / 16%);
        display: flex;
        flex-direction: column;
        height: 100%;
        padding: 16px;
    }
    .btn-bottom {
        position: fixed;
        right: 30px;
        bottom: 30px;

        .el-button.is-round {
            width: 50px !important;
            height: 50px !important;
            min-width: unset;
            padding: 10px !important;
            border-radius: 50%;
        }
    }
}
.vue-grid-item {
    background: #ccc;
    border: 1px solid #000;
}
.display-grid {
    display: flex;
    flex-wrap: wrap;
    margin-top: 8px;
    margin-left: 8px;
    position: absolute;
}
.grid-item {
    width: 196px;
    height: 196px;
    border: 1px dashed;
    margin: 8px;
    display: inline-block;
}
.item-btn-panel {
    text-align: right;
    i {
        font-size: 20px;
    }
}
</style>
