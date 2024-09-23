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
package org.thingsboard.server.install;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.thingsboard.server.service.component.ComponentDiscoveryService;
import org.thingsboard.server.service.install.DatabaseEntitiesUpgradeService;
import org.thingsboard.server.service.install.DatabaseTsUpgradeService;
import org.thingsboard.server.service.install.EntityDatabaseSchemaService;
import org.thingsboard.server.service.install.InstallScripts;
import org.thingsboard.server.service.install.NoSqlKeyspaceService;
import org.thingsboard.server.service.install.SystemDataLoaderService;
import org.thingsboard.server.service.install.TsDatabaseSchemaService;
import org.thingsboard.server.service.install.TsLatestDatabaseSchemaService;
import org.thingsboard.server.service.install.migrate.EntitiesMigrateService;
import org.thingsboard.server.service.install.migrate.TsLatestMigrateService;
import org.thingsboard.server.service.install.update.CacheCleanupService;
import org.thingsboard.server.service.install.update.DataUpdateService;

import java.util.Iterator;
import java.util.List;

import static org.thingsboard.server.service.install.update.DefaultDataUpdateService.getEnv;

@Service
@Profile("install")
@Slf4j
public class ThingsboardInstallService {

    @Value("${install.upgrade:false}")
    private Boolean isUpgrade;

    @Value("${install.upgrade.from_version:1.2.3}")
    private String upgradeFromVersion;

    @Value("${install.load_demo:false}")
    private Boolean loadDemo;

    @Value("${state.persistToTelemetry:false}")
    private boolean persistToTelemetry;

    @Autowired
    private EntityDatabaseSchemaService entityDatabaseSchemaService;

    @Autowired(required = false)
    private NoSqlKeyspaceService noSqlKeyspaceService;

    @Autowired
    private TsDatabaseSchemaService tsDatabaseSchemaService;

    @Autowired(required = false)
    private TsLatestDatabaseSchemaService tsLatestDatabaseSchemaService;

    @Autowired
    private DatabaseEntitiesUpgradeService databaseEntitiesUpgradeService;

    @Autowired(required = false)
    private DatabaseTsUpgradeService databaseTsUpgradeService;

    @Autowired
    private ComponentDiscoveryService componentDiscoveryService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SystemDataLoaderService systemDataLoaderService;

    @Autowired
    private DataUpdateService dataUpdateService;

    @Autowired
    private CacheCleanupService cacheCleanupService;

    @Autowired(required = false)
    private EntitiesMigrateService entitiesMigrateService;

    @Autowired(required = false)
    private TsLatestMigrateService latestMigrateService;
    /**
     * 历史版本链，不包含当前版本
     */
    static final Iterator<String> ITERATOR = List.of(
            "1.0.0.1",
            "1.0.1.0",
            "1.0.1.1",
            "1.0.1.0-r1",
            "1.0.1.1-r1",
            "1.0.1.1-r2",
            "1.0.1.1-r3"
    ).iterator();

    @Autowired
    private InstallScripts installScripts;

    public void performInstall() {
        try {
            if (isUpgrade) {
                String currentVersion = upgradeFromVersion;
                while (ITERATOR.hasNext()) {
                    String element = ITERATOR.next();
                    boolean equals = currentVersion.equals(element);
                    if (equals) {
                        currentVersion = upgrade(currentVersion);
                    }
                }
            } else {
                initialInstallation();
            }
        } catch (Exception e) {
            log.error("Unexpected error during MSAIoTSensingPlatform installation!", e);
            throw new ThingsboardInstallException("Unexpected error during MSAIoTSensingPlatform installation!", e);
        } finally {
            SpringApplication.exit(context);
        }
    }

    private String upgrade(String currentVersion) throws Exception {
        log.info("Starting MSAIoTSensingPlatform Upgrade from version {} ...", currentVersion);
        cacheCleanupService.clearCache(currentVersion);
        String nextVersion;
        switch (currentVersion) {
            case "3.4.3":
                log.info("Upgrading MSAIoTSensingPlatform from version 3.4.3 to 1.0.0.1 ...");
                nextVersion = "1.0.0.1";
                break;
            case "1.0.0.1":
                log.info("Upgrading MSAIoTSensingPlatform from version 1.0.0.1 to 1.0.1.1 ...");
                // thingsboard升级到3.5
                upgrade3_5_0();
                // 数据库脚本
                databaseEntitiesUpgradeService.upgradeDatabase("1.0.1.1");
                // 数据处理方法
                dataUpdateService.updateData("1.0.1.1");

                // 1.0.0.1版本未执行、这版本加上
                if (noSqlKeyspaceService != null) {
                    noSqlKeyspaceService.createDatabaseSchema();
                }
                tsDatabaseSchemaService.createDatabaseSchema();
                if (tsLatestDatabaseSchemaService != null) {
                    tsLatestDatabaseSchemaService.createDatabaseSchema();
                }
                nextVersion = "1.0.1.1";
                break;
            case "1.0.1.0":
                log.info("Upgrading MSAIoTSensingPlatform from version 1.0.1.0 to 1.0.1.1 ...");
                nextVersion = "1.0.1.1";
                break;
            case "1.0.1.1":
                log.info("Upgrading MSAIoTSensingPlatform from version 1.0.1.1 to 1.0.1.1-r1 ...");
                dataUpdateService.updateData("1.0.1.1-r1");
                nextVersion = "1.0.1.1-r1";
                break;
            case "1.0.1.1-r1":
            case "1.0.1.0-r1":
                log.info("Upgrading MSAIoTSensingPlatform from version 1.0.1.1-r1 to 1.0.1.1-r2 ...");
                nextVersion = "1.0.1.1-r2";
                break;
            case "1.0.1.1-r2":
                log.info("Upgrading MSAIoTSensingPlatform from version 1.0.1.1-r2 to 1.0.1.1-r3 ...");
                nextVersion = "1.0.1.1-r3";
                break;
            case "1.0.1.1-r3":
                nextVersion = "1.0.1.1-r4";
                log.info("Upgrading MSAIoTSensingPlatform from version {} to {} ...", currentVersion, nextVersion);
                break;
            default:
                throw new RuntimeException("Unable to upgrade MSAIoTSensingPlatform, unsupported fromVersion: " + upgradeFromVersion);
        }
        log.info("Upgrade finished successfully!");
        return nextVersion;
    }

    private void initialInstallation() throws Exception {
        log.info("Starting MSAIoTSensingPlatform Installation...");
        log.info("Installing DataBase schema for entities...");
        entityDatabaseSchemaService.createDatabaseSchema();

        entityDatabaseSchemaService.createOrUpdateViewsAndFunctions();
        entityDatabaseSchemaService.createOrUpdateDeviceInfoView(persistToTelemetry);

        log.info("Installing DataBase schema for timeseries...");

        if (noSqlKeyspaceService != null) {
            noSqlKeyspaceService.createDatabaseSchema();
        }

        tsDatabaseSchemaService.createDatabaseSchema();

        if (tsLatestDatabaseSchemaService != null) {
            tsLatestDatabaseSchemaService.createDatabaseSchema();
        }

        log.info("Loading system data...");

        componentDiscoveryService.discoverComponents();

        systemDataLoaderService.createSysAdmin();
        systemDataLoaderService.createDefaultTenantProfiles();
        systemDataLoaderService.createAdminSettings();
        systemDataLoaderService.createRandomJwtSettings();
        systemDataLoaderService.loadSystemWidgets();
        systemDataLoaderService.createOAuth2Templates();
        systemDataLoaderService.createQueues();
        systemDataLoaderService.createDefaultNotificationConfigs();

        // systemDataLoaderService.loadSystemPlugins();
        // systemDataLoaderService.loadSystemRules();
        installScripts.loadSystemLwm2mResources();

        if (loadDemo) {
            // 初始化数据
            log.info("Loading demo data...");
            systemDataLoaderService.createDefaultData();
            systemDataLoaderService.updateDashboardAndWidgets();
        }
        // if (loadDemo) {
        //     systemDataLoaderService.loadDemoData();
        // }
        log.info("Installation finished successfully!");
    }

    private void upgrade3_5_0() throws Exception {
        log.info("Upgrading ThingsBoard from version 3.4.4 to 3.5.0 ...");
        databaseEntitiesUpgradeService.upgradeDatabase("3.4.4");
        entityDatabaseSchemaService.createOrUpdateViewsAndFunctions();
        entityDatabaseSchemaService.createOrUpdateDeviceInfoView(persistToTelemetry);
        log.info("Updating system data...");
        systemDataLoaderService.updateSystemWidgets();
        if (!getEnv("SKIP_DEFAULT_NOTIFICATION_CONFIGS_CREATION", false)) {
            systemDataLoaderService.createDefaultNotificationConfigs();
        } else {
            log.info("Skipping default notification configs creation");
        }
        installScripts.loadSystemLwm2mResources();
    }

}

