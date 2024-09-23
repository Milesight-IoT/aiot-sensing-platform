<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { $t } from '@/plugins/i18n';
import { message, confirmBox } from '@/utils/message';
import { FormInstance } from 'element-plus';
import { emitter } from '@/utils/mitt';
import { checkSN, getDeviceProfileList, addDevice, getDeviceByName, getDeviceBycredentialsId } from '@/api/device';
const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return {
                type: '',
            };
        },
    },
});
const ruleFormRef = ref<FormInstance>();
const formData = ref({ ...props.data });
const deviceProfileList = ref([]);
const replace = ref(false);
const initSN = props.data.credentialsId;
async function searchDeviceType() {
    if (deviceProfileList.value.length) return;
    const queryParams = {
        pageSize: 10,
        page: 0,
        sortProperty: 'name',
        sortOrder: 'asc',
    };
    const { data, totalElements, hasNext } = await getDeviceProfileList(queryParams);
    deviceProfileList.value = data;
}
const changeReplace = async val => {
    if (val) {
        const isConfirm = await confirmBox($t('device.pleaseEnterReplaceSN'), {
            title: $t('device.sureReplace'),
        });
        replace.value = isConfirm;
    } else {
        ruleFormRef.value.clearValidate('credentialsId');
        formData.value.credentialsId = props.data.credentialsId;
        console.log('canvelcheck', props.data.credentialsId);
    }
};
const getModelName = vId => {
    const obj = deviceProfileList.value.find(item => {
        return item.id?.id === vId.id;
    });
    return obj?.name;
};
const emit = defineEmits(['updateForm']);
const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    console.log(formData.value);
    const res = await formEl.validate();
    if (res) {
        try {
            const params = { ...formData.value, credentialsId: formData.value.credentialsId.trim() };
            await addDevice(params, params.credentialsId?.toUpperCase());
            const type = getModelName(params.deviceProfileId);
            params.type = type;
            emitter.emit('panelTitle', params.name);
            emit('updateForm', params);
            message($t('status.success'));
        } catch (e) {}
    }
    console.log(res);
};
const rules = {
    name: [
        {
            required: true,
            trigger: 'blur',
            validator: async (rule, value, callback) => {
                const textSearch = value.trim();
                if (textSearch) {
                    if (textSearch == props.data.name) callback();
                    const { name } = await getDeviceByName(textSearch);
                    if (!name) {
                        callback();
                    } else {
                        callback($t('device.existDevice'));
                    }
                } else {
                    callback($t('common.requireFailed'));
                }
            },
        },
    ],
    deviceProfileId: [
        {
            required: true,
            message: $t('common.requireFailed'),
            trigger: 'blur',
        },
    ],
    credentialsId: [
        {
            required: true,
            trigger: 'blur',
            validator: async (rule, value, callback) => {
                const textSearch = value.trim().toUpperCase();
                if (textSearch) {
                    if (textSearch == props.data.credentialsId) callback();
                    let isValid = true;
                    if (textSearch.length != 12) {
                        isValid = false;
                    } else {
                        isValid = await checkSN(textSearch);
                    }

                    !isValid && callback($t('device.invalidSN'));
                    const { name } = await getDeviceBycredentialsId(textSearch);
                    if (!name) {
                        callback();
                    } else {
                        callback($t('device.existDevice'));
                    }
                } else {
                    callback($t('device.pleaseEnterDeviceName'));
                }
            },
        },
    ],
};

onMounted(() => {
    searchDeviceType();
});

watch(
    () => props.data,
    val => {
        formData.value = { ...val };
        replace.value = false;
        ruleFormRef.value?.resetFields();
    },
);
</script>

<template>
    <el-form ref="ruleFormRef" :model="formData" :rules="rules" label-position="top" require-asterisk-position="right">
        <el-form-item :label="$t('common.name')" prop="name">
            <el-input
                v-model="formData.name"
                :placeholder="$t('device.pleaseEnterDeviceName')"
                maxlength="32"
                clearable
            />
        </el-form-item>
        <el-form-item :label="$t('device.model')" prop="deviceProfileId">
            <el-select
                v-model="formData.deviceProfileId"
                value-key="id"
                clearable
                filterable
                :placeholder="$t('device.pleaseSelectModel')"
                @click="searchDeviceType"
                disabled
            >
                <el-option v-for="item in deviceProfileList" :key="item.value" :label="item.name" :value="item.id" />
            </el-select>
        </el-form-item>
        <el-form-item>
            <el-checkbox :label="$t('device.replace')" v-model="replace" @change="changeReplace" />
        </el-form-item>
        <el-form-item :label="$t('device.sn')" prop="credentialsId">
            <el-input
                v-model="formData.credentialsId"
                :placeholder="$t('device.pleaseEnterSN')"
                maxlength="16"
                clearable
                :disabled="!replace"
            />
        </el-form-item>
    </el-form>
    <el-button type="primary" @click="submitForm(ruleFormRef)" class="float-right exter-btn">
        {{ $t('buttons.save') }}
    </el-button>
</template>
