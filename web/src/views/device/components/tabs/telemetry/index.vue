<script setup lang="ts">
import { ref, onMounted, computed, unref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { getLastTelemetry } from '@/api/device';
import { dateTimeFormat, getTableColums } from '@/utils/tools';
import { message, confirmBox } from '@/utils/message';
import { FormInstance } from 'element-plus';
import Cropper from '@/components/ReCropper';
import { useRouter } from 'vue-router';
import { addDeviceAbility, getAbilityByDeviceId, deleteDeviceAbility } from '@/api/device-ability';
import { getSensingObjectByAbilityId } from '@/api/sensing-object';
const router = useRouter();
const dataList = ref([]);
const loading = ref(true);
const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return {};
        },
    },
});
const entityId = computed(() => String(props.data?.id.id));
const columns: TableColumnList = getTableColums([
    {
        label: 'device.lastUpdateTime',
        prop: 'ts',
        formatter: dateTimeFormat,
    },
    {
        label: 'common.key',
        prop: 'key',
        formatter: row => {
            console.log(typeof row.key);
            return $t(row.key);
        },
    },
    {
        label: 'common.value',
        prop: 'value',
    },
    {
        label: '',
        fixed: 'right',
        width: 180,
        slot: 'operation',
    },
]);
const transTelemetryList = dataInfo => {
    const tmpList = [];
    for (const key in dataInfo) {
        const { 0: tmpObj } = dataInfo[key];
        tmpObj.key = key;
        if (key === 'image') {
            const imgData = tmpObj.value;
            Object.assign(tmpObj, {
                imgData,
                value: $t('device.fullImg'),
            });
        }
        tmpList.push(tmpObj);
    }
    return tmpList;
};
async function onSearch() {
    loading.value = true;
    const infos = await getLastTelemetry(unref(entityId));
    dataList.value = transTelemetryList(infos);
    setTimeout(() => {
        loading.value = false;
    }, 500);
}
watch(
    () => props.data,
    val => {
        onSearch();
    },
);
onMounted(() => {
    onSearch();
});
const refCropper = ref();
const formVisible = ref(false);
const imgUrl = ref<string>('');
const roiList = ref([]);
const onClear = () => {
    refCropper.value.cropper.clear();
};
const openDetail = async row => {
    imgUrl.value = row.imgData;
    const { data } = await getAbilityByDeviceId({
        deviceId: unref(entityId),
        abilityType: '2',
        sortProperty: 'createdTime',
    });
    const rows = data.filter(item => {
        return item.abilityType === 2;
    });
    roiList.value = rows;
    formVisible.value = true;
    if (refCropper.value?.cropper) initRoi();
};
const roiColumns: TableColumnList = getTableColums([
    {
        label: 'device.channelName',
        prop: 'ability',
        width: 140,
        formatter: row => {
            return row.ability === 'image' ? $t('device.fullImg') : row.ability;
        },
    },
    {
        label: 'common.coordinate',
        prop: 'extraInfo',
        width: 170,
        formatter: (row, col) => {
            const [x, y, width, height] = row.extraInfo?.split(',') || [];
            const posInfo = x ? `(${x},${y}) (${width},${height})` : '-';
            return posInfo;
        },
    },
    {
        label: 'device.attrRecogniz',
        width: 220,
        prop: 'attributes',
        formatter: (row, col) => {
            return row.attributes?.split(',').join(',') || '-';
        },
    },
    {
        label: '',
        fixed: 'right',
        width: 90,
        slot: 'operation',
    },
]);
const closeDialog = () => {
    formVisible.value = false;
};
const roiVisible = ref(false);
const roiFormRef = ref<FormInstance>();
const roiTableRef = ref();
const roiData = ref({ ability: '', attrName: '' });
const roiAttrs = ref([]);
let igNoreCheck = false;
let igNoreName = false;
let isRongName = false;
const rulesRoi = {
    ability: [
        {
            required: true,
            trigger: 'blur',
            validator: async (rule, value, callback) => {
                const textSearch = value.trim();
                const existRoi = roiList.value.filter(item => item.ability == textSearch);
                if (existRoi.length) callback($t('device.existRoi'));
                if (igNoreName) {
                    callback();
                } else {
                    if (textSearch) {
                        callback();
                    } else {
                        isRongName = true;
                        callback($t('common.requireFailed'));
                    }
                }
            },
        },
    ],
    attrName: [
        {
            required: true,
            trigger: 'blur',
            validator: async (rule, value, callback) => {
                const textSearch = value.trim();
                if (igNoreCheck) {
                    callback();
                } else {
                    if (textSearch) {
                        if (roiAttrs.value.includes(textSearch)) callback($t('device.attrExist'));
                        else callback();
                    } else {
                        callback($t('common.requireFailed'));
                    }
                }
            },
        },
    ],
};
const roiTitle = ref('');
let roiPosData = {};
const clearRoiInfo = () => {
    roiData.value = { ability: '', attrName: '' };
    roiAttrs.value = [];
    roiTitle.value = '';
    refCropper.value.cropper.enable();
    onClear();
};
const initRoi = () => {
    const rows = roiList.value;
    if (rows.length > 1) {
        openRoiDetail(rows[1]);
    }
};
const openRoiDetail = row => {
    if (row.extraInfo) {
        const { setCurrentRow } = roiTableRef.value.getTableRef();
        setCurrentRow(row);
        const [x, y, width, height] = row.extraInfo.split(',');
        refCropper.value.cropper.enable();
        refCropper.value.cropper.crop();
        refCropper.value.cropper.setData({
            width: Number(width),
            height: Number(height),
            x: Number(x),
            y: Number(y),
        });
        roiTitle.value = row.ability;
        roiData.value = row;
        roiAttrs.value = row.attributes.split(',');
        refCropper.value.cropper.disable();
    } else {
        clearRoiInfo();
    }
    //roiVisible.value = true;
};
const addRoi = () => {
    const infos = refCropper.value.cropper.getData();
    const { height, width } = infos;
    if (height * width) {
        roiAttrs.value = [];
        roiData.value = { attrName: '', ability: '' };
        roiPosData = infos;
        roiVisible.value = true;
    } else {
        message($t('device.pleaseDrawRoi'), { type: 'error' });
    }
};
const deleteRoi = async row => {
    if (row.sensingObjectId) {
        const objectInfo = await getSensingObjectByAbilityId(row.id);
        const title = $t('device.sureDelRoiWithObject').replace('%s', row.ability).replace('%s1', objectInfo.name);
        const isConfirm = await confirmBox($t('device.clearObjectBeforeDelRoi'), {
            title,
            cancelButtonText: $t('buttons.ok'),
            confirmButtonText: $t('device.jumpToObj'),
            confirmButtonClass: 'inter-btn',
        });
        if (isConfirm) {
            router.push('/objects');
        }
    } else {
        const title = $t('device.sureDelRoi').replace('%s', row.ability);
        const isConfirm = await confirmBox($t('device.delRoiTips'), {
            title,
        });
        if (isConfirm) {
            await deleteDeviceAbility(row.id);
            roiList.value = roiList.value.filter(item => item.id != row.id);
            if (roiList.value.length == 1) {
                clearRoiInfo();
            } else {
                const lastRow = roiList.value.length - 1;
                openRoiDetail(roiList.value[lastRow]);
            }
            message($t('status.success'));
        }
    }
};
const addAttr = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    igNoreName = !isRongName && true;
    igNoreCheck = false;
    const valid = await formEl.validate();
    if (valid) {
        const { attrName } = { ...roiData.value };
        roiAttrs.value.push(attrName);
        roiData.value.attrName = '';
        igNoreCheck = true;
    } else {
        igNoreCheck = false;
    }
    igNoreName = false;
};
const handleClose = tag => {
    roiAttrs.value = roiAttrs.value.filter(item => {
        return item != tag;
    });
};
const closeRoiDialog = () => {
    roiAttrs.value = [];
    roiFormRef.value.resetFields();
    roiVisible.value = false;
    const lastRow = Math.max(roiList.value.length - 1, 0);
    openRoiDetail(roiList.value[lastRow]);
};
const submitRoiForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    let valid = false;
    try {
        const attLength = roiAttrs.value.length;
        if (attLength > 3) {
            igNoreCheck = true;
        } else {
            const { attrName } = { ...roiData.value };
            igNoreCheck = attLength && attrName == '';
            if (!igNoreCheck) await addAttr(roiFormRef.value);
        }
        valid = await formEl.validate();
    } catch (e) {
        igNoreCheck = false;
    }
    if (valid && roiAttrs.value.length) {
        const { x, y, width, height } = roiPosData;
        const extraInfo = `${parseInt(x)},${parseInt(y)},${parseInt(width)},${parseInt(height)}`;
        const ability = roiData.value.ability;
        const attributes = roiAttrs.value.join(',');
        const rowData = {
            ability,
            extraInfo,
            attributes,
            abilityType: 2,
            deviceId: unref(entityId),
        };

        const newData = await addDeviceAbility(rowData);
        roiList.value.push(newData);
        message($t('status.success'), { type: 'success' });
        closeRoiDialog();
    }
};
</script>
<template>
    <div class="boder-shadow h-full">
        <p class="p-5">{{ $t('device.latestTelemetry') }}</p>
        <pure-table
            row-key="id"
            :loading="loading"
            height="600"
            maxHeight="100%"
            :data="dataList"
            :columns="columns"
            table-layout="fixed"
        >
            <template #operation="{ row }">
                <i
                    class="reset-margin icon-data text-2xl btn-icon"
                    @click="openDetail(row)"
                    v-if="row.key === 'image'"
                />
            </template>
        </pure-table>
        <el-dialog
            v-model="formVisible"
            :title="$t('device.imgRegAbility')"
            :width="700"
            :before-close="closeDialog"
            :append-to-body="true"
            :close-on-click-modal="false"
            class="top-dialog"
        >
            <div class="cropper-container preview-img boder-shadow p-5">
                <Cropper ref="refCropper" :src="imgUrl" :cropTitle="roiTitle" @isReady="initRoi" v-if="formVisible" />
                <div class="mt-3 flex justify-between items-center">
                    <p class="text-sm" v-if="roiList.length < 2">
                        {{ $t('device.setImgRoi') }}
                    </p>
                    <div v-if="roiList.length > 1" class="tag-tip">
                        <el-tag v-for="tag in roiAttrs" :key="tag" class="mx-2" type="info">
                            {{ tag }}
                        </el-tag>
                    </div>
                    <div v-show="roiTitle === ''">
                        <el-button @click="addRoi" class="inter-btn" :disabled="roiList.length > 4">{{
                            $t('buttons.add')
                        }}</el-button>
                        <el-button @click="onClear" class="inter-btn">{{ $t('buttons.clear') }}</el-button>
                    </div>
                    <el-button
                        @click="clearRoiInfo"
                        class="inter-btn"
                        :disabled="roiList.length > 4"
                        v-show="roiTitle !== ''"
                        >{{ $t('device.setNewRoi') }}</el-button
                    >
                </div>
            </div>
            <div class="boder-shadow flex flex-wrap justify-between mt-3 p-5">
                <pure-table
                    showOverflowTooltip
                    ref="roiTableRef"
                    row-key="ability"
                    :loading="loading"
                    height="200"
                    maxHeight="100%"
                    :data="roiList"
                    :columns="roiColumns"
                    @row-click="openRoiDetail"
                    :highlight-current-row="true"
                    class="small-table"
                >
                    <template #operation="{ row }">
                        <i
                            class="reset-margin icon-data text-2xl btn-icon"
                            @click="openRoiDetail(row)"
                            v-if="row.ability != 'image'"
                        />
                        <i
                            class="reset-margin icon-delete text-2xl btn-icon"
                            @click.native.stop="deleteRoi(row)"
                            v-if="row.ability != 'image'"
                        />
                    </template>
                </pure-table>
            </div>
        </el-dialog>
        <el-dialog
            v-model="roiVisible"
            :title="$t('device.addRoi')"
            :width="490"
            draggable
            :before-close="closeRoiDialog"
            :append-to-body="true"
            :close-on-click-modal="false"
        >
            <el-form
                ref="roiFormRef"
                :model="roiData"
                :rules="rulesRoi"
                label-position="top"
                require-asterisk-position="right"
            >
                <el-form-item :label="$t('common.name')" prop="ability">
                    <el-input
                        v-model="roiData.ability"
                        :placeholder="$t('device.pleaseEnterChannelName')"
                        maxlength="16"
                    />
                </el-form-item>
                <el-form-item :label="$t('device.attrRecogniz')" prop="attrName">
                    <div class="flex justify-between w-full">
                        <el-input
                            v-model="roiData.attrName"
                            :placeholder="$t('device.pleaseEnterAttrs')"
                            maxlength="16"
                            :style="{ width: '370px' }"
                        />
                        <el-button
                            class="w-16 inter-btn"
                            @click="addAttr(roiFormRef)"
                            :disabled="roiAttrs.length > 3"
                            >{{ $t('buttons.add') }}</el-button
                        >
                    </div>
                </el-form-item>
                <div class="w-full">
                    <el-tag
                        v-for="tag in roiAttrs"
                        :key="tag"
                        class="mx-2"
                        closable
                        type="info"
                        @close="handleClose(tag)"
                    >
                        {{ tag }}
                    </el-tag>
                </div>
            </el-form>
            <template #footer>
                <el-button @click="closeRoiDialog" class="ms-cancel-btn">{{ $t('buttons.cancel') }}</el-button>
                <el-button type="primary" @click="submitRoiForm(roiFormRef)" class="exter-btn">
                    {{ $t('buttons.save') }}
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>
<style scoped lang="scss">
.preview-img {
    display: block;
    max-width: 660px;
}
.el-tag {
    background-color: #fff;
    margin: 0 5px 5px 0;
}
.tag-tip {
    .el-tag {
        background-color: #f0f2f5;
        border: none;
    }
}
</style>
