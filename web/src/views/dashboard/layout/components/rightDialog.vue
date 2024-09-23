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

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    formEl.resetFields();
};

const emit = defineEmits(['update:visible', 'add']);
watch(
    () => props.visible,
    val => {
        showPanel.value = val;
        emitter.emit('showPanel', val);
    },
);

function handleEmit(param) {
    console.log('Dialog handleEmit', param);
    emit('add', param);
}
</script>

<template>
    <rightPanel :panelTitle="$t('dashboard.selectWidgets')" :panelStyle="{ 'max-width': '860px' }">
        <div class="p-2.5">
            <Details :data="formData" @add="handleEmit" />
        </div>
    </rightPanel>
</template>
