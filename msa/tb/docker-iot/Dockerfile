FROM thingsboard/openjdk11:bullseye-slim

# sys version
ENV PLATFORM_SERVICE_VERSION="1.0.1.1-r4"

ENV PG_MAJOR=12
ENV DATA_FOLDER=/data

ENV HTTP_BIND_PORT=9090
ENV MQTT_BIND_PORT=1883
ENV DATABASE_TS_TYPE=cassandra

ENV PGDATA=/data/db
ENV CASSANDRA_DATA=/data/cassandra

ENV SPRING_DRIVER_CLASS_NAME=org.postgresql.Driver
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/msaiotsensingplatform
ENV SPRING_DATASOURCE_USERNAME=msaiotsensingplatform
ENV SPRING_DATASOURCE_PASSWORD=password

ENV CASSANDRA_KEYSPACE_NAME=msaiotsensingplatform
ENV CASSANDRA_HOST=localhost
ENV CASSANDRA_PORT=9042
ENV CASSANDRA_HOME=/opt/cassandra
ENV CASSANDRA_URL=localhost:9042
ENV CASSANDRA_KEYSPACE_NAME=msaiotsensingplatform
# delete the configuration
ENV OPEN_CLEAN_DISK=true
ENV COMPRESSED_DIRECTORY=/data
ENV DISK_ALARM_SPACE=20

ENV PATH=$CASSANDRA_HOME/bin:$PATH
ENV PATH=$PATH:/usr/lib/postgresql/$PG_MAJOR/bin

ENV PGLOG=/var/log/postgres
ENV CASSANDRA_LOG=/var/log/cassandra

COPY logback.xml msaiotsensingplatform.conf start-db.sh stop-db.sh start-tb.sh upgrade-tb.sh install-tb.sh msaiotsensingplatform.deb /tmp/


RUN apt-get update \
    && apt-get install -y --no-install-recommends wget nmap procps gnupg2 \
    && echo "deb http://apt.postgresql.org/pub/repos/apt/ $(. /etc/os-release && echo -n $VERSION_CODENAME)-pgdg main" | tee --append /etc/apt/sources.list.d/pgdg.list > /dev/null \
    && wget -q https://www.postgresql.org/media/keys/ACCC4CF8.asc -O- | apt-key add - \
    && echo "deb https://debian.cassandra.apache.org 40x main" | tee -a /etc/apt/sources.list.d/cassandra.sources.list > /dev/null \
    && wget -q https://downloads.apache.org/cassandra/KEYS -O- | apt-key add - \
    && apt-get update \
    && apt-get install -y --no-install-recommends cassandra cassandra-tools postgresql-${PG_MAJOR} \
    && rm -rf /var/lib/apt/lists/* \
    && update-rc.d cassandra disable \
    && update-rc.d postgresql disable \
    && apt-get purge -y --auto-remove \
    && sed -i.old '/ulimit/d' /etc/init.d/cassandra \
    && mv /tmp/start-tb.sh /usr/bin \
    && mv /tmp/upgrade-tb.sh /usr/bin \
    && mv /tmp/install-tb.sh /usr/bin \
    && mv /tmp/start-db.sh /usr/bin \
    && mv /tmp/stop-db.sh /usr/bin  \
    && chmod a+x /usr/bin/*.sh \
    && dpkg -i /tmp/msaiotsensingplatform.deb \
    && rm /tmp/msaiotsensingplatform.deb \
    && (systemctl --no-reload disable --now thingsboard.service > /dev/null 2>&1 || :) \
    && mv /tmp/logback.xml /usr/share/msaiotsensingplatform/conf \
    && mv /tmp/msaiotsensingplatform.conf /usr/share/msaiotsensingplatform/conf \
# postgres config
    && mkdir -p $PGLOG \
    && chown -R msaiotsensingplatform:msaiotsensingplatform $PGLOG \
    && chown -R msaiotsensingplatform:msaiotsensingplatform /var/log/postgresql \
    && chown -R msaiotsensingplatform:msaiotsensingplatform /var/run/postgresql \
# cassandra config
    && rm -rf /var/lib/cassandra \
    && chmod a+w /var/lib \
    && chown -R msaiotsensingplatform:msaiotsensingplatform $CASSANDRA_LOG \
    && mkdir -p $DATA_FOLDER \
    && chown -R msaiotsensingplatform:msaiotsensingplatform $DATA_FOLDER \
    && chown -R msaiotsensingplatform:msaiotsensingplatform /var/log/msaiotsensingplatform \
    && chmod 777 -R /var/log/msaiotsensingplatform \
    && chmod 555 /usr/share/msaiotsensingplatform/bin/msaiotsensingplatform.jar



USER msaiotsensingplatform

EXPOSE 7070
EXPOSE 9090
EXPOSE 1883
EXPOSE 5432
EXPOSE 9042
EXPOSE 5683/udp
EXPOSE 5685/udp

VOLUME ["/data"]

CMD ["start-tb.sh"]
