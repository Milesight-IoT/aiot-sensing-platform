/**
 * @description ⚠️：此文件仅供主题插件使用，请不要在此文件中导出别的工具函数（仅在页面加载前运行）
 */

import { type multipleScopeVarsOptions } from "@pureadmin/theme";

/** 预设主题色 */
const themeColors = {
  default: {
    menuText: "#fff",
    menuBg: "#353C4A",
    menuBorder: "#404653",
    menuHover: "#3F4653",
    menuTitleHover: "#fff",
    menuTitleHoverBg: "#3F4653",
    menuActiveBefore: "#4A505C",
    subMenuBg: "#353C4A",
    subMenuActiveBg: "#4A505C",
    subMenuActiveText: "#50b8ff",
    sidebarLogo: "#353C4A",
    contentText: "#333",
    dropMenuHoverBg: "#f5f5f5",
    dropMenuActive: "#036baf",
    dropMenuActiveBg: "#efefef"
  },
  light: {
    subMenuActiveText: "#409eff",
    menuBg: "#fff",
    menuHover: "#e0ebf6",
    subMenuBg: "#fff",
    subMenuActiveBg: "#e0ebf6",
    menuText: "#7a80b4",
    sidebarLogo: "#fff",
    menuTitleHover: "#000",
    menuTitleHoverBg: "#000",
    menuActiveBefore: "#4091f7"
  },
  dusk: {
    subMenuActiveText: "#fff",
    menuBg: "#2a0608",
    menuHover: "#e13c39",
    subMenuBg: "#000",
    subMenuActiveBg: "#e13c39",
    menuText: "rgb(254 254 254 / 65.1%)",
    sidebarLogo: "#42090c",
    menuTitleHover: "#fff",
    menuTitleHoverBg: "#fff",
    menuBorder: "#3a455a",
    menuActiveBefore: "#e13c39"
  },
  volcano: {
    subMenuActiveText: "#fff",
    menuBg: "#2b0e05",
    menuHover: "#e85f33",
    subMenuBg: "#0f0603",
    subMenuActiveBg: "#e85f33",
    menuText: "rgb(254 254 254 / 65%)",
    sidebarLogo: "#441708",
    menuTitleHover: "#fff",
    menuTitleHoverBg: "#fff",
    menuBorder: "#3a455a",
    menuActiveBefore: "#e85f33"
  },
  yellow: {
    subMenuActiveText: "#d25f00",
    menuBg: "#2b2503",
    menuHover: "#f6da4d",
    subMenuBg: "#0f0603",
    subMenuActiveBg: "#f6da4d",
    menuText: "rgb(254 254 254 / 65%)",
    sidebarLogo: "#443b05",
    menuTitleHover: "#fff",
    menuTitleHoverBg: "#fff",
    menuBorder: "#3a455a",
    menuActiveBefore: "#f6da4d"
  },
  mingQing: {
    subMenuActiveText: "#fff",
    menuBg: "#032121",
    menuHover: "#59bfc1",
    subMenuBg: "#000",
    subMenuActiveBg: "#59bfc1",
    menuText: "#7a80b4",
    sidebarLogo: "#053434",
    menuTitleHover: "#fff",
    menuTitleHoverBg: "#fff",
    menuBorder: "#3a455a",
    menuActiveBefore: "#59bfc1"
  },
  auroraGreen: {
    subMenuActiveText: "#fff",
    menuBg: "#0b1e15",
    menuHover: "#60ac80",
    subMenuBg: "#000",
    subMenuActiveBg: "#60ac80",
    menuText: "#7a80b4",
    sidebarLogo: "#112f21",
    menuTitleHover: "#fff",
    menuTitleHoverBg: "#fff",
    menuBorder: "#3a455a",
    menuActiveBefore: "#60ac80"
  },
  pink: {
    subMenuActiveText: "#fff",
    menuBg: "#28081a",
    menuHover: "#d84493",
    subMenuBg: "#000",
    subMenuActiveBg: "#d84493",
    menuText: "#7a80b4",
    sidebarLogo: "#3f0d29",
    menuTitleHover: "#fff",
    menuTitleHoverBg: "#fff",
    menuBorder: "#3a455a",
    menuActiveBefore: "#d84493"
  },
  saucePurple: {
    subMenuActiveText: "#fff",
    menuBg: "#130824",
    menuHover: "#693ac9",
    subMenuBg: "#000",
    subMenuActiveBg: "#693ac9",
    menuText: "#7a80b4",
    sidebarLogo: "#1f0c38",
    menuTitleHover: "#fff",
    menuTitleHoverBg: "#fff",
    menuBorder: "#3a455a",
    menuActiveBefore: "#693ac9"
  }
};

/**
 * @description 将预设主题色处理成主题插件所需格式
 */
export const genScssMultipleScopeVars = (): multipleScopeVarsOptions[] => {
  const result = [] as multipleScopeVarsOptions[];
  Object.keys(themeColors).forEach(key => {
    result.push({
      scopeName: `layout-theme-${key}`,
      varsContent: `
        $subMenuActiveText: ${themeColors[key].subMenuActiveText} !default;
        $menuBg: ${themeColors[key].menuBg} !default;
        $menuHover: ${themeColors[key].menuHover} !default;
        $subMenuBg: ${themeColors[key].subMenuBg} !default;
        $subMenuActiveBg: ${themeColors[key].subMenuActiveBg} !default;
        $menuText: ${themeColors[key].menuText} !default;
        $sidebarLogo: ${themeColors[key].sidebarLogo} !default;
        $menuTitleHover: ${themeColors[key].menuTitleHover} !default;
        $menuTitleHoverBg: ${themeColors[key].menuTitleHoverBg} !default;
        $menuActiveBefore: ${themeColors[key].menuActiveBefore} !default;
        $menuBorder: ${themeColors[key].menuBorder} !default;
        $contentText: ${themeColors[key].contentText} !default;
        $dropMenuHoverBg: ${themeColors[key].dropMenuHoverBg} !default;
        $dropMenuActive: ${themeColors[key].dropMenuActive} !default;
        $dropMenuActiveBg: ${themeColors[key].dropMenuActiveBg} !default;
      `
    } as multipleScopeVarsOptions);
  });
  return result;
};
