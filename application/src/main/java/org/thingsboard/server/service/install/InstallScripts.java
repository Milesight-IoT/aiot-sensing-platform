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
package org.thingsboard.server.service.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.rule.engine.filter.TbMsgTypeSwitchNode;
import org.thingsboard.rule.engine.flow.TbRuleChainInputNodeConfiguration;
import org.thingsboard.server.common.data.Dashboard;
import org.thingsboard.server.common.data.ResourceType;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.TbResource;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.RuleChainId;
import org.thingsboard.server.common.data.id.RuleNodeId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.oauth2.OAuth2ClientRegistrationTemplate;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.common.data.rule.RuleChain;
import org.thingsboard.server.common.data.rule.RuleChainMetaData;
import org.thingsboard.server.common.data.rule.RuleNode;
import org.thingsboard.server.common.data.rule.RuleNodeConstants;
import org.thingsboard.server.common.data.widget.WidgetTypeDetails;
import org.thingsboard.server.common.data.widget.WidgetsBundle;
import org.thingsboard.server.common.enume.MsAppContent;
import org.thingsboard.server.dao.dashboard.DashboardService;
import org.thingsboard.server.dao.exception.DataValidationException;
import org.thingsboard.server.dao.oauth2.OAuth2ConfigTemplateService;
import org.thingsboard.server.dao.relation.RelationService;
import org.thingsboard.server.dao.resource.ResourceService;
import org.thingsboard.server.dao.rule.RuleChainService;
import org.thingsboard.server.dao.widget.WidgetTypeService;
import org.thingsboard.server.dao.widget.WidgetsBundleService;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.thingsboard.server.service.install.DatabaseHelper.objectMapper;
import static org.thingsboard.server.utils.LwM2mObjectModelUtils.toLwm2mResource;

/**
 * Created by ashvayka on 18.04.18.
 */
@Component
@Slf4j
public class InstallScripts {

    public static final String APP_DIR = "application";
    public static final String SRC_DIR = "src";
    public static final String MAIN_DIR = "main";
    public static final String DATA_DIR = "data";
    public static final String JSON_DIR = "json";
    public static final String SYSTEM_DIR = "system";
    public static final String TENANT_DIR = "tenant";
    public static final String EDGE_DIR = "edge";
    public static final String DEVICE_PROFILE_DIR = "device_profile";
    public static final String DEMO_DIR = "demo";
    public static final String RULE_CHAINS_DIR = "rule_chains";
    public static final String ROOT_RULE_CHAIN_CHILD = "rule_chains_child";
    public static final String WIDGET_BUNDLES_DIR = "widget_bundles";
    public static final String OAUTH2_CONFIG_TEMPLATES_DIR = "oauth2_config_templates";
    public static final String DASHBOARDS_DIR = "dashboards";
    public static final String MODELS_LWM2M_DIR = "lwm2m-registry";
    public static final String DASHBOARDS_DEVICE = "dashboard_device_status";

    public static final String MODELS_DIR = "models";
    public static final String CREDENTIALS_DIR = "credentials";

    public static final String JSON_EXT = ".json";
    public static final String XML_EXT = ".xml";

    @Value("${install.data_dir:}")
    private String dataDir;

    @Autowired
    private RuleChainService ruleChainService;
    @Autowired
    private RelationService relationService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private WidgetTypeService widgetTypeService;

    @Autowired
    private WidgetsBundleService widgetsBundleService;

    @Autowired
    private OAuth2ConfigTemplateService oAuth2TemplateService;

    @Autowired
    private ResourceService resourceService;

    private Path getTenantRuleChainsDir() {
        return Paths.get(getDataDir(), JSON_DIR, TENANT_DIR, RULE_CHAINS_DIR);
    }

    public Path getChildTenantRuleChainsDir() {
        return Paths.get(getDataDir(), JSON_DIR, TENANT_DIR, ROOT_RULE_CHAIN_CHILD);
    }

    private Path getDeviceProfileDefaultRuleChainTemplateFilePath() {
        return Paths.get(getDataDir(), JSON_DIR, TENANT_DIR, DEVICE_PROFILE_DIR, "rule_chain_template.json");
    }

    private Path getEdgeRuleChainsDir() {
        return Paths.get(getDataDir(), JSON_DIR, EDGE_DIR, RULE_CHAINS_DIR);
    }

    private Path getTotalDevicesDir() {
        return Paths.get(getDataDir(), JSON_DIR, SYSTEM_DIR, DASHBOARDS_DEVICE, "total_devices_card.json");
    }

    private Path getInactiveDevicesDir() {
        return Paths.get(getDataDir(), JSON_DIR, SYSTEM_DIR, DASHBOARDS_DEVICE, "inactive_devices_card.json");
    }

    private Path getActiveDevicesDir() {
        return Paths.get(getDataDir(), JSON_DIR, SYSTEM_DIR, DASHBOARDS_DEVICE, "active_devices_card.json");
    }

    private Path getDashboardConfiguration() {
        return Paths.get(getDataDir(), JSON_DIR, SYSTEM_DIR, DASHBOARDS_DEVICE, "dashboard_configuration.json");
    }

    public String getDataDir() {
        if (!StringUtils.isEmpty(dataDir)) {
            if (!Paths.get(this.dataDir).toFile().isDirectory()) {
                throw new RuntimeException("'install.data_dir' property value is not a valid directory!");
            }
            return dataDir;
        } else {
            String workDir = System.getProperty("user.dir");
            if (workDir.endsWith("application")) {
                return Paths.get(workDir, SRC_DIR, MAIN_DIR, DATA_DIR).toString();
            } else {
                Path dataDirPath = Paths.get(workDir, APP_DIR, SRC_DIR, MAIN_DIR, DATA_DIR);
                if (Files.exists(dataDirPath)) {
                    return dataDirPath.toString();
                } else {
                    throw new RuntimeException("Not valid working directory: " + workDir + ". Please use either root project directory, application module directory or specify valid \"install.data_dir\" ENV variable to avoid automatic data directory lookup!");
                }
            }
        }
    }

    public void createDefaultRuleChains(TenantId tenantId) throws IOException {
        Path tenantChainsDir = getTenantRuleChainsDir();
        loadRuleChainsFromPath(tenantId, tenantChainsDir, true);
    }

    public void createDefaultEdgeRuleChains(TenantId tenantId) throws IOException {
        Path edgeChainsDir = getEdgeRuleChainsDir();
        loadRuleChainsFromPath(tenantId, edgeChainsDir, false);
    }

    private void loadRuleChainsFromPath(TenantId tenantId, Path ruleChainsPath, boolean isCoreRootInit) throws IOException {
        // 获取文件夹下的文件地址,每个地址对应一个规则链
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(ruleChainsPath,
                path -> path.toString().endsWith(InstallScripts.JSON_EXT))
        ) {
            dirStream.forEach(
                    path -> {
                        try {
                            createRuleChainFromFile(tenantId, path, null, isCoreRootInit);
                        } catch (Exception e) {
                            log.error("Unable to load rule chain from json: [{}]", path.toString());
                            throw new RuntimeException("Unable to load rule chain from json", e);
                        }
                    }
            );
        }
    }

    public RuleChain createDefaultRuleChain(TenantId tenantId, String ruleChainName) throws IOException {
        return createRuleChainFromFile(tenantId, getDeviceProfileDefaultRuleChainTemplateFilePath(), ruleChainName);
    }

    public RuleChain createRuleChainFromFile(TenantId tenantId, Path templateFilePath, String newRuleChainName) throws IOException {
        return createRuleChainFromFile(tenantId, templateFilePath, newRuleChainName, false);
    }

    public RuleChain createRuleChainFromFile(TenantId tenantId, Path templateFilePath, String newRuleChainName, boolean isCoreRootInit) throws IOException {
        JsonNode ruleChainJson = objectMapper.readTree(templateFilePath.toFile());
        RuleChain ruleChain = objectMapper.treeToValue(ruleChainJson.get("ruleChain"), RuleChain.class);
        RuleChainMetaData ruleChainMetaData = objectMapper.treeToValue(ruleChainJson.get("metadata"), RuleChainMetaData.class);

        ruleChain.setTenantId(tenantId);
        if (!StringUtils.isEmpty(newRuleChainName)) {
            ruleChain.setName(newRuleChainName);
        }
        // 特殊操作：是否初始化type = CORE 的规则链根节点
        if (isCoreRootInit) {
            // 设置子规则链
            setChildRuleChains(ruleChain, ruleChainMetaData);
        }
        // 保存规则链
        ruleChain = ruleChainService.saveRuleChain(ruleChain);

        ruleChainMetaData.setRuleChainId(ruleChain.getId());
        // 保存责任链相关的关系和节点信息
        ruleChainService.saveRuleChainMetaData(TenantId.SYS_TENANT_ID, ruleChainMetaData);

        return ruleChain;
    }

    private void setChildRuleChains(RuleChain rootRuleChain, RuleChainMetaData ruleChainMetaData) throws IOException {
        boolean root = rootRuleChain.isRoot();
        if (!root) {
            return;
        }
        Path tenantChainsDir = getChildTenantRuleChainsDir();
        // 根节点下的子规则链 key:name
        Map<String, RuleChainId> childRuleChains = new HashMap<>();
        // 获取文件夹下的文件地址,每个地址对应一个规则链
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(tenantChainsDir,
                path -> path.toString().endsWith(InstallScripts.JSON_EXT))
        ) {
            dirStream.forEach(
                    path -> {
                        try {
                            RuleChain chain = createRuleChainFromFile(rootRuleChain.getTenantId(), path, null);
                            String name = chain.getName();
                            childRuleChains.put(name, chain.getId());
                        } catch (Exception e) {
                            log.error("Unable to load rule chain from json: [{}]", path.toString());
                            throw new RuntimeException("Unable to load rule chain from json", e);
                        }
                    }
            );
        }

        // 设置Root规则链的子规则链 configuration
        setRuleChainInputNode(rootRuleChain, ruleChainMetaData, childRuleChains);
    }

    /**
     * 设置Root规则链的子规则链 configuration
     *
     * @param rootRuleChain     root规则链
     * @param ruleChainMetaData 规则链元数据
     * @param childRuleChains   子规则链ID Map
     */
    private void setRuleChainInputNode(RuleChain rootRuleChain, RuleChainMetaData ruleChainMetaData,
                                       Map<String, RuleChainId> childRuleChains) {
        ruleChainMetaData.getNodes().forEach(ruleNode -> {
            String type = ruleNode.getType();
            String name = ruleNode.getName();
            if (RuleNodeConstants.TB_RULE_CHAIN_INPUT_NODE.equals(type)) {
                try {
                    RuleChainId ruleChainId = childRuleChains.get(name);
                    log.info("initialize child nodes: rootRuleChainId={} ,ruleChainId = {} ,name = {}", rootRuleChain.getId(), ruleChainId, name);
                    if (Objects.isNull(ruleChainId)) {
                        log.error("Error initializing child nodes: The sub-rule chain ID cannot be found!");
                        throw new RuntimeException("Could not find sub-rule chain ID!");
                    }
                    TbRuleChainInputNodeConfiguration configuration = new TbRuleChainInputNodeConfiguration();
                    configuration.setRuleChainId(ruleChainId.toString());
                    JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(configuration));
                    ruleNode.setConfiguration(jsonNode);
                } catch (JsonProcessingException e) {
                    log.error("There was an error in initializing the child node. The content of the json file was read abnormally: ", e);
                    throw new RuntimeException(e.getMessage());
                }
            }
        });
    }

    public void loadSystemWidgets() throws Exception {
        Path widgetBundlesDir = Paths.get(getDataDir(), JSON_DIR, SYSTEM_DIR, WIDGET_BUNDLES_DIR);
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(widgetBundlesDir, path -> path.toString().endsWith(JSON_EXT))) {
            dirStream.forEach(
                    path -> {
                        try {
                            JsonNode widgetsBundleDescriptorJson = objectMapper.readTree(path.toFile());
                            JsonNode widgetsBundleJson = widgetsBundleDescriptorJson.get("widgetsBundle");
                            WidgetsBundle widgetsBundle = objectMapper.treeToValue(widgetsBundleJson, WidgetsBundle.class);
                            WidgetsBundle savedWidgetsBundle = widgetsBundleService.saveWidgetsBundle(widgetsBundle);
                            JsonNode widgetTypesArrayJson = widgetsBundleDescriptorJson.get("widgetTypes");
                            widgetTypesArrayJson.forEach(
                                    widgetTypeJson -> {
                                        try {
                                            WidgetTypeDetails widgetTypeDetails = objectMapper.treeToValue(widgetTypeJson, WidgetTypeDetails.class);
                                            widgetTypeDetails.setBundleAlias(savedWidgetsBundle.getAlias());
                                            widgetTypeService.saveWidgetType(widgetTypeDetails);
                                        } catch (Exception e) {
                                            log.error("Unable to load widget type from json: [{}]", path.toString());
                                            throw new RuntimeException("Unable to load widget type from json", e);
                                        }
                                    }
                            );
                        } catch (Exception e) {
                            log.error("Unable to load widgets bundle from json: [{}]", path.toString());
                            throw new RuntimeException("Unable to load widgets bundle from json", e);
                        }
                    }
            );
        }
    }

    public void loadDashboards(TenantId tenantId, CustomerId customerId) throws Exception {
        Path dashboardsDir = Paths.get(getDataDir(), JSON_DIR, DEMO_DIR, DASHBOARDS_DIR);
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dashboardsDir, path -> path.toString().endsWith(JSON_EXT))) {
            dirStream.forEach(
                    path -> {
                        try {
                            JsonNode dashboardJson = objectMapper.readTree(path.toFile());
                            Dashboard dashboard = objectMapper.treeToValue(dashboardJson, Dashboard.class);
                            dashboard.setTenantId(tenantId);
                            Dashboard savedDashboard = dashboardService.saveDashboard(dashboard);
                            if (customerId != null && !customerId.isNullUid()) {
                                dashboardService.assignDashboardToCustomer(TenantId.SYS_TENANT_ID, savedDashboard.getId(), customerId);
                            }
                        } catch (Exception e) {
                            log.error("Unable to load dashboard from json: [{}]", path.toString());
                            throw new RuntimeException("Unable to load dashboard from json", e);
                        }
                    }
            );
        }
    }

    public void loadDemoRuleChains(TenantId tenantId) {
        try {
            createDefaultRuleChains(tenantId);
            createDefaultRuleChain(tenantId, "Thermostat");
            createDefaultEdgeRuleChains(tenantId);
        } catch (Exception e) {
            log.error("Unable to load rule chain from json", e);
            throw new RuntimeException("Unable to load rule chain from json", e);
        }
    }

    public void createOAuth2Templates() throws Exception {
        Path oauth2ConfigTemplatesDir = Paths.get(getDataDir(), JSON_DIR, SYSTEM_DIR, OAUTH2_CONFIG_TEMPLATES_DIR);
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(oauth2ConfigTemplatesDir, path -> path.toString().endsWith(JSON_EXT))) {
            dirStream.forEach(
                    path -> {
                        try {
                            JsonNode oauth2ConfigTemplateJson = objectMapper.readTree(path.toFile());
                            OAuth2ClientRegistrationTemplate clientRegistrationTemplate = objectMapper.treeToValue(oauth2ConfigTemplateJson, OAuth2ClientRegistrationTemplate.class);
                            Optional<OAuth2ClientRegistrationTemplate> existingClientRegistrationTemplate =
                                    oAuth2TemplateService.findClientRegistrationTemplateByProviderId(clientRegistrationTemplate.getProviderId());
                            if (existingClientRegistrationTemplate.isPresent()) {
                                clientRegistrationTemplate.setId(existingClientRegistrationTemplate.get().getId());
                            }
                            oAuth2TemplateService.saveClientRegistrationTemplate(clientRegistrationTemplate);
                        } catch (Exception e) {
                            log.error("Unable to load oauth2 config templates from json: [{}]", path.toString());
                            throw new RuntimeException("Unable to load oauth2 config templates from json", e);
                        }
                    }
            );
        }
    }

    public void loadSystemLwm2mResources() {
        Path resourceLwm2mPath = Paths.get(dataDir, MODELS_LWM2M_DIR);
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(resourceLwm2mPath, path -> path.toString().endsWith(InstallScripts.XML_EXT))) {
            dirStream.forEach(
                    path -> {
                        try {
                            String data = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
                            TbResource tbResource = new TbResource();
                            tbResource.setTenantId(TenantId.SYS_TENANT_ID);
                            tbResource.setData(data);
                            tbResource.setResourceType(ResourceType.LWM2M_MODEL);
                            tbResource.setFileName(path.toFile().getName());
                            doSaveLwm2mResource(tbResource);
                        } catch (Exception e) {
                            log.error("Unable to load resource lwm2m object model from file: [{}]", path.toString());
                            throw new RuntimeException("resource lwm2m object model from file", e);
                        }
                    }
            );
        } catch (Exception e) {
            log.error("Unable to load resources lwm2m object model from file: [{}]", resourceLwm2mPath.toString());
            throw new RuntimeException("resource lwm2m object model from file", e);
        }
    }

    public JsonNode createBaseDashboardConfiguration() throws IOException {
        Path path = getDashboardConfiguration();
        return objectMapper.readTree(path.toFile());
    }

    private void doSaveLwm2mResource(TbResource resource) throws ThingsboardException {
        log.trace("Executing saveResource [{}]", resource);
        if (StringUtils.isEmpty(resource.getData())) {
            throw new DataValidationException("Resource data should be specified!");
        }
        toLwm2mResource(resource);
        TbResource foundResource =
                resourceService.getResource(TenantId.SYS_TENANT_ID, ResourceType.LWM2M_MODEL, resource.getResourceKey());
        if (foundResource == null) {
            resourceService.saveResource(resource);
        }
    }

    public void addOnceResultRuleChain(Tenant tenant) {
        try {
            TenantId tenantId = tenant.getTenantId();
            log.info("[{}]{} update once result rule chain start ...", tenant.getTenantId(), tenant.getName());
            RuleChain rootTenantRuleChain = ruleChainService.getRootTenantRuleChain(tenant.getId());
            RuleChainId rootTenantRuleChainId = rootTenantRuleChain.getId();
            List<RuleNode> ruleNodes = ruleChainService.findRuleNodesByTenantIdAndType(tenantId, TbMsgTypeSwitchNode.class.getName());
            if (!CollectionUtils.isEmpty(ruleNodes)) {
                RuleNode ruleNode = ruleNodes.get(0);
                RuleNodeId id = ruleNode.getId();
                UUID rootOnceDataReceivedUuid = UUID.randomUUID();
                RuleNodeId nodeId = new RuleNodeId(rootOnceDataReceivedUuid);
                // 增加一条 关系 一个子节点
                List<EntityRelation> relations = new ArrayList<>();
                EntityRelation relation = new EntityRelation();
                relation.setFrom(id);
                relation.setTo(nodeId);
                relation.setType(EntityRelation.MS_ROI_PASSAGE);
                relation.setTypeGroup(RelationTypeGroup.RULE_NODE);
                relations.add(relation);

                RuleChainId rootOnceDataReceivedRuleChainId = new RuleChainId(UUID.randomUUID());
                EntityRelation relation1 = new EntityRelation();
                relation1.setFrom(rootTenantRuleChainId);
                relation1.setTo(nodeId);
                relation1.setType(EntityRelation.CONTAINS_TYPE);
                relation1.setTypeGroup(RelationTypeGroup.RULE_CHAIN);
                relations.add(relation1);
                relationService.saveRelations(tenantId, relations);

                // 子规则链的根节点
                RuleNode rootOnceDataReceivedNode = new RuleNode();
                rootOnceDataReceivedNode.setId(nodeId);
                rootOnceDataReceivedNode.setRuleChainId(rootTenantRuleChainId);

                log.info("initialize child nodes: rootRuleChainId = {} ,ruleChainId = {} ,name = {}", rootTenantRuleChainId, rootOnceDataReceivedRuleChainId, "rootOnceDataReceivedNode");
                TbRuleChainInputNodeConfiguration configuration = new TbRuleChainInputNodeConfiguration();
                configuration.setRuleChainId(rootOnceDataReceivedRuleChainId.getId().toString());
                JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(configuration));
                rootOnceDataReceivedNode.setConfiguration(jsonNode);
                rootOnceDataReceivedNode.setName("Root Once Data Received");
                rootOnceDataReceivedNode.setType("org.thingsboard.rule.engine.flow.TbRuleChainInputNode");
                rootOnceDataReceivedNode.setDebugMode(false);
                rootOnceDataReceivedNode.setCreatedTime(System.currentTimeMillis());
                ObjectNode additionalInfo = JacksonUtil.newObjectNode();
                additionalInfo.put("description", "root-once-data-received");
                additionalInfo.put("layoutX", 370);
                additionalInfo.put("layoutY", 500);
                rootOnceDataReceivedNode.setAdditionalInfo(additionalInfo);
                ruleChainService.saveRuleNode(tenantId, rootOnceDataReceivedNode);

                // 子规则链json脚本
                Path tenantChainsDir = getChildTenantRuleChainsDir();
                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(tenantChainsDir,
                        path -> path.toString().endsWith(MsAppContent.JSON_ONCE_RESULT_RECOGNIZED_NAME))) {
                    dirStream.forEach(
                            path -> {
                                try {
                                    JsonNode ruleChainJson = objectMapper.readTree(path.toFile());
                                    RuleChain chain = objectMapper.treeToValue(ruleChainJson.get("ruleChain"), RuleChain.class);
                                    RuleChainMetaData ruleChainMetaData = objectMapper.treeToValue(ruleChainJson.get("metadata"), RuleChainMetaData.class);
                                    chain.setTenantId(tenantId);
                                    chain.setId(rootOnceDataReceivedRuleChainId);
                                    chain.setCreatedTime(System.currentTimeMillis());
                                    // 保存规则链
                                    chain = ruleChainService.saveRuleChain(chain);
                                    ruleChainMetaData.setRuleChainId(chain.getId());
                                    // 保存责任链相关的关系和节点信息
                                    ruleChainService.saveRuleChainMetaData(TenantId.SYS_TENANT_ID, ruleChainMetaData);
                                } catch (Exception e) {
                                    log.error("Unable to load rule chain from json: [{}]", path.toString());
                                    throw new RuntimeException("Unable to load rule chain from json", e);
                                }
                            }
                    );
                }
            } else {
                log.info("[{}]{} Adding OnceResultRuleChain fail message type switch node not found ...", tenantId, tenant.getName());
            }
        } catch (Exception e) {
            log.error("Unable to update Tenant", e);
        }
    }
}

