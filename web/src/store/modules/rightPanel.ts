import { defineStore } from "pinia";
import { store } from "@/store";
import { rightPanelType } from "./types";
import { storageLocal } from "@pureadmin/utils";

export const useRightPanelStore = defineStore({
  id: "right-panel",
  state: (): rightPanelType => ({
    title: storageLocal().getItem<rightPanelType>("rightPanel")?.title ?? 'Title',
    subTitle: storageLocal().getItem<rightPanelType>("rightPanel")?.subTitle ?? '',
    showPanel: storageLocal().getItem<rightPanelType>("rightPanel")?.showPanel?? false
  }),
  getters: {
    getTitle() {
      return this.title;
    },
    getSubTitle() {
      return this.subTitle;
    },
    getShowPanel() {
      return this.showPanel;
    }
  },
  actions: {
    SET_RIGHTPANEL(rightPanel) {
      // eslint-disable-next-line no-prototype-builtins
      storageLocal().setItem("responsive-layout", rightPanel);
    },
    changeStatus(showPanel?: boolean) {
      this.showPanel = showPanel;
    }
  }
});

export function useRightPanelStoreHook() {
  return useRightPanelStore(store);
}
