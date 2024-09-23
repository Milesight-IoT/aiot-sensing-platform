/*
 * @Author: Lin
 * @Date: 2023-02-13 16:50:39
 * @LastEditTime: 2023-06-05 19:21:22
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

/** 获取Rule规则信息列表 */
const getRuleList = (params?: object) => {
    return http.request<ListResult>('get', '/api/ruleChainOwn/ruleChains', { params });
};

/**
 * @description: 根据Rule规则名称查找Rule规则
 * @param {string} name Rule规则名称
 * @return {*} Rule规则信息
 */
const getRuleByName = (name: string) => {
    const params = { name };
    return http.request('get', `/api/ruleChainOwn/detailsByName`, { params });
};

/** 添加或者更新Rule规则 */
const addRule = (data?: object) => {
    return http.post(`/api/ruleChainOwn/createOrUpdate`, { data });
};

/** 删除Rule规则 */
const deleteRule = (ruleChainOwnId: String) => {
    return http.request('delete', `/api/ruleChainOwn/${ruleChainOwnId}`);
};

const getRuleListByRecipient = (recipientId: String) => {
    return http.request('get', `/api/ruleChainOwn/findRuleChainOwnByRecipientId/${recipientId}`);
};

export { getRuleList, addRule, deleteRule, getRuleByName, getRuleListByRecipient };
