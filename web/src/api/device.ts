/*
 * @Author: your name
 * @Date: 2023-02-06 20:50:39
 * @LastEditTime: 2023-06-27 08:54:28
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
/**
 * @description: 根据设备名称查找设备
 * @param {string} name 设备名称
 * @return {*} 设备信息
 */
const getDeviceByName = (name: string) => {
    const params = { name };
    return http.request('get', `/api/deviceByName`, { params });
};
/**
 * @description: 根据设备SN查找设备
 * @param {string} credentialsId 设备SN
 * @return {*} 设备信息
 */
const getDeviceBycredentialsId = (credentialsId: string) => {
    const params = { credentialsId };
    return http.request('get', `/api/deviceByCredentialsId`, { params });
};
/** 添加或者更新设备 */
const addDevice = (data?: object, id?: string) => {
    return http.post<ListResult>(`/api/device?credentialsId=${id}`, { data });
};
/** 获取设备关联的感知对象 */
const getSensingObjectsByDeviceId = (deviceId: String) => {
    return http.request('get', `/api/sensingObjectsByDeviceId/${deviceId}`);
};
/** 删除设备 */
const deleteDevice = (deviceId: String) => {
    return http.request('delete', `/api/device/${deviceId}`);
};

/** 获取设备关联的感知对象 */
const getLast = (params?: object) => {
    return http.request<ListResult>('get', `/api/plugins/telemetry/values/timeseries`, { params });
};

/** 获取最新遥测数据 */
const getLastTelemetry = (entityId, params?: object) => {
    return http.request('get', `api/plugins/telemetry/DEVICE/${entityId}/values/timeseries`, { params });
};
const checkSN = (sn: string) => {
    const params = { sn };
    return http.request('get', `api/checkSN`, { params });
};
const getDeviceListById = (params?: object) => {
    return http.request('get', '/api/devices', { params })
}
export {
    checkSN,
    getUserDeviceList,
    getDeviceProfileList,
    addDevice,
    getSensingObjectsByDeviceId,
    deleteDevice,
    getLastTelemetry,
    getDeviceByName,
    getDeviceBycredentialsId,
    getDeviceListById
};
