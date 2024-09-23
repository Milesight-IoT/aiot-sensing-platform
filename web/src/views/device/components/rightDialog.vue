<script setup lang="ts">
import { ref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance, CascaderProps } from 'element-plus';
import rightPanel from '@/components/RightPanel/index.vue';
import { emitter } from '@/utils/mitt';
import { tabs } from './tabs';
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

const selected = ref(0);

function tabClick({ index }) {
    selected.value = index;
}
function updateTitle(name) {
    pannelTitle.value = name;
}
function updateForm(val) {
    formData.value = val;
}
function ClosePanel() {
    console.log({ ...formData.value });
    emit('updateTable', formData.value);
}
</script>

<template>
    <rightPanel
        :panelTitle="pannelTitle"
        :panelSubTitle="$t('device.details')"
        :panelStyle="{ 'max-width': '900px' }"
        @closePanel="ClosePanel"
    >
        <el-tabs @tab-click="tabClick">
            <template v-for="(item, index) of tabs" :key="item.key">
                <el-tab-pane :lazy="true" class="h-full">
                    <template #label>
                        <el-tooltip :content="$t(item.content)" placement="top-start">
                            <span :content="$t(item.content)">{{ $t(item.title) }}</span>
                        </el-tooltip>
                    </template>
                    <component
                        v-if="selected == index"
                        :is="item.component"
                        :data="formData"
                        @updateTitle="updateTitle"
                        @updateForm="updateForm"
                    />
                </el-tab-pane>
            </template>
        </el-tabs>
    </rightPanel>
</template>
