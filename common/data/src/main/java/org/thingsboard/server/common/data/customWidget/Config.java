/*
 * Copyright © 2016-2023 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.thingsboard.server.common.data.customWidget;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhangzy
 * 部件配置
 * @Data 2023.04.14
 */
@Data
@NoArgsConstructor
public class Config {
    private List<Datasources> datasources;
    private Timewindow timewindow;
    private boolean showTitle;
    private String backgroundColor;
    private String color;
    private String padding;
    private Settings settings;
    private String title;
    private boolean showTitleIcon;
    private String iconColor;
    private String iconSize;
    private String titleTooltip;
    private boolean dropShadow;
    private boolean enableFullscreen;
    private WidgetStyle widgetStyle;
    private TitleStyle titleStyle;
    private boolean showLegend;
    private boolean useDashboardTimewindow;
    private String widgetCss;
    private int pageSize;
    private String noDataDisplayMessage;

    public Config(List<Datasources> datasources, Settings settings) {
        this.datasources = datasources;
        this.timewindow = new Timewindow();
        this.showTitle = false;
        this.backgroundColor = "#fff";
        this.color = "rgba(0, 0, 0, 0.87)";
        this.padding = "0px";
        this.settings = settings;
        this.title = "New widget";
        this.showTitleIcon = false;
        this.iconColor = "rgba(0, 0, 0, 0.87)";
        this.iconSize = "24px";
        this.titleTooltip = "";
        this.dropShadow = false;
        this.enableFullscreen = false;
        this.titleStyle = new TitleStyle("16px", 400);
        this.showLegend = false;
        this.useDashboardTimewindow = true;
        this.widgetCss = "";
        this.pageSize = 1024;
        this.noDataDisplayMessage = "";
    }
}
