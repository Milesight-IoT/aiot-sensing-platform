import { message, confirmBox } from '@/utils/message';
import { getRecipientList, deleteRecipient } from '@/api/recipient';
import { getRuleListByRecipient } from '@/api/rules';
import { type PaginationProps } from '@pureadmin/table';
import { reactive, ref, computed, onMounted } from 'vue';
import { $t } from '@/plugins/i18n';
import { dateTimeFormat } from '@/utils/tools';
import { emitter } from '@/utils/mitt';
import { useRouter } from 'vue-router';
export function useRecipient() {
    const loading = ref(true);
    const dataList = ref([]);
    const tableListRef = ref();
    const pagination = reactive<PaginationProps>({
        total: 0,
        pageSizes: [10, 20, 30],
        pageSize: 30,
        currentPage: 1,
    });
    const columns: TableColumnList = [
        {
            type: 'selection',
            width: 65,
        },
        {
            headerRenderer: () => $t('common.createdTime'),
            prop: 'createdTime',
            minWidth: 80,
            formatter: dateTimeFormat,
        },
        {
            headerRenderer: () => $t('common.name'),
            prop: 'name',
            minWidth: 80,
        },
        {
            headerRenderer: () => $t('recip.transProtocol'),
            prop: 'transmissionType',
            minWidth: 200,
            cellRenderer: ({ row }) => {
                let res;
                switch (row.transmissionType) {
                    case 'HTTP Post':
                        res = { label: $t('recip.httpPost') };
                        break;
                    case 'MQTT':
                        res = { label: $t('recip.mqtt') };
                        break;
                    default:
                        res = { label: '' };
                }
                return res.label;
            },
        },
        {
            label: '',
            fixed: 'right',
            align: 'right',
            width: 100,
            slot: 'operation',
        },
    ];
    const protocolOpts = [
        {
            value: '1',
            text: 'HTTP Post',
            label: $t('recip.httpPost'),
        },
        {
            value: '2',
            text: 'MQTT',
            label: $t('recip.mqtt'),
        },
    ];
    const selectInfo = ref({ show: false, tips: '' });
    const formDialogVisible = ref(false);
    const formData = ref({});
    function addRecipient() {
        formData.value = {
            name: '',
            transmissionType: 'HTTP Post',
            jsonData: {},
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
            const updateIndex = dataList.value.findIndex(item => item.recipientsId.id == formData.recipientsId.id);
            dataList.value[updateIndex] = formData;
        }
    }

    const router = useRouter();
    /**
     * 单行删除
     * 1. 检查当前接受方关联的规则，返回是否关联
     */
    async function handleDelete(row) {
        const id = row.recipientsId.id;
        const name = row.name;
        const data = await getRuleListByRecipient(id);
        if (data.length) {
            const sureTitle = $t('recip.sureDeleteSensing');
            const associateStrs = Array.from(
                new Set(
                    data.map(item => {
                        return `'${item.name}'`;
                    }),
                ),
            ).join(', ');
            const title = sureTitle.replace('%s', name).replace('%s1', associateStrs);
            const isConfirm = await confirmBox($t('recip.deleteSensingTips'), {
                title,
                cancelButtonText: $t('buttons.ok'),
                confirmButtonText: $t('recip.jumpToRule'),
                confirmButtonClass: 'inter-btn',
            });
            if (isConfirm) {
                router.push('/rules');
            }
            return true;
        } else {
            const sureTitle = $t('recip.sureDelete').replace('%s', name);
            const isConfirm = await confirmBox($t('recip.deleteTips'), {
                title: sureTitle,
            });
            if (isConfirm) {
                await deleteRecipient(id);
                message($t('status.success'));
                getPageInfo();
            }
            return false;
        }
    }
    let multipleSelection = [];
    function handleSelectionChange(val) {
        const count = val.length;
        const key = count > 1 ? 'recip.selectTips' : 'recip.selectOneTips';
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
        const key = delCount > 1 ? 'recip.sureDels' : 'recip.sureDelOne';
        const title = $t(key).replace('%s', delCount);
        const isConfirm = await confirmBox($t('recip.delRecipientTips'), { title });
        if (isConfirm) {
            /** 存在关联无法删除的接收方数组 */
            const assoArr = [];
            for (const item of multipleSelection) {
                const id = item.recipientsId.id;
                const data = await getRuleListByRecipient(id);
                if (data.length) {
                    assoArr.push(item.name);
                } else {
                    await deleteRecipient(id);
                }
            }
            if (assoArr.length) {
                let content = '';
                assoArr.forEach(item => {
                    content += `<span class="ml-2 el-tag">${item}</span>`;
                });
                const isConfirm = await confirmBox(content, {
                    title: $t('recip.delItemsFailed'),
                    cancelButtonText: $t('buttons.ok'),
                    confirmButtonText: $t('recip.jumpToRule'),
                    confirmButtonClass: 'inter-btn',
                    dangerouslyUseHTMLString: true,
                });
                if (isConfirm) {
                    router.push('/rules');
                }
            } else {
                message($t('status.success'));
            }
            const hasItem = delCount - assoArr.length;
            getPageInfo(hasItem);
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
        console.log(`current page: ${val}`);
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
        const { data, totalElements } = await getRecipientList(queryParams);
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

    function showDetail(row) {
        // TODO 调用了两遍？？？
        console.log('showDetail', row);
        formData.value = Object.assign(
            {
                name: '',
                transmissionType: '1',
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
        tableListRef,
        loading,
        columns,
        dataList,
        pagination,
        formDialogVisible,
        onSearch,
        searchName,
        resetForm,
        updateTable,
        addRecipient,
        showDetail,
        handleDelete,
        deleteItems,
        handleSizeChange,
        handleCurrentChange,
        handleSelectionChange,
        protocolOpts,
    };
}
