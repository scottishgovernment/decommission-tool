#!/bin/sh -l
exec /usr/bin/java -Dlogging.config=/opt/decommission-tool/logback.xml -jar /opt/decommission-tool/decommission-tool.jar >> /var/log/decommission-tool/decommission-tool.log 2>&1
