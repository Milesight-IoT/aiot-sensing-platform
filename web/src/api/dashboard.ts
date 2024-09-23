import { http } from '@/utils/http';
import { useUserStoreHook } from '@/store/modules/user';
type ListResult = {
    /** 列表数据是否有下一页 */
    hasNext: boolean;
    /** 列表数据 */
    data: Array<any>;
    /** 总数 */
    totalElements?: number;
    totalPages?: number;
};
const getSystemParams = () => {
    return http.get<any, ListResult>(`/api/system/params`);
};
const getPublicDashboardList = (publicId, params?: object) => {
    return http.get<any, ListResult>(`/api/customer/${publicId}/dashboards`, { params });
};
const getDashboardList = (params?: object) => {
    return http.get<any, ListResult>(`/api/sensing/tenant/dashboards`, { params });
};

const getDashboardDefault = () => {
    return http.get(`/api/sensing/getDefaultId`);
};

const updateDashboard = (params: object) => {
    return http.post(`/api/sensing/dashboard`, { params });
};

const getDashboardById = (id: string) => {
    return http.get(`/api/dashboard/${id}`);
};
/**
 * @description: 判断仪表板名称是否存在
 * @param {string} name 仪表板名称
 * @return {*} 是否存在
 */
const checkDashboardByName = (dashboardName: string) => {
    const params = { dashboardName };
    return http.request('get', `/api/sensing/checkDashboardName`, { params });
};

/** 删除仪表盘 */
const deleteDashboard = (params: string) => {
    return http.request('delete', `/api/dashboard/${params}`);
};

const updateWidget = (data: object) => {
    return http.post(`/api/sensing/dashboard/widgets`, { data });
};
export {
    getSystemParams,
    getPublicDashboardList,
    getDashboardList,
    updateDashboard,
    getDashboardById,
    deleteDashboard,
    updateWidget,
    getDashboardDefault,
    checkDashboardByName
};
