<!--
 * @Author: Lin lin@milesight.com
 * @Date: 2023-06-07 17:58:35
 * @LastEditTime: 2023-06-12 15:14:56
 * @Descripttion: 设备数量组件表单内容
-->
<script setup lang="ts">
import { ref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { FormInstance } from 'element-plus';
import { deviceStatusType, deviceStatus as deviceStatusStr } from './forms';
const props = defineProps({
    /** 父组件传入当前仪表盘对象 */
    data: {
        type: Object,
        default: () => {
            return {
                i: '', // 编辑时应有ID信息，若id为空则新增
                type: 'Active devices', // 点击新增时的默认值，当类型为2时，有deviceStatus类型信息
            };
        },
    },
});
watch(
    () => props.data,
    val => {
        formData.value = val;
        // dashboardData.value = { ...val.dashboard };
    },
);
const formRef = ref<FormInstance>();
const formData = ref(props.data);
const statusText = {
    1: 'Total devices',
    2: 'Active devices',
    3: 'Inactive devices',
};
const submitForm = () => {
    formData.value.data = {
        type: deviceStatusStr[formData.value.type],
    };

    resetForm();
    return formData.value;
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
    <el-form ref="formRef" :model="formData" label-position="top" require-asterisk-position="right">
        <el-form-item :label="$t('widget.statusType')">
            <el-select v-model="formData.type">
                <el-option
                    v-for="(number, text) in deviceStatusStr"
                    :key="number"
                    :label="$t(deviceStatusType[number])"
                    :value="text"
                />
            </el-select>
        </el-form-item>
    </el-form>
</template>
