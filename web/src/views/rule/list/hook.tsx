/*
 * @Author: sreindy8224 sreindy@milesight.com
 * @Date: 2023-04-24 10:07:42
 * @LastEditTime: 2023-06-25 13:21:45
 * @Descripttion: 规则列表
 */
import { message, confirmBox } from '@/utils/message';
import { getRuleList, deleteRule } from '@/api/rules';
import { type PaginationProps } from '@pureadmin/table';
import { reactive, ref, computed, onMounted } from 'vue';
import { $t } from '@/plugins/i18n';
import { dateTimeFormat } from '@/utils/tools';
import { emitter } from '@/utils/mitt';
export function useRule() {
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
        },
        {
            headerRenderer: () => $t('common.createdTime'),
            prop: 'createdTime',
            minWidth: 90,
            formatter: dateTimeFormat,
        },
        {
            headerRenderer: () => $t('common.name'),
            prop: 'name',
            minWidth: 120,
        },
        {
            headerRenderer: () => $t('rules.trigger'),
            prop: 'trigger',
            minWidth: 150,
            cellRenderer: ({ row }) => {
                return $t(row.trigger);
            },
        },
        {
            headerRenderer: () => $t('rules.actions'),
            prop: 'actions',
            minWidth: 150,
            cellRenderer: ({ row }) => {
                const arr = [];
                row.actions.split(',').forEach(item => {
                    arr.push($t(item));
                });
                return arr.join();
            },
        },
        {
            label: '',
            fixed: 'right',
            align: 'right',
            width: 90,
            slot: 'operation',
        },
    ];
    const selectInfo = ref({ show: false, tips: '' });
    const formDialogVisible = ref(false);
    const formData = ref({});
    function addRule() {
        formData.value = {
            name: '',
            trigger: 'Once data received', // default Once data received
            actions: 'Send to recipients',
            jsonData: {
                threshold: 10,
            },
        };
        formDialogVisible.value = true;
    }
    function updateTable(formData) {
        // 新增后刷新界面
        console.log('update');
        if (formData == undefined) {
            onSearch();
        } else {
            // 编辑设备后使用Id更新数组
            const updateIndex = dataList.value.findIndex(item => item.ruleChainOwnId.id == formData.ruleChainOwnId.id);
            dataList.value[updateIndex] = formData;
        }
    }
    const handleDelete = async item => {
        const ruleId = item.ruleChainOwnId.id;
        const ruleName = item.name;
        const sureTitle = $t('rules.sureDel').replace('%s', ruleName);
        const isConfirm = await confirmBox($t('rules.delTips'), {
            title: sureTitle,
        });
        if (isConfirm) {
            await deleteRule(ruleId);
            message($t('status.success'));
            getPageInfo();
        }
    };
    let multipleSelection = [];
    function handleSelectionChange(val) {
        const count = val.length;
        const key = count > 1 ? 'rules.selectTips' : 'rules.selectOneTips';
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
        const key = delCount > 1 ? 'rules.sureDels' : 'rules.sureDelOne';
        const title = $t(key).replace('%s', delCount);
        const isConfirm = await confirmBox($t('rules.multiDelTips'), { title });
        if (isConfirm) {
            for (const item of multipleSelection) {
                const ruleId = item.ruleChainOwnId.id;
                await deleteRule(ruleId);
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
        const { data, totalElements } = await getRuleList(queryParams);
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

    const detailData = ref({ jsonData: {} });
    function showDetail(row) {
        detailData.value = Object.assign(
            {
                name: '',
                trigger: 'Once data received', // default Once data received
                actions: 'Send to recipients',
                jsonData: {},
            },
            row,
        );
        emitter.emit('showPanel', true);
        emitter.emit('panelTitle', row.name);
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
        addRule,
        showDetail,
        handleDelete,
        deleteItems,
        handleSizeChange,
        handleCurrentChange,
        handleSelectionChange
    };
}
