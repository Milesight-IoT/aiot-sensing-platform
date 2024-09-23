<!--
 * @Author: sreindy8224 sreindy@milesight.com
 * @Date: 2023-04-24 10:07:42
 * @LastEditTime: 2023-06-28 00:58:21
 * @Descripttion: 添加仪表盘表单
-->
<script setup lang="ts">
import { ref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { widgetsKeys } from '../../components/widgets/forms';
import statusImg from '@/assets/dashboard/devices_status.png';
import alarmTableImg from '@/assets/dashboard/alarm_table.png';
import snapshotPreviewImg from '@/assets/dashboard/snapshot_preview.png';
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
    val => {},
);
const emit = defineEmits(['add']);

/** 组件类型选项 */
const typeOptions = ref([
    {
        // type: widgetsKeys[1],
        type: 1,
        title: 'dashboard.snapshotPreview',
        img: snapshotPreviewImg,
        text: 'dashboard.snapshotPreviewDescription',
    },
    {
        type: 2,
        title: 'dashboard.devicesStatus',
        img: statusImg,
        text: 'dashboard.devicesStatusDescription',
    },
    {
        type: 3,
        title: 'dashboard.alarmTable',
        img: alarmTableImg,
        text: 'dashboard.alarmTableDescription',
    },
]);

/** 触发添加事件至layout父组件，由layout执行 */
function showAddDialog(type) {
    emit('add', { type });
}
</script>

<template>
    <div class="card-box">
        <el-card v-for="item in typeOptions" :key="item.type" @click="showAddDialog(item.type)">
            <div class="content">
                <h3 class="mb-4">{{ $t(item.title) }}</h3>
                <div class="flex">
                    <img class="mr-5 preview" :src="item.img" />
                    <p>{{ $t(item.text) }}</p>
                </div>
            </div>
        </el-card>
    </div>
</template>
<style scoped lang="scss">
.card-box {
    display: flex;
    flex-wrap: wrap;
    .el-card {
        margin: 10px;
        width: 390px;
        height: 220px;
        border-radius: 5px;
        box-shadow: 2px 2px 4px #00000029;
        cursor: pointer;
        .content {
            img.preview {
                display: inline-block;
                width: 178px;
                height: 118px;
            }
            p {
                display: inline-block;
            }
        }
    }
}
</style>
