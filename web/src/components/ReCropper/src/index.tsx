import type { CSSProperties } from "vue";
import {
    defineComponent,
    onMounted,
    nextTick,
    ref,
    unref,
    computed,
    PropType
} from "vue";
import { useAttrs } from "@pureadmin/utils";
import { $t } from "@/plugins/i18n";
import Cropper from "cropperjs";
import "cropperjs/dist/cropper.css";

type Options = Cropper.Options;

const defaultOptions: Cropper.Options = {
    zoomable: false,
    zoomOnTouch: true,
    zoomOnWheel: true,
    cropBoxMovable: true,
    cropBoxResizable: true,
    toggleDragModeOnDblclick: false,
    autoCrop: false,
    background: false,
    highlight: true,
    center: false,
    responsive: true,
    restore: true,
    checkCrossOrigin: true,
    checkOrientation: true,
    scalable: true,
    modal: false,
    guides: true,
    movable: false,
    rotatable: true,
    viewMode: 1
};

const props = {
    src: {
        type: String,
        required: true
    },
    alt: {
        type: String
    },
    width: {
        type: [String, Number],
        default: ""
    },
    height: {
        type: [String, Number],
        default: "348.75px"
    },
    crossorigin: {
        type: String || Object,
        default: undefined
    },
    imageStyle: {
        type: Object as PropType<CSSProperties>,
        default() {
            return {};
        }
    },
    options: {
        type: Object as PropType<Options>,
        default() {
            return {};
        }
    },
    cropTitle: {
        type: String,
        default: ''
    }
};
export default defineComponent({
    name: "ReCropper",
    props,
    setup(props, { emit }) {
        const cropper: any = ref<Nullable<Cropper>>(null);
        const imgElRef = ref();

        const isReady = ref<boolean>(false);

        const getImageStyle = computed((): CSSProperties => {
            return {
                height: props.height,
                width: props.width,
                maxWidth: "100%",
                ...props.imageStyle
            };
        });

        const getWrapperStyle = computed((): CSSProperties => {
            const { height, width } = props;
            return {
                width: `${width}`.replace(/px/, "") + "px",
                height: `${height}`.replace(/px/, "") + "px"
            };
        });
        const titleStyle = ref();
        function init() {
            const imgEl = unref(imgElRef);
            if (!imgEl) {
                return;
            }
            cropper.value = new Cropper(imgEl, {
                ...defaultOptions,
                ready: () => {
                    isReady.value = true;
                    emit('isReady')
                },
                crop: () => {
                    let info = cropper.value.cropBoxData
                    titleStyle.value = {
                        left: `${info.left + 3}px`,
                        top: `${info.top + 3}px`
                    }
                },
                ...props.options
            });
        }

        function imgError() {
            emit('cropError')
        }

        onMounted(() => {
            nextTick(() => {
                init();
            });
        });

        return {
            props,
            imgElRef,
            cropper,
            getWrapperStyle,
            getImageStyle,
            titleStyle,
            isReady,
            imgError
        };
    },

    render() {
        return (
            <>
                <div
                    class={useAttrs({ excludeListeners: true, excludeKeys: ["class"] })}
                    style={this.getWrapperStyle}
                >
                    <img
                        ref="imgElRef"
                        src={this.props.src}
                        alt={this.props.alt}
                        crossorigin={this.props.crossorigin}
                        style={this.getImageStyle}
                        v-show={this.isReady}
                        onError={this.imgError}
                    />
                    <p v-if={!this.isReady.value} class="bg-white text-center py-44 text-xl"><span>{$t('common.noImg')}</span></p>
                    <p class="roi-text text-lg" style={this.titleStyle} v-show={this.isReady}>{this.props.cropTitle}</p>
                </div>
            </>
        );
    }
});
