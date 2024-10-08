<script setup lang="ts">
import { ref, computed } from "vue";
import { emitter } from "@/utils/mitt";
import { onClickOutside } from "@vueuse/core";
import Close from "@iconify-icons/ep/close";

const target = ref(null);
const show = ref<Boolean>(false);

const iconClass = computed(() => {
  return [
    "mr-[20px]",
    "outline-none",
    "width-[20px]",
    "height-[20px]",
    "rounded-[4px]",
    "cursor-pointer",
    "transition-colors",
    "hover:bg-[#0000000f]",
    "dark:hover:bg-[#ffffff1f]",
    "dark:hover:text-[#ffffffd9]"
  ];
});

onClickOutside(target, (event: any) => {
  if (event.clientX > target.value.offsetLeft) return;
  show.value = false;
});

emitter.on("openPanel", () => {
  show.value = true;
});
</script>

<template>
  <div :class="{ show: show }" class="right-panel-container">
    <div class="right-panel-background" />
    <div ref="target" class="right-panel bg-bg_color">
      <div class="right-panel-items">
        <div class="project-configuration">
          <h4 class="dark:text-white">项目配置</h4>
          <span title="关闭配置" :class="iconClass">
            <IconifyIconOffline
              class="dark:text-white"
              width="20px"
              height="20px"
              :icon="Close"
              @click="show = !show"
            />
          </span>
        </div>
        <div
          class="border-b-[1px] border-solid border-[#dcdfe6] dark:border-[#303030]"
        />
        <slot />
      </div>
    </div>
  </div>
</template>

<style>
.showright-panel {
  overflow: hidden;
  position: relative;
  width: calc(100% - 15px);
}
</style>

<style lang="scss" scoped>
.right-panel-background {
  position: fixed;
  top: 0;
  left: 0;
  opacity: 0;
  transition: opacity 0.3s cubic-bezier(0.7, 0.3, 0.1, 1);
  background: rgba(0, 0, 0, 0.2);
  z-index: -1;
}

.right-panel {
  width: 100%;
  max-width: 315px;
  height: 100vh;
  position: fixed;
  top: 0;
  right: 0;
  box-shadow: 0 0 15px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.25s cubic-bezier(0.7, 0.3, 0.1, 1);
  transform: translate(100%);
  z-index: 40000;
}

.show {
  transition: all 0.3s cubic-bezier(0.7, 0.3, 0.1, 1);

  .right-panel-background {
    z-index: 20000;
    opacity: 1;
    width: 100%;
    height: 100%;
  }

  .right-panel {
    transform: translate(0);
  }
}

.handle-button {
  width: 48px;
  height: 58px;
  position: absolute;
  left: -48px;
  text-align: center;
  font-size: 24px;
  border-radius: 6px 0 0 6px !important;
  z-index: 0;
  pointer-events: auto;
  cursor: pointer;
  color: #fff;
  line-height: 58px;
  top: 45%;
  background: rgb(24, 144, 255);

  i {
    font-size: 24px;
    line-height: 58px;
  }
}

.right-panel-items {
  margin-top: 60px;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.project-configuration {
  display: flex;
  width: 100%;
  height: 30px;
  position: fixed;
  justify-content: space-between;
  align-items: center;
  top: 15px;
  margin-left: 10px;
}

:deep(.el-divider--horizontal) {
  width: 90%;
  margin: 20px auto 0 auto;
}
</style>
