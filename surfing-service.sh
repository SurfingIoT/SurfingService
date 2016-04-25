java -classpath "./target/iot-surfing-service-1.0-SNAPSHOT.jar:./target/dependency/*" -Dsurfing.config="etc/config"  org.surfing.kernel.Kernel $1
#Use this to use a custom JDK
#export JAVA_HOME=/jdk1.8.0_60
#$JAVA_HOME/bin/java -classpath "target\iot-surfing-service-1.0-SNAPSHOT.jar:target\dependency\*" -Dsurfing.config="etc/config"  org.surfing.kernel.Kernel %1



