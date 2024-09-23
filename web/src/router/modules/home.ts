import { home } from '@/router/enums';
const Layout = () => import('@/layout/index.vue');

export default {
    path: '/',
    name: 'Home',
    component: Layout,
    redirect: '/dashboards/:id',
    meta: {
        icon: 'homeFilled',
        title: 'menus.hshome',
        rank: home,
        showLink: false,
    },
} as RouteConfigsTable;
