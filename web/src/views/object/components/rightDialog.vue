<!--
 * @Author: Lin
 * @Date: 2023-02-06 20:50:40
 * @LastEditTime: 2023-04-24 10:46:03
 * @Descripttion: your project
-->
<script setup lang="ts">
import { ref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import rightPanel from '@/components/RightPanel/index.vue';
import { tabs } from './tabs';

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
const formData = ref({ ...props.data });
watch(
    () => props.data,
    val => {
        formData.value = val;
    },
);
const selected = ref(0);

function tabClick({ index }) {
    selected.value = index;
}
function updateForm(val) {
    console.log(val);
    formData.value = val;
}
const emit = defineEmits(['updateTable']);
function ClosePanel() {
    emit('updateTable', formData.value);
}
</script>
<template>
    <rightPanel
        panelTitle="ComunitString"
        :panelSubTitle="$t('object.details')"
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
                        @updateForm="updateForm"
                    />
                </el-tab-pane>
            </template>
        </el-tabs>
    </rightPanel>
</template>
