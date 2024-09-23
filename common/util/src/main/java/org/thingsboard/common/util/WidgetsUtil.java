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

package org.thingsboard.common.util;

import org.thingsboard.server.common.data.customWidget.Config;
import org.thingsboard.server.common.data.customWidget.DataKeys;
import org.thingsboard.server.common.data.customWidget.Datasources;
import org.thingsboard.server.common.data.customWidget.Settings;
import org.thingsboard.server.common.data.customWidget.WidgetDTO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author zhangzy
 * @data 2023-4-17
 * 创建小组件
 */
public class WidgetsUtil {

    public final static String TOTAL_DEVICES_MARKDOWN_TEXT_PATTERN = "<div class='card' id='goto-devices'><div class='description'>" +
            "Total devices</div><div class='value'>${totalDevicesNumber:0}</div></div>";
    public final static String TOTAL_DEVICES_MARKDOWN_CSS = ".tb-markdown-view {\n    height: 100%;\n}\n.card {\n   width: 100%;\n   " +
            "height: 100%;\n   box-sizing: border-box;\n   border: 2px solid #ABADB0;\n   border-radius: 8px;\n   " +
            "background: #FFFFFF;\n   padding: 24px 32px;\n   display: flex;\n   flex-direction: column;\n   align-items: center;\n   j" +
            "ustify-content: space-around;\n}\n\n.card:hover {\n    background: #264363;\n}\n\n.card .description {\n    font-size: 14px;\n    " +
            "font-weight: bold;\n    line-height: 21px;\n    text-align: center;\n    color: #000000;\n    opacity: 0.8;\n}\n\n.card .value {\n    " +
            "font-size: 26px;\n    font-weight: bold;\n    line-height: 39px;\n    text-align: center;\n    color: #000000;\n}\n\n@media (min-width: 960px) " +
            "and (max-width: 1200px) {\n    .card .description {\n        font-size: 12px;\n        line-height: 18px;\n    }\n\n    .card .value {\n        " +
            "font-size: 20px;\n        line-height: 28px;\n    }\n}\n\n@media (min-width: 1921px) {\n    .card .description {\n        font-size: 18px;\n        " +
            "line-height: 26px;\n    }\n\n    .card .value {\n        font-size: 32px;\n        line-height: 42px;\n    }\n}";
    public final static String ACTIVE_DEVICES_MARKDOWN_TEXT_PATTERN = "var activeDevicesNumber = (data[0]['activeDevicesNumber']).toFixed(0);\n" +
            "var totalDevicesNumber = (data[1]['totalDevicesNumber']).toFixed(0);\nvar activeDevicesPercent = " +
            "(totalDevicesNumber ? (activeDevicesNumber / totalDevicesNumber * 100) : 0).toFixed(2) + '%';\n\nreturn " +
            "'<div class=\\'card\\' id=\"goto-devices\">\\n' +\n    '    <div class=\\'description\\'>\\n' +\n    '        " +
            "Active devices\\n' +\n    '    </div>\\n' +\n    '    <div class=\\'value\\'>\\n' +\n    '       <span class=\"val\">' + " +
            "activeDevicesNumber + '</span><span class=\"percent\">' + activeDevicesPercent + '</span>\\n' +\n    '    </div> \\n' +\n    '</div>\\n';\n";
    public final static String ACTIVE_DEVICES_MARKDOWN_CSS = ".tb-markdown-view {\n    height: 100%;\n}\n.card {\n   width: 100%;\n   height: 100%;\n   " +
            "box-sizing: border-box;\n   border: 2px solid #ABADB0;\n   border-radius: 8px;\n   padding: 24px 32px;\n   display: flex;\n   " +
            "flex-direction: column;\n   align-items: center;\n   justify-content: space-around;\n}\n\n.card:hover {\n    border: 2px solid #305680;\n}\n\n." +
            "card .description {\n    font-size: 14px;\n    font-weight: bold;\n    line-height: 21px;\n    text-align: center;\n    " +
            "color: #30DA41;\n}\n\n.card .value {\n    font-size: 26px;\n    font-weight: bold;\n    line-height: 39px;\n    text-align: center;\n    " +
            "color: #0F161D;\n}\n\n.card .value .percent {\n   font-weight: 400;\n   color: #30DA41;\n}\n\n.card .value .val {\n    " +
            "padding-right: 20px;\n}\n\n@media (min-width: 960px) and (max-width: 1200px) {\n    .card .description {\n        font-size: 12px;\n        " +
            "line-height: 18px;\n    }\n\n    .card .value {\n        font-size: 20px;\n        line-height: 28px;\n    }\n    \n    " +
            ".card .value .percent {\n        display: none;\n    }\n\n    .card .value .val {\n        padding-right: 0px;\n    }\n}\n\n" +
            "@media (min-width: 1921px) {\n    .card .description {\n        font-size: 18px;\n        line-height: 26px;\n    }\n\n    " +
            ".card .value {\n        font-size: 32px;\n        line-height: 42px;\n    }\n}\n";
    public final static String IN_ACTIVE_DEVICES_MARKDOWN_TEXT_PATTERN = "var inactiveDevicesNumber=(data[0]['inactiveDevicesNumber']).toFixed(0);" +
            "var totalDevicesNumber=(data[1]['totalDevicesNumber']).toFixed(0);var inactiveDevicesPercent = " +
            "(totalDevicesNumber ? (inactiveDevicesNumber / totalDevicesNumber * 100) : 0).toFixed(2) + '%';return '" +
            "<div class=\\'card\\' id=\\'goto-devices\\'>\\n<div class=\\'description\\'>\\nInactive devices\\n</div>\\n<div class=\\'value\\'>" +
            "\\n<span class=\\'val\\'>' + inactiveDevicesNumber + '</span><span class=\\'percent\\'>' + inactiveDevicesPercent + '</span>\\</div></div>\\n';";
    public final static String IN_ACTIVE_DEVICES_MARKDOWN_CSS = ".tb-markdown-view {\n    height: 100%;\n}\n.card {\n   width: 100%;\n   height: 100%;\n   " +
            "box-sizing: border-box;\n   border: 2px solid #ABADB0;\n   border-radius: 8px;\n   padding: 24px 32px;\n   display: flex;\n   " +
            "flex-direction: column;\n   align-items: center;\n   justify-content: space-around;\n}\n\n.card:hover {\n    border: 2px solid #305680;\n}\n\n" +
            ".card .description {\n    font-size: 14px;\n    font-weight: bold;\n    line-height: 21px;\n    text-align: center;\n    color: #FF0000;\n}\n\n" +
            ".card .value {\n    font-size: 26px;\n    font-weight: bold;\n    line-height: 39px;\n    text-align: center;\n    color: #0F161D;\n}\n\n." +
            "card .value .percent {\n   font-weight: 400;\n   opacity: 0.9;\n   color: #FF0000;\n}\n\n.card .value .val {\n    padding-right: 20px;\n}\n\n" +
            "@media (min-width: 960px) and (max-width: 1200px) {\n    .card .description {\n        font-size: 12px;\n        line-height: 18px;\n    }\n\n    " +
            ".card .value {\n        font-size: 20px;\n        line-height: 28px;\n    }\n    \n    .card .value .percent {\n        display: none;\n    }\n\n    " +
            ".card .value .val {\n        padding-right: 0px;\n    }\n}\n\n@media (min-width: 1921px) {\n    .card .description {\n        font-size: 18px;\n " +
            "       line-height: 26px;\n    }\n\n    .card .value {\n        font-size: 32px;\n        line-height: 42px;\n    }\n}\n";
    public final static String COUNT = "count";
    public final static String TOTAL_DEVICES_NUMBER = "totalDevicesNumber";
    public final static String IN_ACTIVE_DEVICES_NUMBER = "inactiveDevicesNumber";
    public final static String ACTIVE_DEVICES_NUMBER = "activeDevicesNumber";
    public final static String ENTITY_COUNT = "entityCount";
    public static String TOTAL_DATA_KEY_COLOR = "#2196f3";
    public static String IN_ACTIVE_FILTER_DATA_KEY_COLOR = "#f3212a";
    public static String IN_ACTIVE_DATA_KEY_COLOR = "#4caf50";
    public static double TOTAL_DATA_KEY_HASH = 0.41137945842202006;
    public static double IN_ACTIVE_FILTER_DATA_KEY_HASH = 0.5409504120647088;
    public static double IN_ACTIVE_DATA_KEY_HASH = 0.8402163800319546;
    public static double ACTIVE_FILTER_DATA_KEY_HASH = 0.8887059257816985;
    public static double ACTIVE_DATA_KEY_HASH = 0.1476287885341887;
    public static String TOTAL_DATA_SOURCE_ENTITY_ALISA_ID = "9023e73b-0607-bd78-b5d8-c79fcf629bfa";
    public static String IN_ACTIVE_FILTER_ID = "fe9b2e71-b0d1-1b8f-2604-337a20b0f86b";
    public static String ACTIVE_FILTER_ID = "76f0d9de-da08-2ff0-3c4a-2f0212acbc19";

    public static Map<String, Object> createTotalDevice(WidgetDTO dto, UUID uuid) {
        DataKeys dataKeys = new DataKeys(COUNT, COUNT, TOTAL_DEVICES_NUMBER, TOTAL_DATA_KEY_COLOR, TOTAL_DATA_KEY_HASH);
        Settings settings = new Settings(false, TOTAL_DEVICES_MARKDOWN_TEXT_PATTERN, TOTAL_DEVICES_MARKDOWN_CSS, "");
        Datasources datasources = new Datasources(ENTITY_COUNT, TOTAL_DATA_SOURCE_ENTITY_ALISA_ID, null, toList(dataKeys));
        Config config = new Config(toList(datasources), settings);
        return WidgetsUtil.widgetAttributeMap(dto.getSizeX(), dto.getSizeY(), dto.getRow(), dto.getCol(), uuid, config, "markdown_card");
    }

    public static Map<String, Object> createInActiveDevice(WidgetDTO dto, UUID uuid) {
        DataKeys filterDatakeys = new DataKeys(COUNT, COUNT, IN_ACTIVE_DEVICES_NUMBER, IN_ACTIVE_FILTER_DATA_KEY_COLOR, IN_ACTIVE_FILTER_DATA_KEY_HASH);
        DataKeys datakeys = new DataKeys(COUNT, COUNT, TOTAL_DEVICES_NUMBER, IN_ACTIVE_DATA_KEY_COLOR, IN_ACTIVE_DATA_KEY_HASH);
        Datasources filterDatasources = new Datasources(ENTITY_COUNT, TOTAL_DATA_SOURCE_ENTITY_ALISA_ID, IN_ACTIVE_FILTER_ID, toList(filterDatakeys));
        Datasources datasources = new Datasources(ENTITY_COUNT, TOTAL_DATA_SOURCE_ENTITY_ALISA_ID, null, toList(datakeys));
        Settings settings = new Settings(true, "", IN_ACTIVE_DEVICES_MARKDOWN_CSS, IN_ACTIVE_DEVICES_MARKDOWN_TEXT_PATTERN);
        Config config = new Config(toList(filterDatasources, datasources), settings);
        return WidgetsUtil.widgetAttributeMap(dto.getSizeX(), dto.getSizeY(), dto.getRow(), dto.getCol(), uuid, config, "markdown_card");
    }

    public static Map<String, Object> createActiveDevice(WidgetDTO dto, UUID uuid) {
        DataKeys filterDatakeys = new DataKeys(COUNT, COUNT, ACTIVE_DEVICES_NUMBER, TOTAL_DATA_KEY_COLOR, ACTIVE_FILTER_DATA_KEY_HASH);
        DataKeys datakeys = new DataKeys(COUNT, COUNT, TOTAL_DEVICES_NUMBER, IN_ACTIVE_DATA_KEY_COLOR, ACTIVE_DATA_KEY_HASH);
        Datasources filterDatasources = new Datasources(ENTITY_COUNT, TOTAL_DATA_SOURCE_ENTITY_ALISA_ID, ACTIVE_FILTER_ID, toList(filterDatakeys));
        Datasources datasources = new Datasources(ENTITY_COUNT, TOTAL_DATA_SOURCE_ENTITY_ALISA_ID, null, toList(datakeys));
        // 用的是markdown function
        Settings settings = new Settings(true, "", ACTIVE_DEVICES_MARKDOWN_CSS, ACTIVE_DEVICES_MARKDOWN_TEXT_PATTERN);
        Config config = new Config(toList(filterDatasources, datasources), settings);
        return WidgetsUtil.widgetAttributeMap(dto.getSizeX(), dto.getSizeY(), dto.getRow(), dto.getCol(), uuid, config, "markdown_card");
    }

    public static Map<String, Object> createSnapshotPreview(WidgetDTO dto, UUID uuid) {
        Map<String, Object> map = new HashMap<>(16);
        map.put("sensingChannel", dto.getSensingObjectImage());
        map.put("typeAlias", "Snapshot preview");
        map.putAll(createBaseMap(dto, uuid));
        return map;
    }

    public static Map<String, Object> createAlarmTable(WidgetDTO dto, UUID uuid) {
        Map<String, Object> map = new HashMap<>(16);
        map.put("typeAlias", "Alarm table");
        map.putAll(createBaseMap(dto, uuid));
        return map;
    }

    public static Map<String, Object> createBaseMap(WidgetDTO dto, UUID uuid) {
        Map<String, Object> map = new HashMap<>(16);
        map.put("sizeX", dto.getSizeX());
        map.put("sizeY", dto.getSizeY());
        map.put("row", dto.getRow());
        map.put("col", dto.getCol());
        map.put("id", uuid);
        return map;
    }

    public static <T> List<T> toList(T... items) {
        return Arrays.asList(items);
    }

    public static Map<String, Object> fixedDashboardWidget(WidgetDTO dto, UUID uuid) {
        Map<String, Object> map = new HashMap<>(16);
        map.put("sizeX", dto.getSizeX());
        map.put("sizeY", dto.getSizeY());
        map.put("row", dto.getRow());
        map.put("col", dto.getCol());
        map.put("typeAlias", dto.getType());
        map.put("id", uuid);
        if ("Snapshot preview".equals(dto.getType())){
            Optional.ofNullable(dto.getSensingObjectImage()).ifPresent(s -> map.put("sensingChannel", dto.getSensingObjectImage()));
        }
        return map;
    }

    public static Map<String, Object> dashboardWidgetsLayout(WidgetDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("sizeX", dto.getSizeX());
        map.put("sizeY", dto.getSizeY());
        map.put("row", dto.getRow());
        map.put("col", dto.getCol());
        return map;
    }

    public static Map<String, Object> widgetAttributeMap(int sizeX, int sizeY, int row, int col, UUID uuid, Config config, String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("isSystemType", true);
        map.put("bundleAlias", "cards");
        map.put("typeAlias", type);
        map.put("type", "latest");
        map.put("title", "New widget");
        map.put("image", null);
        map.put("description", null);
        map.put("sizeX", sizeX);
        map.put("sizeY", sizeY);
        map.put("config", config);
        map.put("row", row);
        map.put("col", col);
        map.put("id", uuid);
        return map;
    }

}
