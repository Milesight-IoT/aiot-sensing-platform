import { message, confirmBox } from '@/utils/message';
import { getUserOtaPackageList, downloadOtaPakage, deleteOtaPackage } from '@/api/ota';
import { type PaginationProps } from '@pureadmin/table';
import { reactive, ref, computed, onMounted } from 'vue';
import { $t } from '@/plugins/i18n';
import { dateTimeFormat, getTableColums } from '@/utils/tools';

export function useDevice() {
    const INITIAL_DATA = {
        name: 'test',
        type: '',
        credentialsId: '',
        distributeStatus: false,
    };
    const formData = ref({ ...INITIAL_DATA });
    const dataList = ref([]);
    const loading = ref(true);
    const formDialogVisible = ref(false);
    const tableListRef = ref();
    const selectInfo = ref({ show: false, tips: '' });
    const pagination = reactive<PaginationProps>({
        total: 0,
        pageSizes: [10, 30, 50],
        pageSize: 30,
        currentPage: 1,
    });
    const buttonClass = computed(() => {
        return ['text-gray_6', 'dark:!text-white', 'dark:hover:!text-primary'];
    });
    const columns: TableColumnList = getTableColums([
        {
            type: 'selection',
            width: 65,
        },
        {
            label: 'common.createdTime',
            prop: 'createdTime',
            minWidth: 200,
            formatter: dateTimeFormat,
        },
        {
            label: 'common.fileName',
            prop: 'fileName',
            minWidth: 120,
        },
        {
            label: 'common.type',
            prop: 'type',
            minWidth: 150,
        },
        {
            label: 'device.model',
            prop: 'deviceProfileName',
            minWidth: 100,
        },
        {
            label: 'ota.checkSum',
            prop: 'checksum',
            minWidth: 100,
        },
        {
            label: 'ota.distributeAll',
            prop: 'distributeStatus',
            minWidth: 150,
            cellRenderer: ({ row, props }) => {
                return <el-checkbox disabled={true} v-model={row.distributeStatus}></el-checkbox>;
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
    async function onSearch(
        queryParams = {
            pageSize: pagination.pageSize,
            page: 0,
            sortProperty: 'createdTime',
            sortOrder: 'DESC',
        },
    ) {
        loading.value = true;
        const { data, totalElements } = await getUserOtaPackageList(queryParams);
        dataList.value = data;
        pagination.total = totalElements;
        pagination.currentPage = queryParams.page + 1;
        setTimeout(() => {
            loading.value = false;
        }, 500);
    }
    function addOta() {
        formData.value = { ...INITIAL_DATA };
        formDialogVisible.value = true;
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
    async function handleDelete(row) {
        console.log(row);
        const title = $t('ota.sureDelete').replace('%s', row.title);
        const isConfirm = await confirmBox($t('ota.sureDeleteTips'), { title });
        if (isConfirm) {
            const otaPackageId = row.id.id;
            await deleteOtaPackage(otaPackageId);
            getPageInfo();
            message($t('status.success'));
        }
    }

    const resetForm = formEl => {
        if (!formEl) return;
        formEl.resetFields();
        onSearch();
    };
    const downloadFile = async row => {
        const response = await downloadOtaPakage(row.id.id);
        const { headers, data } = response;
        const filename = decodeURIComponent(headers.get('x-filename'));
        console.log(filename);
        const contentType = headers['content-type'];
        console.log(contentType);
        console.log(response.data);
        const linkElement = document.createElement('a');
        try {
            const blob = new Blob([data], { type: contentType });
            const url = URL.createObjectURL(blob);
            linkElement.setAttribute('href', url);
            linkElement.setAttribute('download', filename);
            const clickEvent = new MouseEvent('click', {
                view: window,
                bubbles: true,
                cancelable: false,
            });
            linkElement.dispatchEvent(clickEvent);
            return null;
        } catch (e) {
            throw e;
        }
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
        onSearch(queryParams);
    };
    let multipleSelection = [];
    function handleSelectionChange(val) {
        const count = val.length;
        const key = count > 1 ? 'ota.deletesSelTips' : 'ota.deleteSelTips';
        multipleSelection = val;
        selectInfo.value = {
            key,
            count,
            show: count > 0,
            tips: $t(key).replace('%s', count),
        };
    }
    const handleClick = row => {
        const { toggleRowSelection } = tableListRef.value.getTableRef();
        toggleRowSelection(row);
    };
    const deleteItems = async () => {
        const delCount = multipleSelection.length;
        const key = delCount > 1 ? 'ota.sureDeleteOtas' : 'ota.sureDeleteOta';
        const title = $t(key).replace('%s', delCount);
        const isConfirm = await confirmBox($t('ota.sureDeletesTips'), { title });
        if (isConfirm) {
            for (const item of multipleSelection) {
                const otaPackageId = item.id.id;
                await deleteOtaPackage(otaPackageId);
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
        loading,
        columns,
        dataList,
        pagination,
        buttonClass,
        onSearch,
        resetForm,
        formDialogVisible,
        updateTable,
        addOta,
        downloadFile,
        handleDelete,
        handleClick,
        handleSizeChange,
        handleCurrentChange,
        handleSelectionChange,
    };
}
