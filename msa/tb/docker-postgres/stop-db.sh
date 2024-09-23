#!/bin/bash
PG_CTL=$(find /usr/lib/postgresql/ -name pg_ctl)

${PG_CTL} stop
