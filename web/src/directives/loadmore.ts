import { Directive, type DirectiveBinding } from "vue";
export const loadmore: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    // const child = el.querySelector('.select-trigger');
    // const id = child.getAttribute('aria-describedby');

    const popName = el.getAttribute('popperclass')
    console.log(popName)
    // const poper = document.querySelector(`.${popName}`);
    // const SELECTDOWN_DOM = poper?.querySelector('.el-scrollbar .el-select-dropdown__wrap');
    const SELECTDOWN_DOM = document.querySelector(`.${popName} .el-select-dropdown__wrap`)
    // 这里不能使用箭头函数！
    // eslint-disable-next-line func-names
    SELECTDOWN_DOM.addEventListener('scroll', function () {
      /**
       * scrollHeight 获取元素内容高度(只读)
       * scrollTop 获取或者设置元素的偏移值,
       *  常用于:计算滚动条的位置, 当一个元素的容器没有产生垂直方向的滚动条, 那它的scrollTop的值默认为0.
       * clientHeight 读取元素的可见高度(只读)
       * 如果元素滚动到底, 下面等式返回true, 没有则返回false:
       * ele.scrollHeight - ele.scrollTop === ele.clientHeight;
       */
      const CONDITION = this.scrollHeight - this.scrollTop <= this.clientHeight;
      if (CONDITION) {
        binding.value();
      }
    });
  }
};