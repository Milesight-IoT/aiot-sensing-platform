/*
 * @Author: your name
 * @Date: 2023-02-06 20:50:39
 * @LastEditTime: 2023-04-24 10:49:19
 * @Descripttion: your project
 */
import { $t } from '@/plugins/i18n';
const Layout = () => import('@/layout/index.vue');

export default [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: {
            title: $t('menus.login'),
            showLink: false,
            rank: 101,
        },
    },
    {
        path: '/redirect',
        component: Layout,
        meta: {
            icon: 'homeFilled',
            title: $t('menus.hshome'),
            showLink: false,
            rank: 102,
        },
        children: [
            {
                path: '/redirect/:path(.*)',
                name: 'Redirect',
                component: () => import('@/layout/redirect.vue'),
            },
        ],
    },
    // 下面是一个无layout菜单的例子（一个全屏空白页面），因为这种情况极少发生，所以只需要在前端配置即可（配置路径：src/router/modules/remaining.ts）
] as Array<RouteConfigsTable>;
