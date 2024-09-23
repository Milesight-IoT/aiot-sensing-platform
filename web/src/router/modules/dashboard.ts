import { dashboard } from '@/router/enums';
export default {
    path: '/dashboards',
    component: () => import('@/views/dashboard/list/index.vue'),
    meta: {
        icon: 'icon-dashboards',
        title: 'menus.dashboards',
        rank: dashboard,
        showLink: true,
    },
    children: [
        // 点击菜单时直接进入Default仪表盘
        {
            name: 'dashboardDetail',
            path: '/dashboards/:id',
            component: () => import('@/views/dashboard/layout/index.vue'),
            meta: {
                title: 'menus.dashboard',
                showLink: true,
            },
        },
    ],
} as RouteConfigsTable;
