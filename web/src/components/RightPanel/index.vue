<script setup lang="ts">
import { ref, computed } from 'vue';
import { emitter } from '@/utils/mitt';
import { onClickOutside } from '@vueuse/core';

const target = ref(null);
const show = ref<Boolean>(false);

const props = defineProps({
    panelTitle: String,
    panelSubTitle: String,
    panelStyle: Object,
});
const panelTitle = ref<String>(props.panelTitle);
const iconClass = computed(() => {
    return [
        // 'mr-[20px]',
        'outline-none',
        'width-[20px]',
        'height-[20px]',
        'rounded-[4px]',
        'cursor-pointer',
        'transition-colors',
        'hover:bg-[#0000000f]',
        'dark:hover:bg-[#ffffff1f]',
        'dark:hover:text-[#ffffffd9]',
    ];
});

onClickOutside(target, (event: any) => {
    if (event.clientX > target.value.offsetLeft) return;
    //show.value = false;
});
const emit = defineEmits(['closePanel']);
emitter.on('showPanel', val => {
    show.value = val;
    console.log(val);
    if (!val) {
        emit('closePanel');
    }
});
emitter.on('panelTitle', val => {
    panelTitle.value = val;
});
const closePanel = () => {
    show.value = false;
    emit('closePanel');
};
</script>

<template>
    <div :class="{ show: show }" class="right-panel-container">
        <div class="right-panel-background" />
        <div ref="target" class="right-panel bg-bg_color" :style="panelStyle">
            <div class="right-panel-items">
                <div class="project-configuration">
                    <div>
                        <h4 class="text-white">{{ panelTitle }}</h4>
                        <p class="text-sm text-subTitle">{{ panelSubTitle }}</p>
                    </div>
                    <span :title="$t('buttons.close')" :class="iconClass">
                        <i class="text-2xl text-white icon-eliminate" @click="closePanel" />
                    </span>
                </div>
                <slot name="header" />
                <div class="border-b-[1px] border-solid border-[#dcdfe6] dark:border-[#303030]" />
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
    top: 58px;
    right: 0;
    transition: all 0.25s cubic-bezier(0.7, 0.3, 0.1, 1);
    transform: translate(100%);
    z-index: 1001;
    background: #f5f5f5;
    box-shadow: -6px 0 10px rgba(0, 0, 0, 0.14);
}

.show {
    transition: all 0.3s cubic-bezier(0.7, 0.3, 0.1, 1);

    .right-panel-background {
        z-index: 1000;
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
        line-height: 48px;
    }
}
.text-subTitle {
    color: #c9c9c9;
}
.right-panel-items {
    height: calc(100vh - 60px);
    overflow-y: auto;
}

.project-configuration {
    display: flex;
    width: 100%;
    padding: 20px;
    justify-content: space-between;
    align-items: center;
    background: $menuBg;
}

:deep(.el-divider--horizontal) {
    width: 90%;
    margin: 20px auto 0 auto;
}
</style>
