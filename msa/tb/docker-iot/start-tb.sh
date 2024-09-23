#!/bin/bash
start-db.sh

CONF_FOLDER="/usr/share/msaiotsensingplatform/conf"
jarfile=/usr/share/msaiotsensingplatform/bin/msaiotsensingplatform.jar
configfile=msaiotsensingplatform.conf
firstlaunch=${DATA_FOLDER}/.firstlaunch

source "${CONF_FOLDER}/${configfile}"

if [ ! -f ${firstlaunch} ]; then
    install-tb.sh --loadDemo && touch ${firstlaunch}
fi

if [ -f ${firstlaunch} ]; then
    echo "Starting MSAIoTSensingPlatform ..."

    java -cp ${jarfile} $JAVA_OPTS -Dloader.main=org.thingsboard.server.ThingsboardServerApplication \
                        -Dspring.jpa.hibernate.ddl-auto=none \
                        -Dlogging.config=${CONF_FOLDER}/logback.xml \
                        org.springframework.boot.loader.PropertiesLauncher
else
    echo "ERROR: MSAIoTSensingPlatform is not installed"
fi

stop-db.sh