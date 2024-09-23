<script setup lang="ts">
import { ref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance } from 'element-plus';
import { checkSN, getDeviceProfileList, addDevice, getDeviceByName, getDeviceBycredentialsId } from '@/api/device';
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
const formData = ref(props.data);
const deviceProfileList = ref([]);
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
    },
);

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
            trigger: 'blur',
            validator: async (rule, value, callback) => {
                const textSearch = value.trim();
                if (textSearch) {
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
                    callback($t('common.requireFailed'));
                }
            },
        },
    ],
};
async function searchDeviceType() {
    if (deviceProfileList.value.length) return;
    const queryParams = {
        pageSize: 10,
        page: 0,
        sortProperty: 'name',
        sortOrder: 'asc',
    };
    const { data } = await getDeviceProfileList(queryParams);
    deviceProfileList.value = data;
}
const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async valid => {
        if (valid) {
            formVisible.value = false;
            const params = { ...formData.value, type: undefined, credentialsId: formData.value.credentialsId.trim() };
            await addDevice(params, params.credentialsId?.toUpperCase());
            emit('updateTable');
            resetForm(formEl);
            message($t('status.success'));
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
</script>

<template>
    <el-dialog
        v-model="formVisible"
        :title="$t('device.addNewDevice')"
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
                    clearable
                    :placeholder="$t('device.pleaseEnterDeviceName')"
                    maxlength="32"
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
                >
                    <el-option
                        v-for="item in deviceProfileList"
                        :key="item.value"
                        :label="item.name"
                        :value="item.id"
                    />
                </el-select>
            </el-form-item>
            <el-form-item :label="$t('device.sn')" prop="credentialsId">
                <el-input
                    clearable
                    v-model="formData.credentialsId"
                    :placeholder="$t('device.pleaseEnterSN')"
                    maxlength="16"
                />
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
