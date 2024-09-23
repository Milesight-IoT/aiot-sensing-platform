<script setup lang="ts">
import { isEqual } from '@pureadmin/utils';
import { transformI18n } from '@/plugins/i18n';
import { ref, watch, onMounted, toRaw, nextTick } from 'vue';
import { getParentPaths, findRouteByPath } from '@/router/utils';
import { useMultiTagsStoreHook } from '@/store/modules/multiTags';
import { usePermissionStoreHook } from '@/store/modules/permission';
import { useNav } from '@/layout/hooks/useNav';
import { useRoute, useRouter, RouteLocationMatched } from 'vue-router';
import { emitter } from '@/utils/mitt';
import dashboard from '@/router/modules/dashboard';
const route = useRoute();
const levelList = ref([]);
const router = useRouter();
const routes: any = router.options.routes;
const multiTags: any = useMultiTagsStoreHook().multiTags;
const { changeTitle } = useNav();
const isDashboard = (route: RouteLocationMatched): boolean | string => {
    const name = route && (route.name as string);
    if (!name) return false;
    return name.trim().toLocaleLowerCase() === 'dashboardDetail'.toLocaleLowerCase();
};

const getBreadcrumb = (): void => {
    // 当前路由信息
    let currentRoute;
    let matched = [];

    if (Object.keys(route.query).length > 0) {
        // multiTags.forEach(item => {
        //     if (isEqual(route.query, item?.query)) {
        //         currentRoute = toRaw(item);
        //     }
        // });
        //currentRoute = toRaw(route.path);
        //console.log(route.path);
    } else if (Object.keys(route.params).length > 0) {
        currentRoute = route.matched[route.matched.length - 1];
        // multiTags.forEach(item => {
        //     if (isEqual(route.matched[route.matched.length - 1].path, item?.path)) {
        //         currentRoute = toRaw(item);
        //     }
        // });
        console.log(route.matched);
    } else {
        currentRoute = findRouteByPath(router.currentRoute.value.path, multiTags) || router.currentRoute.value;
    }
    // 当前路由的父级路径组成的数组
    const parentRoutes = getParentPaths(currentRoute.path, routes);

    console.log(parentRoutes);

    // // path的上级路由组成的数组
    // const parentPathArr = getParentPaths(router.currentRoute.value.path, usePermissionStoreHook().wholeMenus);
    // // 当前路由的父级路由信息
    // const parenetRoutes = findRouteByPath(parentPathArr[0] || router.currentRoute.value.path, usePermissionStoreHook().wholeMenus);
    // 存放组成面包屑的数组

    // 获取每个父级路径对应的路由信息
    const len = parentRoutes.length;

    parentRoutes.forEach(path => {
        if (path !== '/') matched.push(findRouteByPath(path, routes));
    });

    if (currentRoute?.path !== '/welcome') matched.push(currentRoute);
    console.log(currentRoute);
    matched.forEach((item, index) => {
        if (currentRoute?.query || currentRoute?.params) return;
        if (item?.children) {
            item.children.forEach(v => {
                if (v?.meta?.title === item?.meta?.title) {
                    matched.splice(index, 1);
                }
            });
        }
    });
    if (matched.length) levelList.value = matched.filter(item => item?.meta && item?.meta.title !== false);
    //document.title = `${len}|${matched.length}|${levelList.value.length}|${router.currentRoute.value.path}}`;
    //console.log(levelList.value);
};

const handleLink = (item: RouteLocationMatched): void => {
    const { redirect, path } = item;
    console.log(redirect, path);
    if (redirect) {
        router.push(redirect as any);
    } else {
        if (path != '/dashboards/:id') router.push(path);
    }
};

onMounted(() => {
    nextTick(() => {
        getBreadcrumb();
    });
});
emitter.on('updateBread', dashboardName => {
    console.log(levelList.value);
    if (levelList.value.length > 1) {
        levelList.value[1].meta.title = dashboardName;
        changeTitle(levelList.value[1].meta);
    } else {
        levelList.value.push({
            path: '/dashboards',
            meta: {
                icon: 'icon-dashboards',
                title: 'menus.dashboards',
            },
        });
        levelList.value.push({
            path: '/dashboards:id',
            meta: {
                title: dashboardName,
            },
        });
    }
});
watch(
    () => route.path,
    () => {
        getBreadcrumb();
    },
);
</script>

<template>
    <el-breadcrumb class="!leading-[60px] select-none" separator=">">
        <transition-group name="breadcrumb">
            <el-breadcrumb-item class="!inline !items-stretch" v-for="item in levelList" :key="item.path">
                <a @click.prevent="handleLink(item)" class="text-lg">
                    <i :class="item.meta.icon" class="text-2xl" />
                    {{ transformI18n(item.meta.title) }}
                </a>
            </el-breadcrumb-item>
        </transition-group>
    </el-breadcrumb>
</template>
