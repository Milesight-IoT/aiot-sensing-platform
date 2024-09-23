/*
 * @Author: sreindy8224 sreindy@milesight.com
 * @Date: 2023-04-24 10:07:42
 * @LastEditTime: 2023-07-09 16:04:58
 * @Descripttion: 仪表盘列表
 */
import { message, confirmBox } from '@/utils/message';
import { getRuleList, deleteRule } from '@/api/rules';
import { type PaginationProps } from '@pureadmin/table';
import { reactive, ref, computed, onMounted, unref } from 'vue';
import { $t } from '@/plugins/i18n';
import { dateTimeFormat } from '@/utils/tools';
import { emitter } from '@/utils/mitt';
import { useCopyToClipboard } from '@pureadmin/utils';
import { getDashboardList, deleteDashboard } from '@/api/dashboard';
import router from '@/router';

export function useDashboard() {
    const loading = ref(true);
    const dataList = ref([]);
    const tableListRef = ref();
    const pagination = reactive<PaginationProps>({
        total: 0,
        pageSizes: [10, 20, 30],
        pageSize: 30,
        currentPage: 1,
    });
    const buttonClass = computed(() => {
        return ['text-gray_6', 'dark:!text-white', 'dark:hover:!text-primary'];
    });
    const columns: TableColumnList = [
        {
            type: 'selection',
            width: 65,
            selectable: row => !isDefault(row),
        },
        {
            headerRenderer: () => $t('common.createdTime'),
            prop: 'createdTime',
            minWidth: 90,
            // formatter: dateTimeFormat,
            cellRenderer: ({ row, column }) => {
                if (isDefault(row)) {
                    return <span>-</span>;
                } else {
                    return dateTimeFormat(row, column);
                }
            },
        },
        {
            headerRenderer: () => $t('common.name'),
            prop: 'name',
            minWidth: 120,
        },
        {
            headerRenderer: () => $t('dashboard.public'),
            prop: 'trigger',
            minWidth: 150,
            cellRenderer: ({ row }) => {
                const public1 = row.assignedCustomers && row.assignedCustomers[0]?.public;
                return <el-checkbox disabled={true} v-model={public1}></el-checkbox>;
            },
        },
        {
            label: '',
            fixed: 'right',
            align: 'right',
            width: 120,
            slot: 'operation',
        },
    ];

    const isDefault = row => {
        return row.createdTime == 253402271999000;
    };

    const selectInfo = ref({ show: false, tips: '' });
    const formDialogVisible = ref(false);
    const formData = ref({});
    function handleAdd() {
        formData.value = {
            name: '',
            public: true,
        };
        formDialogVisible.value = true;
    }
    function updateTable(formData) {
        // 新增后刷新界面
        if (formData == undefined) {
            onSearch();
        } else {
            // 编辑后使用Id更新数组
            const updateIndex = dataList.value.findIndex(item => item.id.id == formData.id.id);
            dataList.value[updateIndex] = formData;
        }
    }
    const handleDelete = async item => {
        const id = item.id.id;
        const name = item.name;
        const sureTitle = $t('dashboard.sureDel').replace('%s', name);
        const isConfirm = await confirmBox($t('dashboard.delTips'), {
            title: sureTitle,
        });
        if (isConfirm) {
            await deleteDashboard(id);
            message($t('status.success'));
            getPageInfo();
        }
    };
    let multipleSelection = [];
    function handleSelectionChange(val) {
        const count = val.length;
        const key = count > 1 ? 'dashboard.selectTips' : 'dashboard.selectOneTips';
        multipleSelection = val;
        selectInfo.value = {
            key,
            count,
            show: count > 0,
            tips: $t(key).replace('%s', count),
        };
    }
    const deleteItems = async () => {
        const delCount = multipleSelection.length;
        const key = delCount > 1 ? 'dashboard.sureDels' : 'dashboard.sureDelOne';
        const title = $t(key).replace('%s', delCount);
        const isConfirm = await confirmBox($t('dashboard.multiDelTips'), { title });
        if (isConfirm) {
            for (const item of multipleSelection) {
                const ruleId = item.id.id;
                await deleteDashboard(ruleId);
            }
            message($t('status.success'));
            getPageInfo(delCount);
            const { clearSelection } = tableListRef.value.getTableRef();
            clearSelection();
            selectInfo.value.show = false;
        }
    };
    const getPageInfo = async (delCount = 1) => {
        const { total, pageSize, currentPage } = pagination;
        const totalPage = Math.ceil((total - delCount) / pageSize) - 1;
        const page = Math.max(Math.min(currentPage - 1, totalPage), 0);
        const queryParams = {
            textSearch: textSearchLast,
            pageSize,
            page,
            sortProperty: 'createdTime',
            sortOrder: 'DESC',
        };
        onSearch(queryParams);
    };

    function handleSizeChange(val: number) {
        const queryParams = {
            textSearch: textSearchLast,
            pageSize: val,
            page: 0,
            sortProperty: 'createdTime',
            sortOrder: 'DESC',
        };
        onSearch(queryParams);
    }

    function handleCurrentChange(val: number) {
        const { pageSize } = pagination;
        const page = Math.max(val - 1, 0);
        const queryParams = {
            textSearch: textSearchLast,
            pageSize,
            page,
            sortProperty: 'createdTime',
            sortOrder: 'DESC',
        };
        onSearch(queryParams);
    }

    async function onSearch(
        queryParams = {
            pageSize: pagination.pageSize,
            page: 0,
            sortProperty: 'createdTime',
            sortOrder: 'DESC',
        },
    ) {
        loading.value = true;
        const { data, totalElements } = await getDashboardList(queryParams);
        dataList.value = data;
        pagination.currentPage = queryParams.page + 1;
        pagination.total = totalElements;
        setTimeout(() => {
            loading.value = false;
        }, 500);
    }
    let textSearchLast = '';
    const searchName = textSearch => {
        textSearchLast = textSearch;
        const queryParams = {
            textSearch,
            pageSize: pagination.pageSize,
            page: 0,
            sortProperty: 'createdTime',
            sortOrder: 'DESC',
        };
        onSearch(queryParams);
    };

    const resetForm = formEl => {
        if (!formEl) return;
        formEl.resetFields();
        onSearch();
    };

    const detailData = ref({ assignedCustomers: [{ public: false }] });
    function showDetail(row) {
        detailData.value = Object.assign(
            {
                name: '',
                public: null,
            },
            row,
        );
        emitter.emit('showPanel', true);
        emitter.emit('panelTitle', row.name);
    }

    const { clipboardValue, copied } = useCopyToClipboard();
    function copyLink(content) {
        clipboardValue.value = ''
        clipboardValue.value = unref(content);
        // if (copied.value) {
        //     message($t('status.success'));
        // }
    }
    const getPublicLink = formData => {
        const dashboardId = formData.id?.id;
        const publicId = formData.assignedCustomers && formData.assignedCustomers[0]?.customerId?.id;
        return `${location.origin}/dashboards/${dashboardId}?publicId=${publicId}`;
    };

    function gotoLayout(row) {
        router.push({
            name: 'dashboardDetail',
            params: {
                id: row.id.id,
            },
        });
    }

    onMounted(() => {
        onSearch();
    });
    return {
        selectInfo,
        formData,
        detailData,
        tableListRef,
        loading,
        columns,
        dataList,
        pagination,
        buttonClass,
        formDialogVisible,
        onSearch,
        searchName,
        resetForm,
        updateTable,
        handleAdd,
        showDetail,
        handleDelete,
        deleteItems,
        handleSizeChange,
        handleCurrentChange,
        handleSelectionChange,
        isDefault,
        copyLink,
        getPublicLink,
        gotoLayout,
    };
}
