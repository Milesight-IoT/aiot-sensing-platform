/*
 * @Author: your name
 * @Date: 2023-02-02 13:59:00
 * @LastEditTime: 2023-08-21 21:38:13
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

/** 获取租户设备信息列表 */
const getUserDeviceList = (params?: object) => {
    return http.request<ListResult>('get', '/api/tenant/deviceInfos', { params });
};

/** 获取设备类型列表 */
const getDeviceProfileList = (params?: object) => {
    return http.request<ListResult>('get', '/api/deviceProfileInfos', { params });
};

/** 添加或者更新设备 */
const addDeviceAbility = (data?: object) => {
    return http.post<ListResult>(`/api/deviceAbility`, { data });
};
/** 获取设备能力列表 */
const getAbilityByDeviceId = (params?: object, noProgress?: boolean) => {
    return http.request<ListResult>('get', `/api/deviceAbilities`, { params, noProgress });
};
/** 删除设备能力 */
const deleteDeviceAbility = (deviceAbilityId: String) => {
    return http.request('delete', `/api/deviceAbility/${deviceAbilityId}`);
};

/** 获取设备关联的感知对象 */
const getLast = (params?: object) => {
    return http.request<ListResult>('get', `/api/plugins/telemetry/values/timeseries`, { params });
};

/** 获取最新遥测数据 */
const getLastTelemetry = (entityId, params?: object) => {
    return http.request('get', `api/plugins/telemetry/DEVICE/${entityId}/values/timeseries`, { params });
};
/** 获取遥测数据圖片 */
const getTelemetryImg = (params?: object) => {
    return http.request('get', `/api/plugins/telemetry/values/image`, { params });
};
const setTelmetryRecognition = (data?: Object) => {
    return http.request('post', `/api/plugins/telemetry/recognition`, { data });
};
const getRoiAbility = (params?: object) => {
    return http.request('get', `/api/selectRoiAbilities`, { params });
}
const getSelRoiAbility = (params?: object) => {
    return http.request('get', `/api/detailsRoiAbilities`, { params });
}
export {
    addDeviceAbility,
    getUserDeviceList,
    getDeviceProfileList,
    getAbilityByDeviceId,
    deleteDeviceAbility,
    getLastTelemetry,
    getTelemetryImg,
    setTelmetryRecognition,
    getRoiAbility,
    getSelRoiAbility
};
