<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { $t } from '@/plugins/i18n';
import { FormInstance, Cascader } from 'element-plus';
import { getObejctImageChannel, getSensingObjectById } from '@/api/sensing-object';
import { emitter } from '@/utils/mitt';
const props = defineProps({
    /** 父组件传入当前仪表盘对象 */
    data: {
        type: Object,
        default: () => {
            return {
                i: '', // 编辑时应有ID信息，若id为空则新增
                sensingChannel: [], // 当类型为1时，存在感知通道信息
                type: 'Snapshot preview',
            };
        },
    },
});

const formRef = ref<FormInstance>();
const formData = ref(props.data);
const rules = {
    sensingChannel: {
        required: true,
        trigger: 'blur',
        message: $t('common.requiredField'),
    },
};

watch(
    () => props.data,
    val => {
        formData.value = val;
        showImageList();
    },
);
onMounted(() => {
    showImageList();
});

const casRef = ref<Cascader>();
const casOptions = ref([]);

const hasMatchData = ref(false);

const handleFilterMethod = (node, keyword) => {
    return false;
};
// let deviceNodes = [];
// const getAbility = async node => {
//     const id = node.value;
//     if (!id) return;
//     const queryParams = {
//         deviceId: id,
//         pageSize: 100,
//         page: 0,
//         sortProperty: 'abilityType',
//         sortOrder: 'desc',
//     };
//     const { data } = await getAbilityByDeviceId(queryParams);
//     const nodes = data.map(item => {
//         const label = item.ability === 'image' ? $t('device.fullImg') : item.ability;
//         const disabled = item.sensingObjectId !== null;
//         return { label, value: item.id, disabled, leaf: true };
//     });
//     const index = deviceNodes.findIndex(item => item.value == id);
//     if (index > -1) deviceNodes[index].children = nodes;
//     return nodes;
// };
let tmpData = [];
let selectName = [];
async function showImageList() {
    if (formData.value.sensingObjectImage) {
        const item = formData.value.sensingObjectImage; //await getSensingObjectById(params);
        totalCounts = 0;
        // tmpData = tmpData.concat(tmpImgData);
        let children = item.sensingChannels;
        children = children.map(channel => {
            return {
                ...channel,
                // TODO deviceName?
                label: channel.ability,
                value: `${channel.deviceId},${channel.deviceAbilityId}`,
            };
        });
        selectName.push(item.name);
        const itemSel = {
            ...item,
            label: item.name,
            value: item.id,
            children,
        };
        if (!hasTotal) tmpData = [itemSel];
        casOptions.value = [...tmpData];
        formData.value.sensingChannel = [item.id, item.deviceAbilityIds];
    } else {
        hasTotal = false;
        hasNextPage = true;
        getTotalImgList();
    }
}
let totalCounts = 0;
let currentPage = 0;
let hasNextPage = true;
let hasTotal = false;
async function getTotalImgList() {
    if (hasTotal) return;
    const queryParams = {
        pageSize: 200,
        page: 0,
    };
    tmpData = [];
    while (hasNextPage) {
        const { data, hasNext } = await getObejctImageChannel(queryParams);
        queryParams.page += 1;
        tmpData = tmpData.concat(data);
        hasNextPage = hasNext;
    }
    const newData = tmpData.map((item, index) => {
        let children = item.sensingChannels;
        children = children.map(channel => {
            return {
                ...channel,
                // TODO deviceName?
                label: channel.ability,
                value: `${channel.deviceId},${channel.deviceAbilityId}`,
            };
        });
        return {
            ...item,
            label: item.name,
            value: item.id,
            children,
        };
    });
    hasTotal = true;
    tmpData = [...newData];
    casOptions.value = [...newData];
}
async function searchImageList(
    queryParams = {
        pageSize: 200,
        page: 0,
    },
) {
    const { data, totalElements, hasNext } = await getObejctImageChannel(queryParams);
    currentPage += 1;
    totalCounts = totalElements;
    hasNextPage = hasNext;
    let delFlag = {
        flag: false,
        index: 0,
    };

    const newData = data.map((item, index) => {
        let children = item.sensingChannels;
        children = children.map(channel => {
            return {
                ...channel,
                // TODO deviceName?
                label: channel.ability,
                value: channel,
            };
        });
        delete item.sensingChannels;
        if (selectName.includes(item.name)) {
            delFlag = {
                flag: true,
                index,
            };
        }

        return {
            ...item,
            label: item.name,
            value: item,
            children,
        };
    });
    if (delFlag.flag) {
        newData.splice(delFlag.index, 1);
    }
    tmpData = tmpData.concat([...newData]);
    casOptions.value = [...tmpData];
}
const filterMethod = async textSearch => {
    if (hasTotal) {
        return true;
    }
    const queryParams = {
        pageSize: 50,
        page: 0,
        textSearch,
    };
    await searchImageList(queryParams);
    return true;
};

const openOpts = val => {
    if (val && !totalCounts) getTotalImgList();
};
const submitForm = async () => {
    const valid = await formRef.value.validate();
    if (valid) {
        const res = {
            type: 'Snapshot preview',
            sensingObjectImage: {
                ...formData.value.sensingChannel[0],
                // 感知通道以及图片
                sensingChannels: [formData.value.sensingChannel[1]],
            },
        };
        const [objectId, deviceAbilityId] = formData.value.sensingChannel;
        const selObject = tmpData.find(item => item.id == objectId);
        if (formData.value.i) {
            formData.value.sensingObjectImage = {
                ...selObject,
                deviceAbilityIds: deviceAbilityId,
            };
        } else {
            formData.value.type = 'Snapshot preview';
            formData.value.sensingObjectImage = {
                ...selObject,
                deviceAbilityIds: deviceAbilityId,
            };
        }
        formData.value.data = {
            name: formData.value.sensingObjectImage.name,
        };
        emitter.emit('showPanel', false);

        return formData.value;
    } else {
        return false;
    }
};

const resetForm = () => {
    formRef.value.resetFields();
};
defineExpose({
    submitForm,
    resetForm,
});
</script>

<template>
    <!-- 表单内容 -->
    <el-form ref="formRef" :model="formData" :rules="rules" label-position="top" require-asterisk-position="right">
        <el-form-item :label="$t('dashboard.imageSensingChannel')" prop="sensingChannel">
            <el-cascader
                ref="casRef"
                v-model="formData.sensingChannel"
                :options="casOptions"
                filterable
                :before-filter="filterMethod"
                popper-class="cas-popper"
                class="w-full"
                @visible-change="openOpts"
                :placeholder="$t('dashboard.pleaseEnterImageSenseChannel')"
            >
                <template #empty>
                    <!-- <el-cascader-panel
                            :options="casOpts"
                            :props="casPanelProps"
                            v-model="filterChannels"
                            @change="filteChange"
                            @focus="focusPanel"
                            v-show="hasMatchData"
                        /> -->
                    <div v-show="!hasMatchData" class="no-data">
                        {{ $t('noMatchData') }}
                    </div>
                </template>
            </el-cascader>
        </el-form-item>
    </el-form>
</template>

<style scoped>
:deep(.el-cascader-node[aria-expanded='false']) {
    display: none !important;
}
</style>
