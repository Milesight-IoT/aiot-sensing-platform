/**
 * 输入限制
 * onkeypress 事件会在键盘按键被按下并释放一个键时发生可在事件触发时检测若输入的值不匹配，直接返回 fales
 * 但复杂规则不能使用，会导致输入不流畅
 *  限制空格 v-input-limit:trim
 *  仅输入数字 v-input-limit:number
 *  仅 字母数字下划线 v-input-limit:letter
 *  v-input-limit:custom 自定义  :ce-custom-reg="reg" 自定义模式，输入自定义的正则
 *** 匹配策略，使用nonReg识别不匹配的并替换
 **/
const strategies = {
    /** 限制空格 */
    trim: {
        reg: /[\S]/,
        nonReg: /\s/g,
    },
    number: {
        reg: /[\d]/,
        nonReg: /\D/gi,
    },
    /** 允许 字母数字下划线 */
    letter: {
        reg: /[\w]/,
        nonReg: /\W/g,
    },
    /** 输入十六进制的时候允许英文字母和数字 */
    hex: {
        reg: /[A-Za-z0-9]/,
        nonReg: /[^a-zA-Z0-9]/g,
    },
    /** 小数，允许输入数字和小数点 */
    decimal: {
        reg: /[0-9.]/,
        nonReg: /[^0-9.]/g,
    },
    /** 负数，允许输入数字和小数点 */
    minus: {
        // reg: /^-?\d{1,5}$/g,
        nonReg: /[^-\d]/g, // 清除非数组和非负号
    },
    /**英文字符: 英文字母、数字和英文符号 */
    lettersymbol: {
        reg: /[\x00-\xff]/,
        nonReg: /[^\x00-\xff]/g,
    },
    // custom: function (arg) {
    //   //自定义
    //   return { ...arg };
    // }
};

/** 当输入非法值时使用replace清除输入的非法值
 * @param ele 指绑定的内部input元素，其value值支持双向绑定
 * @param binding binding.arg是指令冒号后的值，binding.value是等号后的值
 * @param strategy nonReg指内置使用的清除替换正则式
 */
function limitValue(el, ele, binding, strategy) {
    const noReg = strategy.nonReg;
    const srcValue = ele.value;
    const resValue = ele.value.replace(noReg, '');
    ele.value = resValue;
    if (srcValue != resValue) {
        //elInput内的值更新，触发校验
        el.emit('update:modelValue', resValue);
    }
    return ele.value;
}

function eventHandler(el, binding) {
    const strategy = strategies[binding.arg];
    // 使用ElInput的事件，获得对应回调,val.target.value即绑定数据
    el.oninput = e => {
        // 当输入类型是输入法时特殊处理
        if (e.inputType == 'insertCompositionText') {
            // 当输入法结束时才执行限制
            el.addEventListener('compositionend', () => {
                console.log('compositionend');
                limitValue(el.__vueParentComponent, e.target, binding, strategy);
            });
            return;
        } else {
            limitValue(el.__vueParentComponent, e.target, binding, strategy);
            //el.parent.validate();
        }
    };
}

/** v-input-limit指令在/src/main.ts中导入在main中统一注册 */
export const inputLimit = {
    mounted(el, binding) {
        eventHandler(el, binding);
    },
    /** 当绑定到元素上的指令的值发生变化时，Vue3并不会自动重新调用指令的mounted钩子来更新事件侦听器 */
    updated(el, binding) {
        const oldValue = binding.oldValue;
        const newValue = binding.arg;
        if (oldValue !== newValue) {
            eventHandler(el, binding);
        }
    },
};
