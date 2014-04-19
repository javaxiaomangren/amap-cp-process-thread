=====project description
build project

mvn clean;
mvn -Pproduction assembly:assembly  -Dmaven.test.skip=true

tar -zxvf *-bin.tar.gz

sh run.sh > /dev/null &

tailf logs/data-cp.log




java -cp ./cp-libs  -jar ./cp-libs/amap-cp-process-thread-1.0-SNAPSHOT.jar residentiona_jiaodian_api 16 -Dlog4j.configuration=log4j.xml > /dev/null 2>&1