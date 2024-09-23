<!--
 * @Author: sreindy8224 sreindy@milesight.com
 * @Date: 2023-04-24 10:07:42
 * @LastEditTime: 2023-07-13 16:36:04
 * @Descripttion: 添加仪表盘表单
-->
<script setup lang="ts">
import { ref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance } from 'element-plus';
import { checkDashboardByName } from '@/api/dashboard';
import { updateDashboard } from '@/api/dashboard';
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

const emit = defineEmits(['update:visible', 'updateTable']);
const ruleFormRef = ref<FormInstance>();
const formVisible = ref(false);
const formData = ref(props.data);
const rules = {
    name: [
        {
            required: true,
            trigger: 'blur',
            validator: async (rule, value, callback) => {
                const textSearch = value.trim();
                if (textSearch) {
                    // 名字不存在时返回值false
                    const isExist = await checkDashboardByName(textSearch);
                    if (!isExist) {
                        callback();
                    } else {
                        callback($t('dashboard.exist'));
                    }
                } else {
                    callback($t('common.requiredField'));
                }
            },
        },
    ],
};

const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async valid => {
        if (valid) {
            formVisible.value = false;
            const params = {
                dashboardName: formData.value.name,
                openPublic: formData.value.public,
            };
            await updateDashboard(params);
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
</script>

<template>
    <el-dialog
        v-model="formVisible"
        :title="$t('dashboard.addNewDashboard')"
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
            @submit.native.prevent
        >
            <el-form-item :label="$t('common.name')" prop="name">
                <el-input
                    v-model="formData.name"
                    :placeholder="$t('dashboard.pleaseEnterDashboardName')"
                    maxlength="32"
                    clearable
                />
            </el-form-item>
            <el-checkbox v-model="formData.public"> {{ $t('dashboard.makeDashboardPublic') }} </el-checkbox>
        </el-form>
        <template #footer>
            <el-button @click="closeDialog" class="ms-cancel-btn">{{ $t('buttons.cancel') }}</el-button>
            <el-button type="primary" @click="submitForm(ruleFormRef)" class="exter-btn">
                {{ $t('buttons.save') }}
            </el-button>
        </template>
    </el-dialog>
</template>
