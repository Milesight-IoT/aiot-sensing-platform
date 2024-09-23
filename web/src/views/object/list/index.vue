<!--
 * @Author: your name
 * @Date: 2023-02-06 20:50:40
 * @LastEditTime: 2023-04-24 10:46:38
 * @Descripttion: your project
-->
<script setup lang="ts">
import { ref } from 'vue';
import { useSensingObject } from './hook';
import { PureTableBar } from '@/components/RePureTableBar';
import dialogForm from '../components/DialogForm.vue';
import rightDialog from '../components/rightDialog.vue';

defineOptions({
    name: 'SensingObject',
});
const {
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
    onSearch,
    addObject,
    resetForm,
    updateTable,
    showDetail,
    handleDelete,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
} = useSensingObject();
</script>

<template>
    <div class="main">
        <PureTableBar
            :title="$t('object.list')"
            @refresh="onSearch"
            :showSearch="true"
            @search="searchName"
            :selectInfo="selectInfo"
            @delete="deleteItems"
        >
            <template #buttons>
                <el-tooltip effect="dark" :content="$t('buttons.add')" placement="top">
                    <i class="icon-add text-2xl btn-icon-bg" @click="addObject" />
                </el-tooltip>
            </template>
            <template v-slot="{ size, checkList }">
                <pure-table
                    showOverflowTooltip
                    ref="tableListRef"
                    table-layout="fixed"
                    :loading="loading"
                    :size="size"
                    :data="dataList"
                    :columns="columns"
                    :checkList="checkList"
                    :pagination="pagination"
                    :paginationSmall="size === 'small' ? true : false"
                    @selection-change="handleSelectionChange"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :highlight-current-row="true"
                    @row-click="showDetail"
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
