/*
 * @Author: your name
 * @Date: 2023-02-06 20:50:39
 * @LastEditTime: 2023-04-24 10:49:18
 * @Descripttion: your project
 */
import { otaUpdate } from '@/router/enums';
export default {
    path: '/otaUpdates',
    component: () => import('@/views/ota/list/index.vue'),
    meta: {
        icon: 'icon-ota-updates',
        title: 'menus.otaUpdates',
        rank: otaUpdate,
    },
} as RouteConfigsTable;
