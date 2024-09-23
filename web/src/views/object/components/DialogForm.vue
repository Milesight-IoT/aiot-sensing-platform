<script setup lang="ts">
import { ref, watch, unref, nextTick } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance, CascaderProps, Cascader } from 'element-plus';
import { getUserDeviceList } from '@/api/device';
import { getAbilityByDeviceId } from '@/api/device-ability';
import { addSensingObject, getSensingObjectByName } from '@/api/sensing-object';
import { mergedArrayByKey } from '@/utils/tools';

const props = defineProps({
    visible: {
        type: Boolean,
        default: false,
    },
    data: {
        type: Object,
        default: () => {
            return {};
        },
    },
});
const ruleFormRef = ref<FormInstance>();
const formVisible = ref(false);
const formData = ref({ ...props.data });
const emit = defineEmits(['update:visible', 'updateTable']);
watch(
    () => formVisible.value,
    val => {
        emit('update:visible', val);
    },
);

watch(
    () => props.visible,
    val => {
        formVisible.value = val;
        //getCasOpts();
    },
);

watch(
    () => props.data,
    val => {
        formData.value = val;
    },
);

const searchObject = async textSearch => {
    textSearch = textSearch.trim();
    if (textSearch) {
        const { name } = await getSensingObjectByName(textSearch);
        return !name;
    } else {
        return true;
    }
};
let isFirstSearch = true;
async function searchDeviceList(isNode, textSearch?) {
    const queryParams = {
        textSearch,
        pageSize: 100,
        page: 0,
        sortProperty: 'createdTime',
        sortOrder: 'DESC',
    };
    let tmpData = [];
    const { data } = await getUserDeviceList(queryParams);
    tmpData = data;
    isFirstSearch = false;
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
const rules = {
    name: [
        {
            required: true,
            message: () => $t('common.requireFailed'),
            trigger: 'blur',
        },
        {
            validator: async (rule, value, callback) => {
                const isValid = await searchObject(value);
                if (isValid) {
                    callback();
                } else {
                    callback($t('object.existSensingObject'));
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
const casOptions = ref([]);
const getAbility = async node => {
    const id = node.value;
    if (!id) return;
    const queryParams = {
        deviceId: id,
        pageSize: 100,
        page: 0,
        sortProperty: 'abilityType',
        sortOrder: 'desc',
    };
    const { data } = await getAbilityByDeviceId(queryParams);
    const nodes = data.map(item => {
        const label = item.ability === 'image' ? $t('device.fullImg') : item.ability;
        const disabled = item.sensingObjectId !== null;
        return { label, value: item.id, disabled, leaf: true };
    });
    const index = deviceNodes.findIndex(item => item.value == id);
    if (index > -1) deviceNodes[index].children = nodes;
    return nodes;
};
const casProps: CascaderProps = {
    multiple: true,
    lazy: true,
    lazyLoad: async (node, resolve) => {
        const { level } = node;
        let nodes = [];
        if (level == 0) {
            if (isFirstSearch) {
                nodes = await searchDeviceList(true);
                casOptions.value = nodes;
                deviceNodes = nodes;
            }
        } else {
            nodes = await getAbility(node);
        }
        resolve(nodes);
    },
};
const showPopper = ref(false);
const casRef = ref<Cascader>();
const casOpts = ref([]);
const filterChannels = ref([]);
const casPanelProps: CascaderProps = {
    multiple: true,
    lazy: true,
    lazyLoad: async (node, resolve) => {
        const { level } = node;
        let nodes = [];
        if (level == 0) {
            nodes = deviceNodes;
        } else {
            nodes = await getAbility(node);
        }
        resolve(nodes);
    },
};
const hasMatchData = ref(false);
const filterMethod = async textSearch => {
    // casOpts.value = deviceNodes.filter(item => {
    //     return item.label?.indexOf(textSearch) != -1;
    // });
    casOpts.value = await searchDeviceList(true, textSearch);
    hasMatchData.value = casOpts.value.length > 0;
    filterChannels.value = unref(formData.value.channels);
    valueBeforeChannels = unref(formData.value.channels);
    return true;
};
const focusPanel = event => {
    event.preventDefault();
};
let valueBeforeChannels = [];
const filteChange = value => {
    casOptions.value = mergedArrayByKey(casOptions.value, casOpts.value, 'value');
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
    console.log(node);
    console.log(keyword);
    return false;
};
const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async valid => {
        if (valid) {
            formVisible.value = false;
            const params = { ...formData.value, name: formData.value.name.trim() };
            const valueDif = new Set(params.channels?.map(item => item.join(':')));
            const channelsDif = Array.from(valueDif);
            const channels = channelsDif.map(item => item.split(':'));
            params.deviceAbilityIds = channels.map(item => item[1]).join(',');
            params.channels = undefined;
            await addSensingObject(params);
            emit('updateTable');
            message($t('status.success'), { type: 'success' });
            resetForm(formEl);
        }
    });
};

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    formEl.resetFields();
};

const closeDialog = () => {
    formVisible.value = false;
    showPopper.value = false;
    resetForm(ruleFormRef.value);
};
</script>

<template>
    <el-dialog
        v-model="formVisible"
        :title="$t('object.addNewObject')"
        :width="590"
        draggable
        :before-close="closeDialog"
        :close-on-click-modal="false"
    >
        <!-- 表单内容 -->
        <el-form
            ref="ruleFormRef"
            :model="formData"
            :rules="rules"
            label-position="top"
            require-asterisk-position="right"
        >
            <el-form-item :label="$t('common.name')" prop="name">
                <el-input
                    v-model="formData.name"
                    :style="{ width: '550px' }"
                    :placeholder="$t('object.pleaseEnterObjectName')"
                    maxlength="32"
                    clearable
                />
            </el-form-item>
            <el-form-item :label="$t('object.sensingChannels')" prop="channels">
                <el-cascader
                    ref="casRef"
                    v-model="formData.channels"
                    :options="casOptions"
                    :props="casProps"
                    popper-class="cas-popper"
                    clearable
                    filterable
                    :filter-method="handleFilterMethod"
                    :before-filter="filterMethod"
                    @change="casChange"
                    :style="{ width: '550px' }"
                    :placeholder="$t('object.pleaseSelectChannels')"
                >
                    <template #empty>
                        <el-cascader-panel
                            :options="casOpts"
                            :props="casPanelProps"
                            v-model="filterChannels"
                            @change="filteChange"
                            @focus="focusPanel"
                            v-show="hasMatchData"
                        />
                        <div v-show="!hasMatchData" class="no-data">
                            {{ $t('noMatchData') }}
                        </div>
                    </template>
                </el-cascader>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="closeDialog" class="ms-cancel-btn">{{ $t('buttons.cancel') }}</el-button>
            <el-button type="primary" @click="submitForm(ruleFormRef)" class="exter-btn">
                {{ $t('buttons.save') }}
            </el-button>
        </template>
    </el-dialog>
</template>
<style lang="scss" scoped>
.cas-item {
    position: relative;
    top: -20px;
}
</style>
