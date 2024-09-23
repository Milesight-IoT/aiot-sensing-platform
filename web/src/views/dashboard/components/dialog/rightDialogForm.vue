<!--
 * @Author: sreindy8224 sreindy@milesight.com
 * @Date: 2023-04-24 10:07:42
 * @LastEditTime: 2023-06-24 16:29:56
 * @Descripttion: 添加仪表盘表单
-->
<script setup lang="ts">
import { ref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance } from 'element-plus';
import { checkDashboardByName } from '@/api/dashboard';
import { emitter } from '@/utils/mitt';
import { updateDashboard, getDashboardById } from '@/api/dashboard';
import { useDashboard } from '@/views/dashboard/list/hook';
import { computed } from 'vue';
const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return {};
        },
    },
});
watch(
    () => props.data,
    val => {
        const data = JSON.parse(JSON.stringify(val));
        if (!data.assignedCustomers || data.assignedCustomers.length == 0) {
            data.assignedCustomers = [{ public: false }];
        }
        formData.value = { ...data };
    },
);
const emit = defineEmits(['updateForm', 'updateTitle']);
const formRef = ref<FormInstance>();
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
                    //当前名称则过滤校验
                    if (textSearch == props.data.name) callback();
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

const { copyLink, getPublicLink } = useDashboard();

const publicLink = computed(() => {
    return getPublicLink(formData.value);
});
const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async valid => {
        if (valid) {
            formVisible.value = false;
            const params = {
                dashboardId: formData.value.id.id,
                dashboardName: formData.value.name,
                openPublic: formData.value.assignedCustomers[0].public,
            };
            await updateDashboard(params);
            emitter.emit('panelTitle', params.dashboardName);
            emit('updateForm', formData.value);
            message($t('status.success'));
        }
    });
};
</script>

<template>
    <!-- 表单内容 -->
    <el-form ref="formRef" :model="formData" :rules="rules" label-position="top" require-asterisk-position="right">
        <el-form-item :label="$t('common.name')" prop="name">
            <el-input v-model="formData.name" :placeholder="$t('rules.pleaseEnterRuleName')" maxlength="32" clearable />
        </el-form-item>
        <el-form-item>
            <el-checkbox v-model="formData.assignedCustomers[0].public">
                {{ $t('dashboard.makeDashboardPublic') }}
            </el-checkbox>
        </el-form-item>

        <el-form-item :label="$t('dashboard.publicLink')" class="flex">
            <el-input :value="publicLink" disabled class="!w-5/6 flex-auto" :title="publicLink" />
            <el-button
                @click="copyLink(getPublicLink(formData))"
                class="ml-2 !bg-blue-500 exter-btn"
                :disabled="!formData.assignedCustomers[0].public"
            >
                {{ $t('buttons.copy') }}
            </el-button>
        </el-form-item>
    </el-form>
    <el-button type="primary" @click="submitForm(formRef)" class="exter-btn">
        {{ $t('buttons.save') }}
    </el-button>
</template>
