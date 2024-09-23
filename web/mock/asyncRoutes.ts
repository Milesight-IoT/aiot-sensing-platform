// 模拟后端动态生成路由
import { MockMethod } from "vite-plugin-mock";
import { system } from "@/router/enums";

/**
 * roles：页面级别权限，这里模拟二种 "admin"、"common"
 * admin：管理员角色
 * common：普通角色
 */

const systemRouter = {
  path: "/settings",
  meta: {
    icon: "icon-system-settings",
    title: "menus.systemSettings",
    rank: system
  }
};


export default [
  {
    url: "/getAsyncRoutes",
    method: "get",
    response: () => {
      return {
        success: true,
        data: []
      };
    }
  }
] as MockMethod[];
