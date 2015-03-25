#!/bin/sh -l
exec /usr/bin/java -jar /opt/decommission-tool/decommission-tool.jar >> /var/log/decommission-tool/decommission-tool.log 2>&1
