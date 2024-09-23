/*
 * @Author: Lin
 * @Date: 2023-02-06 20:50:39
 * @LastEditTime: 2023-05-31 15:13:53
 * @Descripttion: your project
 */
import { system } from '@/router/enums';
export default {
    path: '/settings',
    redirect: '/settings/recipients',
    meta: {
        icon: 'icon-system-settings',
        title: 'menus.systemSettings',
        rank: system,
        showLink: true,
    },
    children: [
        {
            path: '/settings/recipients',
            name: 'Recipient',
            component: () => import('@/views/system/recipient/list/index.vue'),
            meta: {
                title: 'menus.recipients',
                showParent: true,
            },
        },
    ],
} as RouteConfigsTable;
