#!/bin/bash
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    --loadDemo)
    LOAD_DEMO=true
    shift # past argument
    ;;
    *)
            # unknown option
    ;;
esac
shift # past argument or value
done

if [ "$LOAD_DEMO" == "true" ]; then
    loadDemo=true
else
    loadDemo=false
fi

CONF_FOLDER="/usr/share/msaiotsensingplatform/conf"
jarfile=/usr/share/msaiotsensingplatform/bin/msaiotsensingplatform.jar
configfile=msaiotsensingplatform.conf
upgradeversion=${DATA_FOLDER}/.upgradeversion

source "${CONF_FOLDER}/${configfile}"

echo "Starting MSAIoTSensingPlatform installation ..."

set -e

java -cp ${jarfile} $JAVA_OPTS -Dloader.main=org.thingsboard.server.ThingsboardInstallApplication \
                    -Dinstall.load_demo=${loadDemo} \
                    -Dspring.jpa.hibernate.ddl-auto=none \
                    -Dinstall.upgrade=false \
                    -Dlogging.config=/usr/share/msaiotsensingplatform/bin/install/logback.xml \
                    org.springframework.boot.loader.PropertiesLauncher

echo "${PLATFORM_SERVICE_VERSION}" > ${upgradeversion}
