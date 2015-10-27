
cd /tmp
export JAVA_HOME=/home/surfboard/Tools/jdk1.8.0_60
export JAVA_GATEWAY=/home/surfboard/Surfboard/service
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/home/surfboard/Tools
#export JAVA_HOME=/java/jdk1.8.0
$JAVA_HOME/bin/java -Djava.library.path=/usr/lib/rxtx:/usr/lib/jni -cp "$JAVA_GATEWAY/target/iot-surfing-service-1.0-SNAPSHOT.jar:$JAVA_GATEWAY/target/dependency/*" org.surfing.Kernel
