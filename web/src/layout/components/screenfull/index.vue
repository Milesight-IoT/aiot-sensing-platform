<!--
 * @Author: your name
 * @Date: 2023-02-06 20:50:39
 * @LastEditTime: 2023-02-16 21:19:08
 * @Descripttion: your project
-->
<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { useFullscreen } from "@vueuse/core";

const { t } = useI18n();
const { isFullscreen, toggle } = useFullscreen();
</script>

<template>
  <div
    class="screen-full w-[36px] h-[58px] flex-ac cursor-pointer navbar-bg-hover"
    @click="toggle"
  >
    <i
      :class="isFullscreen ? 'icon-exit-fullscreen' : 'icon-fullscreen'"
      class="font-size-20"
    ></i>
  </div>
</template>
