<script setup lang="ts">
import { useDevice } from './hook';
import { PureTableBar } from '@/components/RePureTableBar';
import dialogForm from '../components/DialogForm.vue';
import rightDialog from '../components/rightDialog.vue';
defineOptions({
    name: 'Device',
});

const {
    selectInfo,
    tableListRef,
    searchName,
    loading,
    columns,
    dataList,
    pagination,
    buttonClass,
    onSearch,
    resetForm,
    formData,
    formDialogVisible,
    addDevice,
    showDetail,
    handleDelete,
    deleteItems,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
    updateTable,
} = useDevice();
</script>

<template>
    <div class="main">
        <PureTableBar
            :title="$t('device.list')"
            @refresh="onSearch"
            :showSearch="true"
            @search="searchName"
            :selectInfo="selectInfo"
            @delete="deleteItems"
        >
            <template #buttons>
                <el-tooltip effect="dark" :content="$t('buttons.add')" placement="top">
                    <i class="icon-add text-2xl btn-icon-bg" @click="addDevice" />
                </el-tooltip>
            </template>
            <template v-slot="{ checkList }">
                <pure-table
                    showOverflowTooltip
                    ref="tableListRef"
                    table-layout="fixed"
                    :loading="loading"
                    :data="dataList"
                    :columns="columns"
                    :checkList="checkList"
                    @selection-change="handleSelectionChange"
                    @row-click="showDetail"
                    :pagination="pagination"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :highlight-current-row="true"
                >
                    <template #operation="{ row }">
                        <i class="icon-data text-2xl btn-icon" @click="showDetail(row)" />
                        <i class="icon-delete text-2xl btn-icon" @click.native.stop="handleDelete(row)" />
                    </template>
                </pure-table>
            </template>
        </PureTableBar>
        <dialogForm v-model:visible="formDialogVisible" :data="formData" @updateTable="onSearch" />
        <rightDialog :data="formData" @updateTable="updateTable" />
    </div>
</template>

<style scoped lang="scss">
:deep(.el-dropdown-menu__item i) {
    margin: 0;
}
</style>
