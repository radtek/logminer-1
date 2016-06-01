JAVA_HOME="/home/oracle/OBase/jdk1.7.0_80"

RUNNING_USER=root

APP_HOME=/home/oracle/OBase/logminerplus

APP_MAINCLASS=com.logminerplus.gui.Main
 
CLASSPATH=$APP_HOME/:$APP_HOME/classes
for i in "$APP_HOME"/lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

JAVA_OPTS="-ms512m -mx512m -Xmn256m -XX:MaxPermSize=128m"
 
JAVA_CMD="$JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH $APP_MAINCLASS"

su - $RUNNING_USER -c "$JAVA_CMD"
     