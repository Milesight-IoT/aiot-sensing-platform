<script setup lang="ts">
import Screenfull from "./screenfull/index.vue";
import mixNav from "./sidebar/mixNav.vue";
import { useNav } from "@/layout/hooks/useNav";
import Breadcrumb from "./sidebar/breadCrumb.vue";
import topCollapse from "./sidebar/topCollapse.vue";
import PwdDialog from "./sidebar/userPwd.vue";
import { useTranslationLang } from "../hooks/useTranslationLang";

const {
  layout,
  device,
  logout,
  onPanel,
  pureApp,
  username,
  authorityName,
  toggleSideBar,
  getDropdownItemStyle,
  getDropdownItemClass,
  pwdData,
  pwdDialogVisible,
  changePwd
} = useNav();

const { t, locale, translationCh, translationEn } = useTranslationLang();
</script>

<template>
  <div
    class="navbar bg-[#fff] shadow-sm shadow-[rgba(0, 21, 41, 0.08)] dark:shadow-[#0d0d0d]"
  >
    <topCollapse
      v-if="device === 'mobile'"
      class="hamburger-container"
      :is-active="pureApp.sidebar.opened"
      @toggleClick="toggleSideBar"
    />

    <Breadcrumb
      v-if="layout !== 'mix' && device !== 'mobile'"
      class="breadcrumb-container"
    />

    <mixNav v-if="layout === 'mix'" />

    <div v-if="layout === 'vertical'" class="vertical-header-right">
      <!-- 菜单搜索 -->
      <Screenfull />
      <!-- 国际化 -->
      <el-dropdown id="header-translation" trigger="click">
        <div class="w-[36px] h-[58px] flex-ac cursor-pointer navbar-bg-hover">
          <i class="icon-language font-size-20"></i>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="translation">
            <el-dropdown-item
              :class="{ 'is-check': locale === 'en' }"
              @click="translationEn"
            >
              English
            </el-dropdown-item>
            <el-dropdown-item
              :class="{ 'is-check': locale === 'zh' }"
              @click="translationCh"
            >
              简体中文
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <!-- 退出登录 -->
      <el-dropdown trigger="click">
        <span class="el-dropdown-link navbar-bg-hover select-none">
          <i class="icon-admin font-size-40"></i>
          <div>
            <p v-if="username" class="dark:text-white">{{ username }}</p>
            <p v-if="authorityName" class="dark:text-white">
              {{ authorityName }}
            </p>
          </div>
          <i class="icon-more-vertical font-size-20"></i>
        </span>
        <template #dropdown>
          <el-dropdown-menu class="logout">
            <el-dropdown-item @click="changePwd">
              <i class="icon-lock text-lg"></i>
              {{ t("buttons.security") }}
            </el-dropdown-item>
            <el-dropdown-item @click="logout">
              <i class="icon-logout text-lg"></i>
              {{ t("buttons.loginOut") }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
  <PwdDialog
    v-model:visible="pwdDialogVisible"
    :data="pwdData"
    @logout="logout"
  />
</template>

<style lang="scss" scoped>
.navbar {
  width: 100%;
  height: 58px;
  overflow: hidden;

  .hamburger-container {
    line-height: 58px;
    height: 100%;
    float: left;
    cursor: pointer;
  }

  .icon-language {
    color: #000000d9;
  }
  .vertical-header-right {
    display: flex;
    min-width: 280px;
    height: 58px;
    align-items: center;
    color: #000000d9;
    justify-content: flex-end;

    .el-dropdown-link {
      height: 58px;
      padding: 10px;
      display: flex;
      align-items: center;
      justify-content: space-around;
      cursor: pointer;
      color: #000000d9;

      p {
        font-size: 14px;
      }

      img {
        width: 22px;
        height: 22px;
        border-radius: 50%;
      }
    }
  }

  .breadcrumb-container {
    float: left;
    margin-left: 16px;
  }
}

.translation {
  ::v-deep(.el-dropdown-menu__item) {
    padding: 5px 40px;
  }

  .check-zh {
    position: absolute;
    left: 20px;
  }

  .check-en {
    position: absolute;
    left: 20px;
  }
}

.logout {
  ::v-deep(.el-dropdown-menu__item) {
    min-width: 100%;
    display: inline-flex;
    flex-wrap: wrap;
  }
}
</style>
