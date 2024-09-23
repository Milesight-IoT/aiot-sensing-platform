/*
 * @Author: your name
 * @Date: 2023-01-13 13:43:06
 * @LastEditTime: 2023-11-03 11:19:26
 * @Descripttion: your project
 */
import { defineStore } from 'pinia';
import { store } from '@/store';
import { userType } from './types';
import { routerArrays } from '@/layout/types';
import { router, resetRouter } from '@/router';
import { storageSession } from '@pureadmin/utils';
import { getLogin, refreshTokenApi, getPublicToken } from '@/api/user';
import { UserResult, RefreshTokenResult } from '@/api/user';
import { useMultiTagsStoreHook } from '@/store/modules/multiTags';
import { type DataInfo, setToken, removeToken, sessionKey } from '@/utils/auth';
import { getDashboardDefault } from '@/api/dashboard';
export const useUserStore = defineStore({
    id: 'pure-user',
    state: (): userType => ({
        // 用户名
        username: storageSession().getItem<DataInfo<number>>(sessionKey)?.username ?? '',
        // 页面级别权限
        roles: storageSession().getItem<DataInfo<number>>(sessionKey)?.roles ?? [],
        // 前端生成的验证码（按实际需求替换）
        verifyCode: '',
        // 判断登录页面显示哪个组件（0：登录（默认）、1：手机登录、2：二维码登录、3：注册、4：忘记密码）
        currentPage: 0,
        isPublic: 0,
        defaultId: storageSession().getItem('defaultId') ?? '',
    }),
    actions: {
        /** 存储用户名 */
        SET_USERNAME(username: string) {
            this.username = username;
        },
        /** 存储角色 */
        SET_ROLES(roles: Array<string>) {
            this.roles = roles;
        },
        /** 存储前端生成的验证码 */
        SET_VERIFYCODE(verifyCode: string) {
            this.verifyCode = verifyCode;
        },
        /** 存储登录页面显示哪个组件 */
        SET_CURRENTPAGE(value: number) {
            this.currentPage = value;
        },
        /** 登入 */
        async loginByUsername(data) {
            try {
                const tokenInfo = await getLogin(data);
                if (tokenInfo.status != 401) {
                    setToken({ ...tokenInfo, ...data, roles: ['Administrator'] });
                    const defaultId = await getDashboardDefault();
                    this.defaultId = defaultId
                    storageSession().setItem('defaultId', defaultId);
                }
                return tokenInfo;
            } catch (e) {
                console.log(e);
                return e;
            }
        },
        /** 前端登出（不调用接口） */
        logOut() {
            this.username = '';
            this.roles = [];
            this.isPublic = 0;
            removeToken();
            useMultiTagsStoreHook().handleTags('equal', [...routerArrays]);
            resetRouter();
            router.push('/login');
        },
        /** 刷新`token` */
        async handRefreshToken(data) {
            const { token, refreshToken } = await refreshTokenApi(data);
            setToken({
                username: this.username,
                roles: this.roles,
                token,
                refreshToken,
                //expires: '2023/10/30 00:00:00',
            });
            return { token, refreshToken };
        },
        /** 公开连接的token信息更新 */
        async loginPublic(publicId) {
            try {
                const { token, refreshToken } = await getPublicToken(publicId);
                setToken({
                    username: this.username,
                    roles: this.roles,
                    token,
                    refreshToken,
                    //expires: '2023/10/30 00:00:00',
                });
                this.isPublic = 1;
                return { token, refreshToken };
            } catch (e) {
                console.log(e);
                return e;
            }
        },
    },
});

export function useUserStoreHook() {
    return useUserStore(store);
}
