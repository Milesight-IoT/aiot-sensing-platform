import { getSensingObjectList, deleteSensingObject } from '@/api/sensing-object';
import { message, confirmBox } from '@/utils/message';
import { type PaginationProps } from '@pureadmin/table';
import { reactive, ref, computed, onMounted } from 'vue';
import { $t } from '@/plugins/i18n';
import { dateTimeFormat, getDateTime, getTableColums } from '@/utils/tools';
import { emitter } from '@/utils/mitt';

export function useSensingObject() {
    const dataList = ref([]);
    const loading = ref(true);
    const INITIAL_DATA = {
        name: '',
        channels: [],
    };
    const formData = ref({ ...INITIAL_DATA });
    const formDialogVisible = ref(false);
    const rightDialogVisible = ref(false);
    const tableListRef = ref();
    const pagination = reactive<PaginationProps>({
        total: 0,
        pageSizes: [10, 20, 30],
        pageSize: 30,
        currentPage: 1,
    });
    const columns: TableColumnList = getTableColums([
        {
            type: 'selection',
            width: 65,
        },
        {
            label: 'common.createdTime',
            prop: 'createdTime',
            width: 200,
            formatter: dateTimeFormat,
        },
        {
            label: 'common.name',
            prop: 'name',
            width: 200,
        },
        {
            label: 'object.sensingChannels',
            prop: 'sensingChannels',
            cellRenderer: ({ row, props }) => {
                return (
                    <div class="flex flex-wrap">
                        {row.sensingChannels.length
                            ? row.sensingChannels.map(item => {
                                const keyName = `${item.deviceName}/${item.ability === 'image' ? $t('device.fullImg') : item.ability
                                    }`;
                                const keyValue = item.abilityType === 2 ? getDateTime(item.ts) : item.value;
                                return (
                                    <p class="mr-7">
                                        <span class="w-32 inline-block truncate align-bottom" title={keyName}>
                                            {keyName}
                                        </span>
                                        <span
                                            class="w-36 inline-block font-bold truncate align-bottom"
                                            title={keyValue}
                                        >
                                            {keyValue}
                                        </span>
                                    </p>
                                );
                            })
                            : $t('object.noAssociatChannel')}
                    </div>
                );
            },
        },
        {
            label: '',
            fixed: 'right',
            align: 'right',
            width: 90,
            slot: 'operation',
        },
    ]);
    const buttonClass = computed(() => {
        return ['!h-[20px]', 'reset-margin', '!text-gray-500', 'dark:!text-white', 'dark:hover:!text-primary'];
    });
    const selectInfo = ref({ show: false, tips: '' });
    async function onSearch(
        queryParams = {
            pageSize: pagination.pageSize,
            page: 0,
            sortProperty: 'createdTime',
            sortOrder: 'DESC',
        },
    ) {
        loading.value = true;
        const { data, totalElements } = await getSensingObjectList(queryParams);
        dataList.value = data;
        pagination.total = totalElements;
        pagination.currentPage = queryParams.page + 1;
        setTimeout(() => {
            loading.value = false;
        }, 500);
    }
    function updateTable(formData) {
        // 新增后刷新界面
        if (formData == undefined) {
            onSearch();
        } else {
            // 编辑设备后使用Id更新数组
            const updateIndex = dataList.value.findIndex(item => item.id == formData.id);
            dataList.value[updateIndex] = formData;
        }
    }
    function addObject() {
        formData.value = Object.assign({}, INITIAL_DATA);
        formDialogVisible.value = true;
    }
    const deleteObject = async (deviceId: String) => {
        try {
            await deleteSensingObject(deviceId);
        } catch (e) {
            console.log(e);
        }
    };
    async function handleDelete(row) {
        console.log(row);
        const title = $t('object.sureDelete').replace('%s', row.name);
        const isConfirm = await confirmBox($t('object.deleteTips'), { title });
        if (isConfirm) {
            const SensingObjectId = row.id;
            await deleteSensingObject(SensingObjectId);
            getPageInfo();
            message($t('status.success'));
        }
    }
    const initChannelsData = sensingChannels => {
        return sensingChannels.map(item => {
            return [item.deviceId, item.deviceAbilityId];
        });
    };
    function showDetail(row) {
        const { sensingChannels } = row;
        formData.value = Object.assign({}, row, { channels: initChannelsData(sensingChannels) });
        emitter.emit('showPanel', true);
        emitter.emit('panelTitle', row.name);
    }

    const resetForm = formEl => {
        if (!formEl) return;
        formEl.resetFields();
        onSearch();
    };
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
        pagination.currentPage = 1;
        onSearch(queryParams);
    };
    let multipleSelection = [];
    function handleSelectionChange(val) {
        const count = val.length;
        const key = count > 1 ? 'object.deletesSelTips' : 'object.oneSelObject';
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
        const key = delCount > 1 ? 'object.sureDeleteObjects' : 'object.sureDeleteObject';
        const title = $t(key).replace('%s', delCount);
        const isConfirm = await confirmBox($t('object.sureDeletesTips'), { title });
        if (isConfirm) {
            for (const item of multipleSelection) {
                const SensingObjectId = item.id;
                await deleteSensingObject(SensingObjectId);
            }
            getPageInfo(delCount);
            const { clearSelection } = tableListRef.value.getTableRef();
            clearSelection();
            selectInfo.value.show = false;
            message($t('status.success'));
        }
    };
    const getPageInfo = async (delCount = 1) => {
        const { total, pageSize, currentPage } = pagination;
        const totalPage = Math.ceil((total - delCount) / pageSize) - 1;
        let page = Math.min(currentPage - 1, totalPage);
        page = Math.max(0, page);
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
        console.log(`${val} items per page`);
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

    onMounted(() => {
        onSearch();
    });

    return {
        selectInfo,
        tableListRef,
        searchName,
        deleteItems,
        formData,
        formDialogVisible,
        rightDialogVisible,
        loading,
        columns,
        dataList,
        pagination,
        buttonClass,
        updateTable,
        onSearch,
        resetForm,
        addObject,
        showDetail,
        handleDelete,
        handleSizeChange,
        handleCurrentChange,
        handleSelectionChange,
    };
}
