/*
 * @Author: Lin
 * @Date: 2023-02-13 16:50:39
 * @LastEditTime: 2023-06-27 08:52:03
 * @Descripttion: your project
 */
import { http } from '@/utils/http';

type ListResult = {
    /** 列表数据是否有下一页 */
    hasNext: boolean;
    /** 列表数据 */
    data: Array<any>;
    /** 总数 */
    totalElements?: number;
    totalPages?: number;
};

/** 获取租户Recipient规则信息列表 */
const getRecipientList = (params?: object) => {
    return http.request<ListResult>('get', '/api/recipients/findRecipientsList', { params });
};

/**
 * @description: 根据Recipient规则名称查找Recipient规则
 * @param {string} name Recipient规则名称
 * @return {*} Recipient规则信息
 */
const getRecipientByName = (name: string) => {
    const params = { name };
    return http.request('get', `/api/recipients/detailsByName`, { params });
};

/** 添加或者更新Recipient规则 */
const addRecipient = (data?: object) => {
    return http.post(`/api/recipients/createOrUpdate`, { data });
};

/** 删除Recipient规则 */
const deleteRecipient = (recipientsId: String) => {
    return http.request('delete', `/api/recipients/${recipientsId}`);
};

const getRecipientListById = (params?: object) => {
    return http.request('get', '/api/recipients/recipientsIds', { params })
}

export { getRecipientList, addRecipient, deleteRecipient, getRecipientByName, getRecipientListById };
