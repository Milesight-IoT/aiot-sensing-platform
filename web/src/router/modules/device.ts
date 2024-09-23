import { device } from '@/router/enums';
export default {
    path: '/devices',
    component: () => import('@/views/device/list/index.vue'),
    meta: {
        icon: 'icon-devices',
        title: 'menus.devices',
        rank: device,
    },
} as RouteConfigsTable;
