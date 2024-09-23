<script setup lang="ts">
import { ref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance } from 'element-plus';
import { getDeviceProfileList } from '@/api/device';
const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return {};
        },
    },
});

const ruleFormRef = ref<FormInstance>();

const formData = ref(props.data);
const deviceProfileList = ref([]);
async function searchDeviceType() {
    const queryParams = {
        pageSize: 10,
        page: 0,
        sortProperty: 'name',
        sortOrder: 'asc',
    };
    const { data, totalElements, hasNext } = await getDeviceProfileList(queryParams);
    deviceProfileList.value = data;
}
const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(valid => {
        if (valid) {
            message($t('status.success'), { type: 'success' });
            formVisible.value = false;
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
    resetForm(ruleFormRef.value);
};

watch(
    () => props.data,
    val => {
        formData.value = val;
    },
);

const rules = {
    name: [
        {
            required: true,
            message: $t('common.requireFailed'),
            trigger: 'blur',
        },
    ],
    type: [
        {
            required: true,
            message: $t('common.requireFailed'),
            trigger: 'blur',
        },
    ],
    credentialsId: [
        {
            required: true,
            message: $t('common.requireFailed'),
            trigger: 'blur',
        },
    ],
};
</script>

<template>
    <el-form ref="ruleFormRef" :model="formData" :rules="rules" label-position="top" require-asterisk-position="right">
        <el-form-item :label="$t('common.name')" prop="name">
            <el-input v-model="formData.name" :placeholder="t('device.pleaseEnterDeviceName')" maxlength="32" />
        </el-form-item>
        <el-form-item :label="$t('device.model')" prop="type">
            <el-select
                v-model="formData.model"
                value-key=""
                clearable
                filterable
                :placeholder="$t('device.pleaseSelectModel')"
                class="w-full"
            >
                <el-option v-for="item in deviceProfileList" :key="item.value" :label="item.name" :value="item.id" />
            </el-select>
        </el-form-item>
        <el-form-item :label="$t('device.sn')" prop="credentialsId">
            <el-input v-model="formData.credentialsId" :placeholder="t('device.pleaseEnterSN')" maxlength="16" />
        </el-form-item>
    </el-form>
    <el-button type="primary" @click="submitForm(ruleFormRef)" class="float-right">
        {{ t('buttons.save') }}
    </el-button>
</template>
