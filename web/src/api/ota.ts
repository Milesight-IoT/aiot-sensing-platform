/*
 * @Author: your name
 * @Date: 2023-02-06 20:50:39
 * @LastEditTime: 2023-04-24 10:49:50
 * @Descripttion: your project
 */
import { http } from '@/utils/http';
import { config } from 'process';

type ListResult = {
    /** 列表数据是否有下一页 */
    hasNext: boolean;
    /** 列表数据 */
    data: Array<any>;
    /** 总数 */
    totalElements?: number;
    totalPages?: number;
};

/** 获取租户OTA信息列表 */
const getUserOtaPackageList = (params?: object) => {
    return http.request<ListResult>('get', '/api/otaPackages', { params });
};

/** 添加或者更新OTA */
const addOtaPackage = (data?: object) => {
    return http.post<ListResult>(`/api/otaPackage`, { data });
};

/** 添加或者更新OTA文件 */
const addOtaPackageFile = (data?: object, otaPackageId?: String) => {
    const headers = {
        'Content-Type': 'multipart/form-data; boundary=------------------------a7233c91f0556e2f',
    };
    const timeout = 1000 * 60 * 2; //2min超时时间
    return http.post<ListResult>(`/api/otaPackage/${otaPackageId}`, { data, headers, timeout });
};

/** 获取设备型号管理的OTA列表 */
const getOtaPackagesBydeviceProfileId = (deviceProfileId: string, type: String, params?: object) => {
    return http.request<ListResult>('get', `/api/otaPackages/${deviceProfileId}/${type}`, { params });
};
/** 删除OTA */
const deleteOtaPackage = (otaPackageId: String) => {
    return http.request('delete', `/api/otaPackage/${otaPackageId}`);
};

/** 获取设备OTA升级状态 */
const getStatesByDeviceId = deviceId => {
    return http.request('get', `/api/plugins/telemetry/values/timeseries`, { deviceId });
};

/** 下周ota文件包 */
const downloadOtaPakage = (otaPackageId: string) => {
    const timeout = 1000 * 60 * 2; //2min超时时间
    return http.request('get', `api/otaPackage/${otaPackageId}/download`, {
        responseType: 'arraybuffer',
        resHeaders: true,
        timeout,
    });
};
const getOtaByDeviceId = (deviceId: String) => {
    return http.request('get', `/api/device/otaPackage/${deviceId}`);
};
/**根据文件名获取ota信息 */
const getOtaByTitle = (title: string) => {
    return http.request('get', `api/otaPackageByTitle?title=${title}`);
};
export {
    getUserOtaPackageList,
    downloadOtaPakage,
    addOtaPackage,
    addOtaPackageFile,
    getOtaPackagesBydeviceProfileId,
    getStatesByDeviceId,
    deleteOtaPackage,
    getOtaByDeviceId,
    getOtaByTitle,
};
