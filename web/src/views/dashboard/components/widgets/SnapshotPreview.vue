<script setup lang="ts">
import Cropper from '@/components/ReCropper';
import { ref, computed, onMounted, watch } from 'vue';
import dayjs from 'dayjs';
const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return {
                name: '',
                img: '',
                ts: '',
            };
        },
    },
});
const refCropper = ref();
const cropImg = ref();
const showError = ref(false);
const showImg = computed(() => {
    if (props.data.img && props.data.img?.indexOf('image/') != '-1' && !props.data.extraInfo) return true;
    return false;
});
const showCrop = computed(() => {
    if (props.data.img?.indexOf('image/') != '-1' && props.data.extraInfo && !showError.value) return true;
    return false;
});
const initRoi = () => {
    // const [x, y, width, height] = props.data.extraInfo.split(',');
    const [x, y, width, height] = props.data.extraInfo.split(',');
    console.log(x, y, width, height);
    refCropper.value.cropper.crop();
    refCropper.value.cropper.setData({
        width: Number(width),
        height: Number(height),
        x: Number(x),
        y: Number(y),
    });
    const canvas = refCropper.value.cropper.getCroppedCanvas();
    console.log(canvas);
    canvas.toBlob(blob => {
        if (!blob) return;
        const fileReader: FileReader = new FileReader();
        fileReader.readAsDataURL(blob);
        fileReader.onloadend = e => {
            if (!e.target?.result || !blob) return;
            cropImg.value = e.target.result;
        };
        fileReader.onerror = () => {
            console.log('error crop');
        };
    });
    //refCropper.value.cropper.disable();
};
function formatTime(timestamp) {
    if (timestamp) {
        return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss');
    } else {
        return '-';
    }
}
const imgError = () => {
    showError.value = true;
};
watch(
    () => props.data,
    val => {
        if (props.data.img && props.data.img?.indexOf('image/') == '-1') imgError();
    },
);
</script>

<template>
    <div class="relative w-full h-full p-5 box">
        <p class="mb-5 text-base title">{{ data.name }}</p>
        <img
            class=""
            :src="data.img"
            alt="No image"
            v-if="showImg && !showError"
            @error="imgError"
            @load="showError = false"
        />
        <img class="" :src="cropImg" alt="No image" v-if="showCrop" @error="imgError" />
        <div class="img-block" v-if="!showImg && !showCrop && !showError">{{ $t('common.noImg') }}</div>
        <div class="img-block" v-if="showError">{{ $t('common.errorImg') }}</div>
        <div class="cropper-container img-block" v-if="showCrop">
            <Cropper ref="refCropper" :src="data.img" @isReady="initRoi" @cropError="imgError" />
        </div>
        <div class="absolute bottom-0 mb-5 time">
            <i class="mr-2 icon-time" />
            <span>{{ formatTime(data.ts) }}</span>
        </div>
    </div>
</template>

<style scoped lang="scss">
img {
    max-height: calc(100% - 80px);
    width: 100%;
    margin: 0 auto;
}
.img-block {
    height: calc(100% - 80px);
    width: 100%;
    margin: 0 auto;
    text-align: center;
    display: inline-block;
    vertical-align: middle;
    border: 1px solid #ddd;
    display: flex;
    justify-content: center;
    align-items: center;
}
.cropper-container {
    border: none;
    visibility: hidden;
}
</style>
