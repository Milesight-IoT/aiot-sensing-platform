<!--
 * @Author: your name
 * @Date: 2023-02-06 20:50:40
 * @LastEditTime: 2023-08-15 19:02:28
 * @Descripttion: your project
-->
<script setup lang="ts">
import { ref } from 'vue';
import { useDevice } from './hook';
import { PureTableBar } from '@/components/RePureTableBar';
import dialogForm from '../components/DialogForm.vue';

defineOptions({
    name: 'OtaUpdate',
});

const {
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
    addOta,
    downloadFile,
    handleDelete,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
    updateTable,
    handleClick,
} = useDevice();
</script>

<template>
    <div class="main">
        <PureTableBar
            :title="$t('ota.packageRepos')"
            @refresh="onSearch"
            :showSearch="true"
            @search="searchName"
            :selectInfo="selectInfo"
            @delete="deleteItems"
        >
            <template #buttons>
                <el-tooltip effect="dark" :content="$t('buttons.add')" placement="top">
                    <i class="icon-add text-2xl btn-icon-bg" @click="addOta" />
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
                    @row-click="handleClick"
                    @selection-change="handleSelectionChange"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                >
                    <template #operation="{ row }">
                        <i class="icon-download text-2xl btn-icon" @click.native.stop="downloadFile(row)" />
                        <i class="icon-delete text-2xl btn-icon" @click.native.stop="handleDelete(row)" />
                    </template>
                </pure-table>
            </template>
        </PureTableBar>
        <dialogForm v-model:visible="formDialogVisible" :data="formData" @updateTable="onSearch" />
    </div>
</template>

<style scoped lang="scss">
:deep(.el-dropdown-menu__item i) {
    margin: 0;
}
</style>
