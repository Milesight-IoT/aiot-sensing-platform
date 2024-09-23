#!/bin/bash
CASSANDRA_PID=$(ps aux | grep '[c]assandra' | awk '{print $2}')

echo "Stopping cassandra (pid ${CASSANDRA_PID})."
kill -SIGTERM ${CASSANDRA_PID}

PG_CTL=$(find /usr/lib/postgresql/ -name pg_ctl)
echo "Stopping postgres."
${PG_CTL} stop

while [ -e /proc/${CASSANDRA_PID} ]
do
    echo "Waiting for cassandra to stop."
    sleep 2
done
echo "Cassandra was stopped."
