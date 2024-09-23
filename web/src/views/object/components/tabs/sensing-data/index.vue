<script setup lang="ts">
import { delay, addClass } from '@pureadmin/utils';
import { ref, onMounted, reactive, watch } from 'vue';
import type { PaginationProps } from '@pureadmin/table';
import { message } from '@/utils/message';
import { $t } from '@/plugins/i18n';
import { getHistroyTelemetryList } from '@/api/sensing-object';
import { getTelemetryImg, setTelmetryRecognition } from '@/api/device-ability';
import {
    dateTimeFormat,
    getDateTime,
    getTableColums,
    downloadCsv,
    downloadJson,
    downloadBase64Img,
} from '@/utils/tools';
import Cropper from '@/components/ReCropper';
import { renderVal } from './hook';
const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return {};
        },
    },
});
const dataList = ref([]);
const loading = ref(true);
const tableSize = ref('default');
/** 分页配置 */
const pagination = reactive<PaginationProps>({
    total: 0,
    pageSizes: [10, 20, 30],
    pageSize: 30,
    currentPage: 1,
});
const searchFormData = ref({ channel: {}, timeRange: [] });
const channelList = ref([]);
const columns: TableColumnList = getTableColums([
    {
        type: 'selection',
        align: 'left',
        width: 65,
    },
    {
        label: 'common.createdTime',
        prop: 'ts',
        formatter: dateTimeFormat,
    },
    {
        label: 'common.value',
        prop: 'value',
        cellRenderer: renderVal,
    },
    {
        label: '',
        fixed: 'right',
        width: 180,
        slot: 'operation',
    },
]);
async function initData() {
    const sensingChannels = props.data.sensingChannels;
    const array = formatChannel(sensingChannels);
    channelList.value = [...array];
    const startTs = new Date(new Date().setHours(0, 0, 0, 0));
    const endTs = new Date(new Date().setHours(0, 0, 0, 0) + 24 * 60 * 60 * 1000 - 1);
    searchFormData.value = {
        channel: array[0]?.id,
        channelName: array[0]?.name,
        timeRange: [startTs, endTs],
    };
    onSearch();
}
const onSearch = async (
    queryParams = {
        pageSize: pagination.pageSize,
        page: 0,
        sortProperty: 'ts',
        sortOrder: 'DESC',
    },
) => {
    if (channelList.value.length < 1) {
        dataList.value = [];
        loading.value = false;
    } else {
        loading.value = true;
        const { timeRange } = searchFormData.value;
        const searchParams = {
            startTs: new Date(timeRange[0]).getTime(),
            endTs: new Date(timeRange[1]).getTime(),
        };
        queryParams = {
            ...searchFormData.value.channel,
            ...searchParams,
            ...queryParams,
        };
        const { data, totalElements } = await getHistroyTelemetryList(queryParams);
        dataList.value = data;
        pagination.total = totalElements;
        pagination.currentPage = queryParams.page + 1;
        delay(600).then(() => {
            loading.value = false;
        });
    }
};
const formatChannel = sensingChannels => {
    return sensingChannels.map(item => {
        return {
            name: `${item.deviceName}/${item.ability === 'image' ? $t('device.fullImg') : item.ability}`,
            id: {
                deviceId: item.deviceId,
                ability: item.ability,
                deviceAbilityId: item.deviceAbilityId,
            },
        };
    });
};
const setChannelName = val => {
    const itemMatch = channelList.value.filter(item => {
        return item.id.deviceId == val.deviceId && item.id.ability == val.ability;
    });
    searchFormData.value.channelName = itemMatch[0]?.name;
};
watch(
    () => props.data,
    val => {
        console.log('changeData');
        initData();
    },
);

onMounted(() => {
    initData();
    showDate(true);
});

const formVisible = ref(false);
const imgUrl = ref('');
const formData = ref({ ...props.data });
const refCropper = ref();
let roiExtraInfo = '';
let openRow = {};
const openDetail = async row => {
    console.log(row);
    openRow = row;
    const { deviceId, ts } = row;
    const imageData = await getTelemetryImg({ deviceId, ts });
    imgUrl.value = imageData;
    formData.value = row.ability == 'image' ? '' : JSON.parse(row.value);
    roiExtraInfo = row.extraInfo;
    formVisible.value = true;
};
const initRoi = () => {
    if (roiExtraInfo) {
        const [x, y, width, height] = roiExtraInfo.split(',');
        refCropper.value.cropper.crop();
        refCropper.value.cropper.setData({
            width: Number(width),
            height: Number(height),
            x: Number(x),
            y: Number(y),
        });
    }
    refCropper.value.cropper.disable();
};
const closeDialog = () => {
    formVisible.value = false;
};
const showDownLoad = ref(false);
const downloadTips = ref('');
const tableListRef = ref();
let multipleSelection = [];
const handleSelectionChange = val => {
    const count = val.length;
    const key = count > 1 ? 'object.datasSel' : 'object.dataSel';
    multipleSelection = val;
    console.log(val);
    downloadTips.value = $t(key).replace('%s', count);
    showDownLoad.value = count;
};
const handleClick = row => {
    const { toggleRowSelection } = tableListRef.value.getTableRef();
    toggleRowSelection(row);
};
const downLoadImg = async (item, fileName) => {
    const { deviceId, ts } = item;
    const imageData = await getTelemetryImg({ deviceId, ts });
    const imgTime = getDateTime(ts, 'YYYYMMDDHHmmss');
    downloadBase64Img(imageData, `${fileName}_${imgTime}.jpg`);
};
const downLoadData = async () => {
    const dataInfo = { ...multipleSelection[0] };
    const objectName = props.data.name;
    const channelName = searchFormData.value?.channelName;
    const timeNow = getDateTime(new Date().getTime(), 'YYYYMMDDHHmmss');
    const fileName = `${objectName}_${channelName?.replace('/', '-')}`;
    if (dataInfo.abilityType == 1) {
        const opts = {
            fields: [
                {
                    label: 'Created time',
                    value: row => {
                        return getDateTime(row.ts);
                    },
                },
                {
                    label: 'Key',
                    value: 'key',
                    default: channelName,
                },
                {
                    label: 'Value',
                    value: 'value',
                },
            ],
        };
        downloadCsv(multipleSelection, `${fileName}_${timeNow}.csv`, opts);
    } else if (dataInfo.ability == 'image') {
        multipleSelection.map((item, index) => {
            setTimeout(() => {
                downLoadImg(item, fileName);
            }, 200 * index);
        });
    } else {
        const dataList = {};
        multipleSelection.map((item, index) => {
            const roiObj = JSON.parse(item.textJson);
            dataList[index] = {
                'Created time': getDateTime(item.createdTime),
                ...roiObj,
            };
            return '';
        });
        downloadJson(dataList, `${fileName}_${timeNow}.json`);
    }
    const { clearSelection } = tableListRef.value.getTableRef();
    clearSelection();
    //message($t("status.success"));
};
function handleSizeChange(val: number) {
    console.log(`${val} items per page`);
    const queryParams = {
        pageSize: val,
        page: 0,
        sortProperty: 'ts',
        sortOrder: 'DESC',
    };
    onSearch(queryParams);
}
function handleCurrentChange(val: number) {
    console.log(`current page: ${val}`);
    const { pageSize } = pagination;
    const page = Math.max(val - 1, 0);
    const queryParams = {
        pageSize,
        page,
        sortProperty: 'ts',
        sortOrder: 'DESC',
    };
    onSearch(queryParams);
}
const setRoiInfo = async () => {
    openRow.value = JSON.stringify(formData.value);
    await setTelmetryRecognition(openRow);
    message($t('status.success'));
    closeDialog();
};

let isFirst = true;
const showDate = value => {
    if (value && isFirst) {
        let confimBtn = document.querySelector('.el-picker-panel__footer .is-plain');
        addClass(document.querySelector('.el-picker-panel__footer .is-plain'), 'exter-btn');
        confimBtn.innerHTML = `<span>${$t('buttons.search')}</span>`;
        confimBtn = null;
        isFirst = false;
    }
};
</script>

<template>
    <div class="boder-shadow h-full relative">
        <el-form
            ref="ruleFormRef"
            :model="searchFormData"
            label-position="top"
            require-asterisk-position="right"
            class="search-bar"
        >
            <el-form-item :label="$t('object.sensingChannel')">
                <el-select
                    v-model="searchFormData.channel"
                    value-key="deviceAbilityId"
                    filterable
                    @change="setChannelName"
                >
                    <el-option v-for="item in channelList" :key="item.name" :label="item.name" :value="item.id" />
                </el-select>
            </el-form-item>
            <el-form-item :label="$t('common.timeRange')">
                <el-date-picker
                    ref="dateRef"
                    v-model="searchFormData.timeRange"
                    type="datetimerange"
                    range-separator="To"
                    @change="onSearch()"
                    :clearable="false"
                />
            </el-form-item>
            <el-button @click="onSearch()" class="exter-btn" :disabled="channelList.length < 1">
                {{ $t('buttons.search') }}</el-button
            >
        </el-form>
        <div class="downlod-panel w-full h-[100px] p-4 bg-dark absolute" v-show="showDownLoad">
            <p>{{ downloadTips }}</p>
            <el-button @click="downLoadData" class="exter-btn">{{ $t('buttons.download') }}</el-button>
        </div>
        <pure-table
            :height="tableSize === 'small' ? 352 : 465"
            :size="`default`"
            showOverflowTooltip
            ref="tableListRef"
            table-layout="fixed"
            :loading="loading"
            :data="dataList"
            :columns="columns"
            :pagination="pagination"
            @row-click="handleClick"
            @selection-change="handleSelectionChange"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        >
            <template #operation="{ row }">
                <i
                    class="reset-margin icon-data text-2xl btn-icon"
                    @click.native.stop="openDetail(row)"
                    v-if="row.abilityType === 2"
                />
            </template>
        </pure-table>
        <el-dialog
            v-model="formVisible"
            :title="$t('object.imgPreview')"
            :width="700"
            draggable
            :before-close="closeDialog"
            :append-to-body="true"
            :close-on-click-modal="false"
        >
            <div class="cropper-container preview-img boder-shadow p-5">
                <Cropper ref="refCropper" :src="imgUrl" :cropTitle="roiTitle" @isReady="initRoi" v-if="formVisible" />
            </div>
            <el-form
                ref="ruleFormRef"
                :model="formData"
                label-position="top"
                require-asterisk-position="right"
                class="boder-shadow flex flex-wrap justify-between mt-3 p-5"
                v-if="formData"
            >
                <el-form-item
                    v-for="(value, label) in formData"
                    :label="label"
                    :key="label"
                    :style="{ width: '300px' }"
                >
                    <el-input v-model="formData[label]" maxlength="32" />
                </el-form-item>
                <div class="text-right w-full">
                    <el-button class="exter-btn" @click="setRoiInfo">{{ $t('object.artificialRecognize') }}</el-button>
                </div>
            </el-form>
        </el-dialog>
    </div>
</template>
<style scoped lang="scss">
.preview-img {
    display: block;
    max-width: 660px;
}
.downlod-panel {
    display: flex;
    justify-content: space-between;
    align-items: center;
    top: 0;
    z-index: 9;
}
.search-bar {
    display: flex;
    padding: 20px;
    align-items: flex-end;
    .el-form-item {
        margin-bottom: 0;
        margin-right: 20px;
    }
    .inline-btn {
        color: #fff;
        border-radius: 4px;
        background: #209ff3;
    }
}
</style>
