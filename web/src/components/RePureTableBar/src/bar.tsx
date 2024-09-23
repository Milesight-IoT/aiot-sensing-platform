import { delay, debounce } from "@pureadmin/utils";
import { useEpThemeStoreHook } from "@/store/modules/epTheme";
import { defineComponent, ref, computed, watch, type PropType } from "vue";
import ExpandIcon from "./svg/expand.svg?component";
import RefreshIcon from "./svg/refresh.svg?component";
import SettingIcon from "./svg/settings.svg?component";
import CollapseIcon from "./svg/collapse.svg?component";
import { $t } from "@/plugins/i18n";

const props = {
  /** 头部最左边的标题 */
  title: {
    type: String,
    default: "列表"
  },
  /** 对于树形表格，如果想启用展开和折叠功能，传入当前表格的ref即可 */
  tableRef: {
    type: Object as PropType<any>
  },
  /** 表格工具 */
  showTool: {
    type: Boolean,
    default: false
  },
  showSearch: {
    type: Boolean,
    default: true
  },
  selectInfo: {
    type: Object,
    default: {
      show: false,
      tips: '',
      count: 1
    }
  }
};

export default defineComponent({
  name: "PureTableBar",
  props,
  emits: ["refresh", "search", "delete"],
  setup(props, { emit, slots, attrs }) {
    const buttonRef = ref();
    const checkList = ref([]);
    const size = ref("default");
    const isExpandAll = ref(true);
    const loading = ref(false);
    const showSearch = ref(false)
    const getDropdownItemStyle = computed(() => {
      return s => {
        return {
          background:
            s === size.value ? useEpThemeStoreHook().epThemeColor : "",
          color: s === size.value ? "#fff" : "var(--el-text-color-primary)"
        };
      };
    });

    const iconClass = computed(() => {
      return [
        "text-black",
        "dark:text-white",
        "duration-100",
        "hover:!text-primary",
        "cursor-pointer",
        "outline-none"
      ];
    });

    function onReFresh() {
      loading.value = true;
      emit("refresh");
      delay(500).then(() => (loading.value = false));
    }

    function onExpand() {
      isExpandAll.value = !isExpandAll.value;
      toggleRowExpansionAll(props.tableRef.data, isExpandAll.value);
    }

    function toggleRowExpansionAll(data, isExpansion) {
      data.forEach(item => {
        props.tableRef.toggleRowExpansion(item, isExpansion);
        if (item.children !== undefined && item.children !== null) {
          toggleRowExpansionAll(item.children, isExpansion);
        }
      });
    }

    const dropdown = {
      dropdown: () => (
        <el-dropdown-menu class="translation">
          <el-dropdown-item
            style={getDropdownItemStyle.value("large")}
            onClick={() => (size.value = "large")}
          >
            宽松
          </el-dropdown-item>
          <el-dropdown-item
            style={getDropdownItemStyle.value("default")}
            onClick={() => (size.value = "default")}
          >
            默认
          </el-dropdown-item>
          <el-dropdown-item
            style={getDropdownItemStyle.value("small")}
            onClick={() => (size.value = "small")}
          >
            紧凑
          </el-dropdown-item>
        </el-dropdown-menu>
      )
    };

    const reference = {
      reference: () => (
        <SettingIcon
          class={["w-[16px]", iconClass.value]}
          onMouseover={e => (buttonRef.value = e.currentTarget)}
        />
      )
    };
    const searchText = ref('')
    const showSearchPanel = () => {
      searchText.value = ''
      showSearch.value = true
    }
    const hideSearch = () => {
      emit("search", '');
      showSearch.value = false
    }
    function onEmitSearch() {
      emit("search", searchText.value.trim());
    }
    const onSearch = debounce(onEmitSearch, 500);

    const showDelPanel = ref(false)
    const delTips = ref('')
    const onDelete = () => {
      emit("delete");
    }
    watch(
      () => props.selectInfo,
      val => {
        showDelPanel.value = val.show;
        delTips.value = val.tips
      }
    );

    return () => (
      <>
        <div {...attrs} class="-[99/100] bg-bg_color h-full flex flex-col relative">
          <div class="flex justify-between w-full h-[60px] p-4">
            <p class="pb-5 truncate">{props.title}</p>
            <div class="flex items-center justify-around">
              <div class="flex mr-4">
                {slots?.buttons()}
                {props.showSearch ? (<>
                  <el-tooltip effect="dark" content={$t('buttons.search')} placement="top">
                    <i class="icon-search text-2xl btn-icon-bg" onClick={() => showSearchPanel()} ></i>
                  </el-tooltip>
                </>) : null}
              </div>
              {props.tableRef?.size ? (
                <>
                  <el-tooltip
                    effect="dark"
                    content={isExpandAll.value ? "折叠" : "展开"}
                    placement="top"
                  >
                    <ExpandIcon
                      class={["w-[16px]", iconClass.value]}
                      style={{
                        transform: isExpandAll.value ? "none" : "rotate(-90deg)"
                      }}
                      onClick={() => onExpand()}
                    />
                  </el-tooltip>
                  <el-divider direction="vertical" />
                </>
              ) : null}
              {props.showTool ? (
                <>
                  <el-tooltip effect="dark" content="刷新" placement="top">
                    <RefreshIcon
                      class={[
                        "w-[16px]",
                        iconClass.value,
                        loading.value ? "animate-spin" : ""
                      ]}
                      onClick={() => onReFresh()}
                    />
                  </el-tooltip>
                  <el-divider direction="vertical" />

                  <el-tooltip effect="dark" content="密度" placement="top">
                    <el-dropdown v-slots={dropdown} trigger="click">
                      <CollapseIcon class={["w-[16px]", iconClass.value]} />
                    </el-dropdown>
                  </el-tooltip>
                  <el-divider direction="vertical" />

                  <el-popover v-slots={reference} width="200" trigger="click">
                    <el-checkbox-group v-model={checkList.value}>
                      <el-checkbox label="序号列" />
                      <el-checkbox label="勾选列" />
                    </el-checkbox-group>
                  </el-popover>
                </>
              ) : null}
            </div>
            <el-tooltip
              popper-options={{
                modifiers: [
                  {
                    name: "computeStyles",
                    options: {
                      adaptive: false,
                      enabled: false
                    }
                  }
                ]
              }}
              placement="top"
              virtual-ref={buttonRef.value}
              virtual-triggering
              trigger="hover"
              content="列设置"
            />
          </div>
          <div class="flex justify-between w-full h-[60px] p-4 bg-white absolute" v-show={showSearch.value}>
            <el-tooltip effect="dark" content={$t('buttons.search')} placement="top">
              <i class="icon-search text-xl btn-icon-bg" onClick={() => onSearch()} ></i>
            </el-tooltip>
            <el-input v-model={searchText.value} placeholder={$t('common.typeOfSearch')} class="grow mx-2" onInput={() => onSearch()}></el-input>
            <el-tooltip effect="dark" content={$t('buttons.cancel')} placement="top">
              <i class="icon-eliminate text-xl btn-icon-bg" onClick={() => hideSearch()} ></i>
            </el-tooltip>
          </div>
          <div class="flex justify-between w-full h-[60px] p-4 bg-dark absolute" v-show={showDelPanel.value}>
            <p>{$t(props.selectInfo.key).replace('%s', props.selectInfo.count)}</p>
            <el-tooltip effect="dark" content={$t('buttons.delete')} placement="top">
              <i class="icon-delete text-2xl btn-icon" onClick={() => onDelete()} ></i>
            </el-tooltip>
          </div>
          {slots.default({ size: size.value, checkList: checkList.value })}
        </div>
      </>
    );
  }
});
