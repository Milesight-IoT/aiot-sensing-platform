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
/** 根据名称获取感知对象 */
const getSensingObjectByName = (name: String) => {
    const params = { name };
    return http.request('get', `/api/sensingObjectByName`, { params });
};
/** 获取感知对象列表 */
const getSensingObjectList = (params?: object) => {
    return http.request<ListResult>('get', '/api/sensingObjects', { params });
};

/** 添加或者更新感知对象 */
const addSensingObject = (data?: object) => {
    return http.post<ListResult>(`/api/sensingObject`, { data });
};

/** 删除感知对象 */
const deleteSensingObject = (SensingObjectId: String) => {
    return http.request('delete', `/api/sensingObject/${SensingObjectId}`);
};

/**  获取历史遥测数据 */
const getHistroyTelemetryList = (params?: object) => {
    return http.request<ListResult>('get', 'api/plugins/telemetry/values/timeseries', { params });
};
/** 获取设备能力关联的感知对象 */
const getSensingObjectByAbilityId = (deviceAbilityId: String) => {
    return http.request('get', `/api/sensingObjectsByAbilityId/${deviceAbilityId}`);
};

const getObejctImageChannel = (params?: object) => {
    return http.request('get', `/api/sensingObjectImages`, { params });
};

const getSensingObjectById = (params?: object) => {
    return http.request('get', '/api/sensingObjectsByIds', { params })
}
const getSensingObjectSelectList = (params?: object) => {
    return http.request('get', '/api/sensingObjectSelectList', { params })
}

export {
    getSensingObjectList,
    addSensingObject,
    deleteSensingObject,
    getHistroyTelemetryList,
    getSensingObjectByAbilityId,
    getSensingObjectByName,
    getObejctImageChannel,
    getSensingObjectById,
    getSensingObjectSelectList
};
