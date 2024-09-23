import Axios, { AxiosInstance, AxiosRequestConfig, CustomParamsSerializer } from 'axios';
import { PureHttpError, RequestMethods, PureHttpResponse, PureHttpRequestConfig } from './types.d';
import { stringify } from 'qs';
import NProgress from '../progress';
import { getToken, formatToken, getRefreshToken } from '@/utils/auth';
import { useUserStoreHook } from '@/store/modules/user';

// 相关配置请参考：www.axios-js.com/zh-cn/docs/#axios-request-config-1
const defaultConfig: AxiosRequestConfig = {
    // 请求超时时间
    timeout: 10000,
    headers: {
        Accept: 'application/json, text/plain, */*',
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
    },
    // 数组格式参数序列化（https://github.com/axios/axios/issues/5142）
    paramsSerializer: {
        serialize: stringify as unknown as CustomParamsSerializer,
    },
};

class PureHttp {
    constructor() {
        this.httpInterceptorsRequest();
        this.httpInterceptorsResponse();
    }

    /** token过期后，暂存待执行的请求 */
    private static requests = [];

    /** 防止重复刷新token */
    private static isRefreshing = false;

    /** 初始化配置对象 */
    private static initConfig: PureHttpRequestConfig = {};

    /** 保存当前Axios实例对象 */
    private static axiosInstance: AxiosInstance = Axios.create(defaultConfig);

    /** 重连原始请求 */
    private static retryOriginalRequest(config: PureHttpRequestConfig) {
        return new Promise(resolve => {
            PureHttp.requests.push((token: string) => {
                config.headers['Authorization'] = formatToken(token);
                resolve(config);
            });
        });
    }

    /** 请求拦截 */
    private httpInterceptorsRequest(): void {
        PureHttp.axiosInstance.interceptors.request.use(
            async (config: PureHttpRequestConfig) => {
                // 开启进度条动画
                if (!config.noProgress) NProgress.start();
                // 优先判断post/get等方法是否传入回掉，否则执行初始化设置等回掉
                if (typeof config.beforeRequestCallback === 'function') {
                    config.beforeRequestCallback(config);
                    return config;
                }
                if (PureHttp.initConfig.beforeRequestCallback) {
                    PureHttp.initConfig.beforeRequestCallback(config);
                    return config;
                }
                // 设置合法响应码
                config.validateStatus = status => {
                    console.log('请求状态码status: ', status);
                    // 设置状态码为401时合法不会报错以便响应拦截
                    return status === 200 || status === 300 || status === 401;
                };
                /** 请求白名单，放置一些不需要token的接口（通过设置请求白名单，防止token过期后再请求造成的死循环问题） */
                const whiteList = ['/api/auth/token', '/login'];
                const ignoreToken = whiteList.some(v => config.url.indexOf(v) > -1);
                if (ignoreToken) {
                    PureHttp.isRefreshing = false;
                }
                return ignoreToken
                    ? config
                    : new Promise(resolve => {
                        const data = getToken();
                        //data.expires = new Date().getTime() + 3600;
                        if (data) {
                            console.log('accessToken ====', formatToken(data.accessToken));
                            config.headers['Authorization'] = formatToken(data.accessToken);
                            resolve(config);
                        } else {
                            resolve(config);
                        }
                    });
            },
            error => {
                console.log(error)
                return Promise.reject(error);
            },
        );
    }

    /** 响应拦截 */
    private httpInterceptorsResponse(): void {
        const instance = PureHttp.axiosInstance;
        instance.interceptors.response.use(
            (response: PureHttpResponse) => {
                const $config = response.config;
                // 关闭进度条动画
                if (!$config.noProgress) NProgress.done();
                // 优先判断post/get等方法是否传入回掉，否则执行初始化设置等回掉
                if (typeof $config.beforeResponseCallback === 'function') {
                    $config.beforeResponseCallback(response);
                    return response.data;
                }
                if (PureHttp.initConfig.beforeResponseCallback) {
                    PureHttp.initConfig.beforeResponseCallback(response);
                    return response.data;
                }
                if ($config.resHeaders) {
                    return {
                        data: response.data,
                        headers: response.headers,
                    };
                }
                if (response.status === 401) {
                    const whiteList = ['/api/auth/token'];
                    const ignoreToken = whiteList.some(v => $config.url.indexOf(v) > -1);
                    if (ignoreToken) {
                        return useUserStoreHook().logOut();
                    }
                    const data = getRefreshToken();
                    if (!PureHttp.isRefreshing && data) {
                        PureHttp.isRefreshing = true;
                        // token过期刷新
                        //console.log('token time out======');
                        useUserStoreHook()
                            .handRefreshToken({ refreshToken: data.refreshToken })
                            .then(res => {
                                const token = res.token;
                                $config.headers['Authorization'] = formatToken(token);
                                PureHttp.requests.forEach(cb => cb(token));
                                PureHttp.requests = [];
                                PureHttp.isRefreshing = false;
                            })
                            .finally(() => {
                                PureHttp.isRefreshing = false;
                            });
                    } else {
                        //useUserStoreHook().logOut();
                        PureHttp.isRefreshing = true;
                        return response.data
                    }
                    return PureHttp.retryOriginalRequest($config);
                }
                return response.data;
            },
            (error: PureHttpError) => {
                const $error = error;
                $error.isCancelRequest = Axios.isCancel($error);
                // 关闭进度条动画
                NProgress.done();
                if ($error.response.status == 403) {
                    useUserStoreHook().logOut();
                }
                // 所有的响应异常 区分来源为取消请求/非取消请求
                return Promise.reject($error);
            },
        );
    }

    /** 通用请求工具函数 */
    public request<T>(
        method: RequestMethods,
        url: string,
        param?: AxiosRequestConfig,
        axiosConfig?: PureHttpRequestConfig,
    ): Promise<T> {
        const config = {
            method,
            url,
            ...param,
            ...axiosConfig,
        } as PureHttpRequestConfig;

        // 单独处理自定义请求/响应回掉
        return new Promise((resolve, reject) => {
            PureHttp.axiosInstance
                .request(config)
                .then((response: undefined) => {
                    resolve(response);
                })
                .catch(error => {
                    reject(error);
                });
        });
    }

    /** 单独抽离的post工具函数 */
    public post<T, P>(url: string, params?: AxiosRequestConfig<T>, config?: PureHttpRequestConfig): Promise<P> {
        return this.request<P>('post', url, params, config);
    }

    /** 单独抽离的get工具函数 */
    public get<T, P>(url: string, params?: AxiosRequestConfig<T>, config?: PureHttpRequestConfig): Promise<P> {
        return this.request<P>('get', url, params, config);
    }
}

export const http = new PureHttp();
