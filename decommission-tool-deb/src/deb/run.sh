#!/bin/sh -l
exec /usr/bin/java \
  -XX:+UseParallelGC \
  -XX:GCTimeRatio=4 \
  -XX:AdaptiveSizePolicyWeight=90 \
  -verbosegc \
  -XX:+PrintGC \
  -XX:+PrintGCDetails \
  -XX:+PrintGCDateStamps \
  -XX:+PrintTenuringDistribution \
  -XX:+UseGCLogFileRotation \
  -XX:GCLogFileSize=1m \
  -XX:NumberOfGCLogFiles=8 \
  -Xloggc:/var/log/decommission-tool/gc.log \
  -Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=18089 \
  -Dcom.sun.management.jmxremote.local.only=false \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -Dlogging.config=/opt/decommission-tool/logback.xml \
  -jar \
  /opt/decommission-tool/decommission-tool.jar \
  >> /var/log/decommission-tool/decommission-tool.log \
  2>&1
