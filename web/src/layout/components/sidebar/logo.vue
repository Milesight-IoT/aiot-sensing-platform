<!--
 * @Author: your name
 * @Date: 2023-02-06 20:50:39
 * @LastEditTime: 2023-02-16 21:18:20
 * @Descripttion: your project
-->
<script setup lang="ts">
import { useNav } from "@/layout/hooks/useNav";

const props = defineProps({
  collapse: Boolean
});

const { title } = useNav();
</script>

<template>
  <div class="sidebar-logo-container" :class="{ collapses: props.collapse }">
    <transition name="sidebarLogoFade">
      <router-link
        v-if="props.collapse"
        key="props.collapse"
        :title="title"
        class="sidebar-logo-link"
        to="/"
      >
        <i class="icon-logo sidebar-logo" />
      </router-link>
      <router-link
        v-else
        key="expand"
        :title="title"
        class="sidebar-logo-link"
        to="/"
      >
        <i class="icon-logo" />
      </router-link>
    </transition>
  </div>
</template>

<style lang="scss" scoped>
.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 58px;
  text-align: center;
  overflow: hidden;

  .sidebar-logo-link {
    height: 100%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    margin-top: 5px;
  }
  .icon-logo {
    font-size: 34px;
    color: white;
    padding-left: 10px;
  }

  .collapses {
    .sidebar-logo {
      margin-right: 0;
    }
  }
}
</style>
