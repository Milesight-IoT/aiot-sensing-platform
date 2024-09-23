<!--
 * @Author: lin@milesight.com
 * @Date: 2023-02-06 20:50:40
 * @LastEditTime: 2023-07-11 11:37:27
 * @LastEditAuthor: sreindy8224
-->
<script setup lang="ts">
import { useRule } from './hook';
import { PureTableBar } from '@/components/RePureTableBar';
import dialogForm from '../components/DialogForm.vue';
import rightDialog from '../components/rightDialog.vue';
defineOptions({
    name: 'Rule',
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
    detailData,
    formDialogVisible,
    addRule,
    showDetail,
    handleDelete,
    deleteItems,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
    updateTable,
} = useRule();
</script>

<template>
    <div class="main">
        <PureTableBar
            :title="$t('rules.list')"
            @refresh="onSearch"
            :showSearch="true"
            @search="searchName"
            :selectInfo="selectInfo"
            @delete="deleteItems"
        >
            <template #buttons>
                <el-tooltip effect="dark" :content="$t('buttons.add')" placement="top">
                    <i class="text-2xl icon-add btn-icon-bg" @click="addRule" />
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
                        <i class="text-xl icon-data btn-icon" @click.stop="showDetail(row)" />
                        <i class="text-2xl icon-delete btn-icon" @click.native.stop="handleDelete(row)" />
                    </template>
                </pure-table>
            </template>
        </PureTableBar>
        <dialogForm v-model:visible="formDialogVisible" :data="formData" @updateTable="onSearch" />
        <rightDialog :data="detailData" @updateTable="updateTable" />
    </div>
</template>

<style scoped lang="scss">
:deep(.el-dropdown-menu__item i) {
    margin: 0;
}
</style>
