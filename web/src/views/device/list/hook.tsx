import { message, confirmBox } from '@/utils/message';
import { getUserDeviceList, getSensingObjectsByDeviceId, deleteDevice } from '@/api/device';
import { type PaginationProps } from '@pureadmin/table';
import { reactive, ref, computed, onMounted } from 'vue';
import { $t } from '@/plugins/i18n';
import { dateTimeFormat, getTableColums } from '@/utils/tools';
import { useRouter } from 'vue-router';
import { emitter } from '@/utils/mitt';
export function useDevice() {
    const router = useRouter();
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
    const columns: TableColumnList = getTableColums([
        {
            type: 'selection',
            width: 65,
        },
        {
            label: 'common.createdTime',
            prop: 'createdTime',
            minwidth: 90,
            formatter: dateTimeFormat,
        },
        {
            label: 'common.name',
            prop: 'name',
            minwidth: 90,
        },
        {
            label: 'device.model',
            prop: 'type',
            minWidth: 150,
        },
        {
            label: 'device.sn',
            prop: 'credentialsId',
            minWidth: 150,
        },
        {
            label: 'common.status',
            prop: 'active',
            minWidth: 150,
            cellRenderer: ({ row, props }) => {
                let res;
                switch (row.active) {
                    case true:
                        res = { className: 'text-success', label: $t('status.active') };
                        break;
                    case false:
                        res = { className: 'text-danger', label: $t('status.inactive') };
                        break;
                }
                return <span class={res.className}>{res.label}</span>;
            },
        },
        {
            fixed: 'right',
            align: 'right',
            width: 90,
            slot: 'operation',
        },
    ]);
    const selectInfo = ref({ show: false, tips: '' });
    const formDialogVisible = ref(false);
    const formData = ref({});
    function addDevice() {
        formData.value = {
            name: '',
            deviceProfileId: '',
            credentialsId: '',
        };
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
    const checkSensingObjectById = async (deviceId: String, deviceName: String) => {
        const sensingData = await getSensingObjectsByDeviceId(deviceId);
        if (sensingData.length) {
            const sureTitle = $t('device.sureDeleteSensing');
            const objectStrs = Array.from(
                new Set(
                    sensingData.map(item => {
                        return `'${item.name}'`;
                    }),
                ),
            ).join(', ');
            const title = sureTitle.replace('%s', deviceName).replace('%s1', objectStrs);
            const isConfirm = await confirmBox($t('device.deleteSensingTips'), {
                title,
                cancelButtonText: $t('buttons.ok'),
                confirmButtonText: $t('device.jumpToObj'),
                confirmButtonClass: 'inter-btn',
            });
            if (isConfirm) {
                router.push('/objects');
            }
            return true;
        }
    };
    const deleteSingleDevice = async item => {
        const deviceId = item.id.id;
        const deviceName = item.name;
        const hasSensingObj = await checkSensingObjectById(deviceId, deviceName);
        if (!hasSensingObj) {
            const sureTitle = $t('device.sureDelete').replace('%s', deviceName);
            const isConfirm = await confirmBox($t('device.deleteTips'), {
                title: sureTitle,
            });
            if (isConfirm) {
                await deleteDevice(deviceId);
                message($t('status.success'));
                getPageInfo();
            }
        }
    };
    function handleDelete(row) {
        deleteSingleDevice(row);
    }
    let multipleSelection = [];
    function handleSelectionChange(val) {
        const count = val.length;
        const key = count > 1 ? 'device.deletesSelTips' : 'device.deleteSelTips';
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
        const key = delCount > 1 ? 'device.sureDelDevices' : 'device.sureDelDevice';
        const title = $t(key).replace('%s', delCount);
        const isConfirm = await confirmBox($t('device.sureDelDevicesTips'), {
            title,
        });
        if (isConfirm) {
            const deviceArr = [];
            for (const item of multipleSelection) {
                const deviceId = item.id.id;
                const sensingData = await getSensingObjectsByDeviceId(deviceId);
                if (sensingData.length) {
                    deviceArr.push(item.name);
                } else {
                    await deleteDevice(deviceId);
                }
            }
            if (deviceArr.length) {
                let content = '';
                deviceArr.map(item => {
                    content += `<span class="ml-2 el-tag">${item}</span>`;
                });
                const isConfirm = await confirmBox(content, {
                    title: $t('device.delItemsFailed'),
                    cancelButtonText: $t('buttons.ok'),
                    confirmButtonText: $t('device.jumpToObj'),
                    confirmButtonClass: 'inter-btn',
                    dangerouslyUseHTMLString: true,
                });
                if (isConfirm) {
                    router.push('/objects');
                }
            } else {
                message($t('status.success'));
            }
            const hasItem = delCount - deviceArr.length;
            getPageInfo(hasItem);
            const { clearSelection } = tableListRef.value.getTableRef();
            clearSelection();
            selectInfo.value.show = false;
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
        const { data, totalElements } = await getUserDeviceList(queryParams);
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
        formData.value = Object.assign({}, row);
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
        buttonClass,
        onSearch,
        searchName,
        resetForm,
        formDialogVisible,
        updateTable,
        addDevice,
        showDetail,
        handleDelete,
        deleteItems,
        handleSizeChange,
        handleCurrentChange,
        handleSelectionChange,
    };
}
