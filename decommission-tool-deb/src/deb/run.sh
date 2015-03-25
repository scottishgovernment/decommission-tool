#!/bin/sh -l
exec /usr/bin/java -jar /opt/healthcheck/healthcheck.jar >> /var/log/healthcheck/healthcheck.log 2>&1
