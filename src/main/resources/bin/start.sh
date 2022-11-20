#!/bin/bash

echo 开始重新端点摘要存证服务

pid=`ps -ef | grep endpoint-chain-demo-1.0.1.jar | grep -v grep | awk '{print $2}'`

#if [ "$pid" != "" ]; then

kill -9 $pid

#else

echo 开始启动端点摘要存证服务

nohup java -jar endpoint-chain-demo-1.0.1.jar --spring.config.location=conf/application.properties --logging.config=conf/logback-boot.xml  -Xms512m -Xmx512m  > nohup.out 2>&1 &

pid=`ps -ef | grep  endpoint-chain-demo-1.0.1.jar | grep -v grep | awk '{print $2}'`

echo 端点摘要存证已启动pid:$pid

#fi
