<script setup lang="ts">
import { ref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance } from 'element-plus';
import rightPanel from '@/components/RightPanel/index.vue';
import { emitter } from '@/utils/mitt';
import Details from './rightDialogForm.vue';
const props = defineProps({
    visible: {
        type: Boolean,
        default: false,
    },
    data: {
        type: Object,
        default: () => {
            return { name: '' };
        },
    },

    /** 允许父组件传入标题参数 */
    title: {
        type: String,
    },
});

const ruleFormRef = ref<FormInstance>();

const showPanel = ref(false);
const formData = ref(props.data);
const pannelTitle = ref('panelTitle');

const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(valid => {
        if (valid) {
            message($t('status.success'), { type: 'success' });
            showPanel.value = false;
            resetForm(formEl);
        }
    });
};

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    formEl.resetFields();
};

const closeDialog = () => {
    showPanel.value = false;
    resetForm(ruleFormRef.value);
};

const emit = defineEmits(['update:visible', 'updateTable']);
watch(
    () => props.visible,
    val => {
        showPanel.value = val;
        emitter.emit('showPanel', val);
    },
);

watch(
    () => props.data,
    val => {
        formData.value = val;
        console.log(val);
    },
);

watch(
    () => props.title,
    val => {
        updateTitle(val);
    },
);

/** 更新右侧抽屉窗的标题 */
function updateTitle(name) {
    pannelTitle.value = name;
}
function updateForm(val) {
    formData.value = val;
    emit('updateTable', val);
}
</script>

<template>
    <rightPanel
        :panelTitle="pannelTitle"
        :panelSubTitle="$t('dashboard.details')"
        :panelStyle="{ 'max-width': '630px' }"
    >
        <div class="p-5">
            <Details :data="formData" @updateTitle="updateTitle" @updateForm="updateForm" />
        </div>
    </rightPanel>
</template>
