import { $t } from '@/plugins/i18n';
import { object } from '@/router/enums';
export default {
    path: '/objects',
    component: () => import('@/views/object/list/index.vue'),
    meta: {
        icon: 'icon-objects',
        title: 'menus.objects',
        rank: object,
    },
} as RouteConfigsTable;
