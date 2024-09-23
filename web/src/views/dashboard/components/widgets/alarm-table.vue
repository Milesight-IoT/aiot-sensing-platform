<script setup lang="ts">
import { PureTableBar } from '@/components/RePureTableBar';
import { reactive, ref, computed, onMounted, watch } from 'vue';
import { type PaginationProps } from '@pureadmin/table';
import { $t } from '@/plugins/i18n';
import { dateTimeFormat } from '@/utils/tools';
onMounted(() => {
    //onSearch();
    const { totalElements } = props.data;
    pagination.total = totalElements;
});
const props = defineProps({
    /** 父组件传入当前仪表盘对象 */
    data: {
        type: Object,
        default: () => {
            return {
                data: [
                    {
                        createdTime: 1687931142402,
                        type: 'Low battery',
                        value: '{"alarmThreshold":50,"battery":5}',
                        deviceId: {
                            entityType: 'DEVICE',
                            id: 'fe7658a0-b8df-11ed-8971-6f97d23ce966',
                        },
                        ruleName: 'a two',
                        ruleChainId: {
                            entityType: 'RULE_CHAIN_OWN',
                            id: '28e9a3e0-099d-11ee-a48f-df2326edf6cc',
                        },
                        tenantId: {
                            entityType: 'TENANT',
                            id: '50a20150-9019-11ed-966c-67f3e94fcfec',
                        },
                        source: '299023090I52',
                        id: {
                            entityType: 'DASHBOARD_RULE_DEVICES',
                            id: '04fcc530-1577-11ee-802d-8d6238cba320',
                        },
                        name: 'dashboard_rule_devices',
                    },
                ],
                hasNext: false,
                totalElements: 0,
                totalPages: 0,
            };
        },
    },
    cmdId: {
        type: Number,
        defalut: 0,
    },
});
// const loading = ref(true);
watch(
    () => props.data,
    val => {
        const { data, totalElements, currentPage } = val;
        dataList.value = data;
        pagination.total = totalElements;
        //pagination.currentPage = currentPage;
    },
);
const dataList = ref(props.data.data);
const tableListRef = ref();
const pagination = reactive<PaginationProps>({
    total: 0,
    pagerCount: 3,
    pageSizes: [10, 20, 30],
    pageSize: 30,
    currentPage: 1,
});
const formatDetail = (row, col) => {
    const value = row[col.property];
    if (row.type == 'Low battery') {
        console.log(JSON.parse(value));
        return JSON.parse(value)?.battery;
    } else {
        return '-';
    }
};
const columns: TableColumnList = [
    {
        headerRenderer: () => $t('common.createdTime'),
        prop: 'createdTime',
        minWidth: 140,
        formatter: dateTimeFormat,
    },
    {
        headerRenderer: () => $t('common.type'),
        prop: 'type',
        minWidth: 100,
        cellRenderer: ({ row }) => {
            const typeStr = {
                'Low battery': 'rules.lowBattery',
                'Devices become inactive': 'rules.deviceInactive',
                'Once data received': 'rules.onceReceive',
            };
            return $t(typeStr[row.type]);
        },
    },
    {
        headerRenderer: () => $t('dashboard.source'),
        prop: 'source',
        minWidth: 140,
    },
    {
        headerRenderer: () => $t('common.detail'),
        prop: 'value',
        minWidth: 100,
        formatter: formatDetail,
        // cellRenderer: row => {
        //     return
        // }
    },
];
function handleSizeChange(val: number) {
    const queryParams = {
        pageSize: val,
        page: 0,
        sortProperty: 'createdTime',
        // sortOrder: 'DESC',
    };
    onSearch(queryParams);
}

function handleCurrentChange(val: number) {
    const { pageSize } = pagination;
    const page = Math.max(val - 1, 0);
    const queryParams = {
        pageSize,
        page,
        sortProperty: 'createdTime',
        // sortOrder: 'DESC',
    };
    onSearch(queryParams);
}
// 连接ws
const emit = defineEmits(['search']);
async function onSearch(
    queryParams = {
        pageSize: pagination.pageSize,
        page: 0,
        sortProperty: 'createdTime',
        // sortOrder: 'DESC',
    },
) {
    emit('search', queryParams, props.cmdId);
}
</script>

<template>
    <PureTableBar :title="$t('dashboard.alarmTable')" :showSearch="false">
        <template #buttons />
        <pure-table
            showOverflowTooltip
            ref="tableListRef"
            table-layout="fixed"
            :data="dataList"
            :columns="columns"
            :pagination="pagination"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
    </PureTableBar>
</template>

<style scoped></style>
