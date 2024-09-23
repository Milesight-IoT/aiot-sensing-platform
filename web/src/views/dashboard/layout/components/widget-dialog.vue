<script setup lang="ts">
import { ref, shallowRef, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { forms, widgetsKeys } from '../../components/widgets/forms';
import DeviceForm from '@/views/dashboard/components/widgets/device-form.vue';
import SnapshotForm from '@/views/dashboard/components/widgets/snapshot-form.vue';
const props = defineProps({
    visible: {
        type: Boolean,
        default: false,
    },
    /** 组件类型信息 */
    descript: {
        type: Object,
        default: () => {
            return {
                type: 2,
                isNew: true,
            };
        },
    },
    data: {
        type: Object,
        default: () => {
            return {
                i: '', // 编辑时应有ID信息，若id为空则新增
                // dashboard: {}, // 仪表盘信息
                // form: {
                //     channels: [], // 当类型为1时，存在感知通道信息
                //     statusType: 2, // 当类型为2时，有deviceStatus类型信息
                // },
            };
        },
    },
});
const showDialog = ref(false);
const formData = ref({ ...props.data });
const type = ref(2);
const dialogTitle = ref('panelTitle');
const formComponents = {
    1: SnapshotForm,
    2: DeviceForm,
    // alarmTable无新增弹窗
    3: 'alarmTable',
};
const dialogInfo = ref({ component: formComponents[type.value] });
const widgetRef = ref(null);
watch(
    () => props.visible,
    val => {
        showDialog.value = val;
    },
);
watch(
    () => showDialog.value,
    val => {
        emit('update:visible', val);
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
    () => props.descript,
    val => {
        type.value = val.type;
        getComponent(type.value);
    },
);

function getComponent(type) {
    const widgetKey = widgetsKeys[type];
    dialogTitle.value = $t(`widget.${widgetKey}`);
    dialogInfo.value = {
        component: shallowRef(formComponents[type]),
    };
}
const emit = defineEmits(['update:visible', 'update']);
const submitForm = async () => {
    try {
        const widgetItem = await widgetRef.value.submitForm();
        closeDialog();
        console.log('widgetItem', widgetItem);
        emit('update', widgetItem);
    } catch (e) {}
};
const closeDialog = () => {
    widgetRef.value.resetForm();
    showDialog.value = false;
};
</script>

<template>
    <el-dialog
        v-model="showDialog"
        :title="dialogTitle"
        :width="590"
        draggable
        :before-close="closeDialog"
        :close-on-click-modal="false"
    >
        <component :is="dialogInfo.component" :data="formData" ref="widgetRef" />
        <template #footer>
            <el-button @click="closeDialog" class="ms-cancel-btn">{{ $t('buttons.cancel') }}</el-button>
            <el-button type="primary" @click="submitForm" class="exter-btn">
                {{ $t('buttons.save') }}
            </el-button>
        </template>
    </el-dialog>
</template>
