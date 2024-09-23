import { rules } from '@/router/enums';
export default {
    path: '/rules',
    component: () => import('@/views/rule/list/index.vue'),
    meta: {
        icon: 'icon-rules',
        title: 'menus.rules',
        rank: rules,
    },
} as RouteConfigsTable;
