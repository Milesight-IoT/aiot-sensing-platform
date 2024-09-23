<!--
 * @Author: Lin lin@milesight.com
 * @Date: 2023-06-07 14:09:54
 * @LastEditTime: 2023-06-26 14:28:10
 * @Descripttion: Dashboard-Widget-设备状态组件
-->
<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue';
import { $t } from '@/plugins/i18n';
const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return { type: 1, count: 12, percent: 100 };
        },
    },
    total: {
        type: Number,
        default: 1,
    },
    wsObject: {
        type: Object,
    },
});
const colorClass = ref('');
const title = ref('');
const showPercent = ref(false);
const formatType = type => {
    console.log('formatType', type);
    if (!type) return;
    const typeList = {
        1: { name: 'widget.totalDevices', colorClass: 'total', showPercent: false },
        2: { name: 'widget.activeDevices', colorClass: 'active', showPercent: true },
        3: { name: 'widget.inactiveDevices', colorClass: 'inactive', showPercent: true },
    };
    colorClass.value = typeList[type].colorClass;
    title.value = typeList[type].name;
    showPercent.value = typeList[type].showPercent;
};
onMounted(() => {
    formatType(props.data?.type);
});
watch(
    () => props.data,
    val => {
        formatType(val.type);
    },
);
const percent = computed(() => {
    if (props.data.count && props.total) {
        return Number((props.data.count / props.total) * 100).toFixed(2);
    } else {
        return '-';
    }
});
</script>

<template>
    <div class="status-box">
        <div class="title" :class="colorClass">{{ $t(title) }}</div>
        <div class="info">
            <span>{{ data.count }}</span>
            <span :class="colorClass" v-if="showPercent">{{ percent }}%</span>
        </div>
    </div>
</template>
<style scoped lang="scss">
.status-box {
    display: flex;
    width: 100%;
    height: 100%;
    flex-direction: column;
    text-align: center;
    justify-content: center;
    color: #353c4a;
    font-weight: 700;
    .title {
        margin-bottom: 20px;
    }
    .info {
        font-size: 26px;
    }
    .total {
        color: #333;
    }
    .inactive {
        color: #fc454b;
        margin-left: 10px;
    }
    .active {
        color: #5ab92b;
        margin-left: 10px;
    }
}
</style>
