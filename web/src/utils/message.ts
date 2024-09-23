import { type VNode } from 'vue';
import { isFunction } from '@pureadmin/utils';
import { type MessageHandler, ElMessage, ElMessageBox } from 'element-plus';
import { $t } from '@/plugins/i18n';
import { acceptHMRUpdate } from 'pinia';

type messageStyle = 'el' | 'antd';
type messageTypes = 'info' | 'success' | 'warning' | 'error';

interface MessageParams {
    /** 消息类型，可选 `info` 、`success` 、`warning` 、`error` ，默认 `info` */
    type?: messageTypes;
    /** 自定义图标，该属性会覆盖 `type` 的图标 */
    icon?: any;
    /** 是否将 `message` 属性作为 `HTML` 片段处理，默认 `false` */
    dangerouslyUseHTMLString?: boolean;
    /** 消息风格，可选 `el` 、`antd` ，默认 `antd` */
    customClass?: messageStyle;
    /** 显示时间，单位为毫秒。设为 `0` 则不会自动关闭，`element-plus` 默认是 `3000` ，平台改成默认 `2000` */
    duration?: number;
    /** 是否显示关闭按钮，默认值 `false` */
    showClose?: boolean;
    /** 文字是否居中，默认值 `false` */
    center?: boolean;
    /** `Message` 距离窗口顶部的偏移量，默认 `20` */
    offset?: number;
    /** 设置组件的根元素，默认 `document.body` */
    appendTo?: string | HTMLElement;
    /** 合并内容相同的消息，不支持 `VNode` 类型的消息，默认值 `false` */
    grouping?: boolean;
    /** 关闭时的回调函数, 参数为被关闭的 `message` 实例 */
    onClose?: Function | null;
}

/**
 * @description: `Message` 消息提示函数
 * @return {*}
 */
const message = (message: string | VNode | (() => VNode), params?: MessageParams): MessageHandler => {
    if (!params) {
        return ElMessage({
            message,
            customClass: 'pure-message',
            type: 'success',
        });
    } else {
        const {
            icon,
            type = 'success',
            dangerouslyUseHTMLString = false,
            customClass = 'antd',
            duration = 10000,
            showClose = false,
            center = false,
            offset = 20,
            appendTo = document.body,
            grouping = false,
            onClose,
        } = params;

        return ElMessage({
            message,
            type,
            icon,
            dangerouslyUseHTMLString,
            duration,
            showClose,
            center,
            offset,
            appendTo,
            grouping,
            // 全局搜 pure-message 即可知道该类的样式位置
            customClass: customClass === 'antd' ? 'pure-message' : '',
            onClose: () => (isFunction(onClose) ? onClose() : null),
        });
    }
};

/**
 * 关闭所有 `Message` 消息提示函数
 */
const closeAllMessage = (): void => ElMessage.closeAll();

interface ElMessageBoxOptions {
    /** Callback before MessageBox closes, and it will prevent MessageBox from closing */
    beforeClose?: Function | null;
    /** Custom class name for MessageBox */
    customClass?: messageStyle;
    /** Text content of cancel button */
    cancelButtonText?: string;
    /** Text content of confirm button */
    confirmButtonText?: string;
    /** Custom class name of cancel button */
    cancelButtonClass?: string;
    /** Custom class name of confirm button */
    confirmButtonClass?: string;
    /** Whether to align the content in center */
    center?: boolean;
    /** Whether MessageBox can be drag */
    draggable?: boolean;
    /** Title of the MessageBox */
    title?: string | ElMessageBoxOptions;
    /** Message type, used for icon display */
    type?: MessageType;
    /** Custom icon component */
    icon?: string | Component;
    /** Whether to lock body scroll when MessageBox prompts */
    lockScroll?: boolean;
    /** Whether to show a cancel button */
    showCancelButton?: boolean;
    /** Whether to show a confirm button */
    showConfirmButton?: boolean;
    /** Whether to show a close button */
    showClose?: boolean;
    /** 是否将 `message` 属性作为 `HTML` 片段处理，默认 `false` */
    dangerouslyUseHTMLString?: boolean;
    onComfirm?: Function | null;
    onCancel?: Function | null;
}
/**
 * @description: 确认弹窗函数
 * @param {string} message
 * @param {*} options
 * @return {*}
 */
const confirmBox = async (message: string | VNode | (() => VNode), options: ElMessageBoxOptions) => {
    const {
        cancelButtonClass = 'ms-cancel-btn',
        confirmButtonClass = 'exter-btn',
        title = $t('buttons.confirm'),
        confirmButtonText = $t('buttons.yes'),
        cancelButtonText = $t('buttons.no'),
        type,
        center,
        draggable,
        icon,
        lockScroll,
        showCancelButton = true,
        showConfirmButton = true,
        showClose = false,
        ...rest
    } = options;
    try {
        const action = await ElMessageBox.confirm(message, title, {
            cancelButtonClass,
            confirmButtonClass,
            confirmButtonText,
            cancelButtonText,
            type,
            center,
            draggable,
            icon,
            lockScroll,
            showCancelButton,
            showConfirmButton,
            showClose,
            ...rest,
        });
        return action === 'confirm';
    } catch (e) {
        return false;
    }
};

export { message, closeAllMessage, confirmBox };
