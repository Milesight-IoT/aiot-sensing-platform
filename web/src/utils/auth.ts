import Cookies from 'js-cookie';
import { storageSession } from '@pureadmin/utils';
import { useUserStoreHook } from '@/store/modules/user';

export interface DataInfo<T> {
    /** token */
    token?: string;
    accessToken?: string;
    /** 用于调用刷新accessToken的接口时所需的token */
    refreshToken: string;
    /** 用户名 */
    username?: string;
    /** 当前登陆用户的角色 */
    roles?: Array<string>;
}

export const sessionKey = 'user-info';
export const TokenKey = 'authorized-token';

/** 获取`token` */
export function getToken(): DataInfo<number> {
    // 此处与`TokenKey`相同，此写法解决初始化时`Cookies`中不存在`TokenKey`报错
    console.log(Cookies.get(TokenKey))
    return Cookies.get(TokenKey) ? JSON.parse(Cookies.get(TokenKey)) : storageSession().getItem(sessionKey);
}

/** 获取`refreshtoken` */
export function getRefreshToken(): DataInfo<number> {
    // 此处与`TokenKey`相同，此写法解决初始化时`Cookies`中不存在`TokenKey`报错
    return storageSession().getItem(sessionKey);
}

/**
 * @description 设置`token`以及一些必要信息并采用无感刷新`token`方案
 * 无感刷新：后端返回`accessToken`（访问接口使用的`token`）、`refreshToken`（用于调用刷新`accessToken`的接口时所需的`token`，`refreshToken`的过期时间（比如30天）应大于`accessToken`的过期时间（比如2小时））、`expires`（`accessToken`的过期时间）
 * 将`accessToken`、`expires`这两条信息放在key值为authorized-token的cookie里（过期自动销毁）
 * 将`username`、`roles`、`refreshToken`、`expires`这四条信息放在key值为`user-info`的sessionStorage里（浏览器关闭自动销毁）
 */
export function setToken(data: DataInfo<Date>) {
    const { token, refreshToken, accessToken } = data;
    const cookieString = JSON.stringify({ accessToken: token || accessToken });

    Cookies.set(TokenKey, cookieString);

    function setSessionKey(username: string, roles: Array<string>) {
        useUserStoreHook().SET_USERNAME(username);
        useUserStoreHook().SET_ROLES(roles);
        storageSession().setItem(sessionKey, {
            refreshToken,
            username,
            roles,
        });
    }

    if (data.username && data.roles) {
        const { username, roles } = data;
        setSessionKey(username, roles);
    } else {
        const username = storageSession().getItem<DataInfo<number>>(sessionKey)?.username ?? '';
        const roles = storageSession().getItem<DataInfo<number>>(sessionKey)?.roles ?? [];
        setSessionKey(username, roles);
    }
}

/** 删除`token`以及key值为`user-info`的session信息 */
export function removeToken() {
    console.log('remove Token ====')
    Cookies.remove(TokenKey);
    sessionStorage.clear();
}

/** 格式化token（jwt格式） */
export const formatToken = (token: string): string => {
    console.log(token)
    return 'Bearer ' + token;
};
