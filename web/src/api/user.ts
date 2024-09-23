import { http } from '@/utils/http';

type UserResult = {
    /** `token` */
    token: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
};

type RefreshTokenResult = {
    /** `token` */
    token: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
};

/** 登录 */
const getLogin = (data?: object) => {
    return http.request<UserResult>('post', '/api/auth/login', { data });
};

/** 刷新token */
const refreshTokenApi = (data?: object) => {
    return http.request<RefreshTokenResult>('post', '/api/auth/token', { data });
};

const changePwd = (data: object) => {
    return http.request('post', '/api/auth/changePassword', { data });
};
const checkPwdMatch = (password: string) => {
    const params = {
        password,
    };
    return http.request('get', '/api/auth/existsPassword', { params });
};
/** 获取公开连接的token信息 */
const getPublicToken = (publicId) => {
    return http.request<UserResult>('post', '/api/auth/login/public', { data: { publicId } });
}
export { changePwd, checkPwdMatch, getLogin, refreshTokenApi, RefreshTokenResult, UserResult, getPublicToken };
