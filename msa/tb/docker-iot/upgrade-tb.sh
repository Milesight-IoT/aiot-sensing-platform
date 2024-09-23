#!/bin/bash
start-db.sh
CONF_FOLDER="/usr/share/msaiotsensingplatform/conf"
jarfile=/usr/share/msaiotsensingplatform/bin/msaiotsensingplatform.jar
configfile=msaiotsensingplatform.conf
upgradeversion=${DATA_FOLDER}/.upgradeversion

source "${CONF_FOLDER}/${configfile}"

FROM_VERSION=`cat ${upgradeversion}`

echo "Starting MsaiotSensingPlatform upgrade ..."

if [[ -z "${FROM_VERSION// }" ]]; then
    echo "FROM_VERSION variable is invalid or unspecified!"
    exit 1
else
    fromVersion="${FROM_VERSION// }"
fi

java -cp ${jarfile} $JAVA_OPTS -Dloader.main=org.thingsboard.server.ThingsboardInstallApplication \
                -Dspring.jpa.hibernate.ddl-auto=none \
                -Dinstall.upgrade=true \
                -Dinstall.upgrade.from_version=${fromVersion} \
                -Dlogging.config=/usr/share/msaiotsensingplatform/bin/install/logback.xml \
                org.springframework.boot.loader.PropertiesLauncher
echo "${PLATFORM_SERVICE_VERSION}" > ${upgradeversion}

stop-db.sh