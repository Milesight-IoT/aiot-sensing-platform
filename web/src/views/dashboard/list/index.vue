<!--
 * @Author: lin@milesight.com
 * @Date: 2023-02-06 20:50:40
 * @LastEditTime: 2023-06-12 20:03:18
 * @LastEditAuthor: sreindy8224
-->
<script setup lang="ts">
import { useDashboard } from './hook';
import { PureTableBar } from '@/components/RePureTableBar';
import dialogForm from '../components/dialog/DialogForm.vue';
import rightDialog from '../components/dialog/rightDialog.vue';
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
    onSearch,
    formData,
    detailData,
    formDialogVisible,
    handleAdd,
    showDetail,
    copyLink,
    getPublicLink,
    handleDelete,
    deleteItems,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
    updateTable,
    isDefault,
    gotoLayout,
} = useDashboard();
</script>

<template>
    <div class="main">
        <PureTableBar
            :title="$t('menus.dashboards')"
            @refresh="onSearch"
            :showSearch="true"
            @search="searchName"
            :selectInfo="selectInfo"
            @delete="deleteItems"
        >
            <template #buttons>
                <el-tooltip effect="dark" :content="$t('buttons.add')" placement="top">
                    <i class="text-2xl icon-add btn-icon-bg" @click="handleAdd" />
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
                    @row-click="gotoLayout"
                    :pagination="pagination"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :highlight-current-row="true"
                >
                    <template #operation="{ row }">
                        <i
                            class="text-xl icon-copy-link btn-icon"
                            @click.stop="copyLink(getPublicLink(row))"
                            v-show="!isDefault(row) && row.assignedCustomers && row.assignedCustomers[0]?.public"
                        />
                        <i class="text-xl icon-data btn-icon" @click.stop="showDetail(row)" />
                        <i
                            class="text-2xl icon-delete btn-icon"
                            @click.stop="handleDelete(row)"
                            v-show="!isDefault(row)"
                        />
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
