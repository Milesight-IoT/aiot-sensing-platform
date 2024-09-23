<script setup lang="ts">
import { ref, watch, onMounted, unref, nextTick } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance, CascaderProps } from 'element-plus';
import { getUserDeviceList, getDeviceListById } from '@/api/device';
import { getAbilityByDeviceId } from '@/api/device-ability';
import { addSensingObject, getSensingObjectByName } from '@/api/sensing-object';
import { emitter } from '@/utils/mitt';
import { mergedArrayByKey } from '@/utils/tools';
const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return {};
        },
    },
});
const ruleFormRef = ref<FormInstance>();
const formData = ref({ ...props.data });
const getDevicesByIds = async deviceIds => {
    const params = {
        deviceIds,
    };
    const tmpData = await getDeviceListById(params);
    return tmpData.map(item => {
        return {
            label: item.name,
            value: item.id?.id,
        };
    });
};
async function searchDeviceList(isNode, textSearch?) {
    const queryParams = {
        textSearch,
        pageSize: 100,
        page: 0,
        sortProperty: 'createdTime',
        sortOrder: 'DESC',
    };
    const { data } = await getUserDeviceList(queryParams);
    let tmpData = data || [];
    if (isNode)
        return tmpData.map(item => {
            return {
                label: item.name,
                value: item.id?.id,
            };
        });
    else {
        return tmpData;
    }
}
const emit = defineEmits(['updateForm']);
const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    try {
        const valid = await formEl.validate();
        if (valid) {
            const params = { ...formData.value, name: formData.value.name.trim() };
            const valueDif = new Set(params.channels?.map(item => item.join(':')));
            const channelsDif = Array.from(valueDif);
            const channels = channelsDif.map(item => item.split(':'));
            const deviceAbilityIds = channels.map(item => item[1]).join(',');
            params.deviceAbilityIds = deviceAbilityIds;
            params.channels = undefined;
            params.sensingChannels = undefined;
            const objInfo = await addSensingObject(params);
            params.sensingChannels = objInfo.sensingChannels;
            emitter.emit('panelTitle', params.name);
            objInfo.channels = channels;
            emit('updateForm', objInfo);
            message($t('status.success'), { type: 'success' });
        }
    } catch (e) {}
};

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    formEl.resetFields();
};

watch(
    () => props.data,
    val => {
        formData.value = { ...val };
        if (val.channels) {
            valueBeforeChannels = [...val.channels];
            getCasOpts();
        }
        resetForm(ruleFormRef.value);
    },
);
onMounted(() => {
    getCasOpts();
});
const rules = {
    name: [
        {
            required: true,
            trigger: 'blur',
            validator: async (rule, value, callback) => {
                const textSearch = value.trim();
                if (textSearch) {
                    if (textSearch == props.data.name) callback();
                    const { name } = await getSensingObjectByName(textSearch);
                    if (!name) {
                        callback();
                    } else {
                        callback($t('object.existSensingObject'));
                    }
                } else {
                    callback($t('common.requireFailed'));
                }
            },
        },
    ],
    channels: [
        {
            validator: async (rule, value, callback) => {
                const valueDif = new Set(value.map(item => item.join(':')));
                const isInvalid = valueDif.size > 8;
                if (isInvalid) {
                    callback($t('object.pleaseSelectChannels'));
                } else {
                    callback();
                }
            },
            trigger: 'change',
        },
    ],
};
let deviceNodes = [];
const getAbility = async (id, noProgress = true) => {
    const queryParams = {
        deviceId: id,
        pageSize: 100,
        page: 0,
        sortProperty: 'abilityType',
        sortOrder: 'desc',
    };
    let tmpData = [];
    let hasNextPage = true;
    while (hasNextPage) {
        const { data, hasNext } = await getAbilityByDeviceId(queryParams, noProgress);
        queryParams.page += 1;
        tmpData = tmpData.concat(data);
        hasNextPage = hasNext;
    }
    return tmpData.map(item => {
        const label = item.ability === 'image' ? $t('device.fullImg') : item.ability;
        const disabled = item.sensingObjectId !== null && item.sensingObjectId != props.data?.id;
        return { label, value: item.id, disabled, leaf: true };
    });
};
const getAbilitys = async node => {
    const id = node.value;
    if (!id) return;
    const queryParams = {
        deviceId: id,
        pageSize: 100,
        page: 0,
        sortProperty: 'abilityType',
        sortOrder: 'desc',
    };
    const { data } = await getAbilityByDeviceId(queryParams, true);
    const nodes = data.map(item => {
        const label = item.ability === 'image' ? $t('device.fullImg') : item.ability;
        const disabled = item.sensingObjectId !== null && item.sensingObjectId != props.data?.id;
        return { label, value: item.id, disabled, leaf: true };
    });
    const index = deviceNodes.findIndex(item => item.value == id);
    if (index > -1) deviceNodes[index].children = nodes;
    return nodes;
};
const getCasOpts = async () => {
    deviceNodes = await searchDeviceList(true);
    const channels = formData.value.channels;
    let noProgress = false;
    const deviceIds = channels.map(item => item[0]).join(',');
    let selDevices = [];
    console.log(deviceIds);
    if (deviceIds) selDevices = await getDevicesByIds(deviceIds);
    deviceNodes = mergedArrayByKey(deviceNodes, selDevices, 'value');
    for (let i = 0; i < selDevices.length; i++) {
        casProps.value.lazy = false;
        const id = selDevices[i]?.value;
        const index = deviceNodes.findIndex(item => item.value == id);
        if (index >= 0) {
            const item = deviceNodes[index];
            if (!item.children) {
                const children = await getAbility(id, noProgress);
                console.log(children);
                noProgress = true;
                deviceNodes[index] = {
                    label: item.label,
                    value: item.value,
                    children,
                };
            }
        }
    }
    console.log(deviceNodes);
    casOpts.value = deviceNodes;
    casProps.value.lazy = true;
};
const casProps: CascaderProps = ref({
    multiple: true,
    lazy: true,
    lazyLoad: async (node, resolve) => {
        const { level } = node;
        let nodes = [];
        if (level == 0) {
            nodes = deviceNodes;
        } else {
            const id = node.value;
            const queryParams = {
                deviceId: id,
                pageSize: 900,
                page: 0,
                sortProperty: 'abilityType',
                sortOrder: 'desc',
            };
            const { data } = await getAbilityByDeviceId(queryParams, true);
            nodes = data.map(item => {
                const label = item.ability === 'image' ? $t('device.fullImg') : item.ability;
                const disabled = item.sensingObjectId !== null && item.sensingObjectId != props.data?.id;
                return { label, value: item.id, disabled, leaf: true };
            });
        }
        resolve(nodes);
    },
});
const casRef = ref<Cascader>();
const casOpts = ref([]);
const filterChannels = ref([]);
const casPanelOpts = ref([]);
const casPanelProps: CascaderProps = {
    multiple: true,
    lazy: true,
    lazyLoad: async (node, resolve) => {
        const { level } = node;
        let nodes = [];
        if (level == 0) {
            nodes = deviceNodes;
        } else {
            nodes = await getAbilitys(node);
        }
        resolve(nodes);
    },
};
let valueBeforeChannels = [];
const hasMatchData = ref(false);
const filterMethod = async textSearch => {
    // casPanelOpts.value = deviceNodes.filter(item => {
    //     return item.label?.indexOf(textSearch) != -1;
    // });
    casPanelOpts.value = await searchDeviceList(true, textSearch);
    hasMatchData.value = casPanelOpts.value.length > 0;
    valueBeforeChannels = unref(formData.value.channels);
    filterChannels.value = unref(formData.value.channels);
    return true;
};
const filteChange = value => {
    casOpts.value = mergedArrayByKey(casPanelOpts.value, casOpts.value, 'value');
    nextTick(() => {
        formData.value.channels = [...valueBeforeChannels, ...value];
    });
};
const casChange = value => {
    nextTick(() => {
        filterChannels.value = unref(formData.value.channels);
    });
};
const handleFilterMethod = (node, keyword) => {
    return false;
};
</script>

<template>
    <el-form ref="ruleFormRef" :model="formData" :rules="rules" label-position="top" require-asterisk-position="right">
        <el-form-item :label="$t('common.name')" prop="name">
            <el-input
                clearable
                v-model="formData.name"
                :placeholder="$t('object.pleaseEnterObjectName')"
                maxlength="32"
            />
        </el-form-item>
        <el-form-item :label="$t('object.sensingChannels')" prop="channels">
            <el-cascader
                ref="casRef"
                :options="casOpts"
                :props="casProps"
                v-model="formData.channels"
                clearable
                filterable
                :filter-method="handleFilterMethod"
                :before-filter="filterMethod"
                @change="casChange"
                popper-class="cas-popper"
                :placeholder="$t('object.pleaseSelectChannels')"
            >
                <template #empty>
                    <el-cascader-panel
                        :options="casPanelOpts"
                        :props="casPanelProps"
                        v-model="filterChannels"
                        @change="filteChange"
                        v-show="hasMatchData"
                    />
                    <div v-show="!hasMatchData" class="no-data">
                        {{ $t('noMatchData') }}
                    </div>
                </template>
            </el-cascader>
        </el-form-item>
    </el-form>
    <el-button type="primary" @click="submitForm(ruleFormRef)" class="float-right exter-btn">
        {{ $t('buttons.save') }}
    </el-button>
</template>
