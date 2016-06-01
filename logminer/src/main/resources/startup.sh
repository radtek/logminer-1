#!/bin/sh
#璇ヨ剼鏈负Linux涓嬪惎鍔╦ava绋嬪簭鐨勯�鐢ㄨ剼鏈�鍗冲彲浠ヤ綔涓哄紑鏈鸿嚜鍚姩service鑴氭湰琚皟鐢紝
#涔熷彲浠ヤ綔涓哄惎鍔╦ava绋嬪簭鐨勭嫭绔嬭剼鏈潵浣跨敤銆�#
#
#璀﹀憡!!!锛氳鑴氭湰stop閮ㄥ垎浣跨敤绯荤粺kill鍛戒护鏉ュ己鍒剁粓姝㈡寚瀹氱殑java绋嬪簭杩涚▼銆�#鍦ㄦ潃姝昏繘绋嬪墠锛屾湭浣滀换浣曟潯浠舵鏌ャ�鍦ㄦ煇浜涙儏鍐典笅锛屽绋嬪簭姝ｅ湪杩涜鏂囦欢鎴栨暟鎹簱鍐欐搷浣滐紝
#鍙兘浼氶�鎴愭暟鎹涪澶辨垨鏁版嵁涓嶅畬鏁淬�濡傛灉蹇呴』瑕佽�铏戝埌杩欑被鎯呭喌锛屽垯闇�鏀瑰啓姝よ剼鏈紝
#澧炲姞鍦ㄦ墽琛宬ill鍛戒护鍓嶇殑涓�郴鍒楁鏌ャ�
#
#
###################################
#鐜鍙橀噺鍙婄▼搴忔墽琛屽弬鏁�#闇�鏍规嵁瀹為檯鐜浠ュ強Java绋嬪簭鍚嶇О鏉ヤ慨鏀硅繖浜涘弬鏁�###################################
#JDK鎵�湪璺緞
JAVA_HOME="/home/wenghaixing/jdk1.8.0_73"
 
#鎵ц绋嬪簭鍚姩鎵�娇鐢ㄧ殑绯荤粺鐢ㄦ埛锛岃�铏戝埌瀹夊叏锛屾帹鑽愪笉浣跨敤root甯愬彿
RUNNING_USER=root
 
#Java绋嬪簭鎵�湪鐨勭洰褰曪紙classes鐨勪笂涓�骇鐩綍锛�APP_HOME=$(pwd)
 
#闇�鍚姩鐨凧ava涓荤▼搴忥紙main鏂规硶绫伙級
APP_MAINCLASS=com.logminerplus.gui.Main
 
#鎷煎噾瀹屾暣鐨刢lasspath鍙傛暟锛屽寘鎷寚瀹歭ib鐩綍涓嬫墍鏈夌殑jar
CLASSPATH=$APP_HOME/:$APP_HOME/classes
for i in "$APP_HOME"/lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done
 
#java铏氭嫙鏈哄惎鍔ㄥ弬鏁�JAVA_OPTS="-ms512m -mx512m -Xmn256m -XX:MaxPermSize=128m"
 
###################################
#(鍑芥暟)鍒ゆ柇绋嬪簭鏄惁宸插惎鍔�#
#璇存槑锛�#浣跨敤JDK鑷甫鐨凧PS鍛戒护鍙奼rep鍛戒护缁勫悎锛屽噯纭煡鎵緋id
#jps 鍔�l 鍙傛暟锛岃〃绀烘樉绀簀ava鐨勫畬鏁村寘璺緞
#浣跨敤awk锛屽垎鍓插嚭pid ($1閮ㄥ垎)锛屽強Java绋嬪簭鍚嶇О($2閮ㄥ垎)
###################################
#鍒濆鍖杙sid鍙橀噺锛堝叏灞�級
psid=0
 
checkpid() {
   javaps=`$JAVA_HOME/bin/jps -l | grep $APP_MAINCLASS`
 
   if [ -n "$javaps" ]; then
      psid=`echo $javaps | awk '{print $1}'`
   else
      psid=0
   fi
}
 
###################################
#(鍑芥暟)鍚姩绋嬪簭
#
#璇存槑锛�#1. 棣栧厛璋冪敤checkpid鍑芥暟锛屽埛鏂�psid鍏ㄥ眬鍙橀噺
#2. 濡傛灉绋嬪簭宸茬粡鍚姩锛�psid涓嶇瓑浜�锛夛紝鍒欐彁绀虹▼搴忓凡鍚姩
#3. 濡傛灉绋嬪簭娌℃湁琚惎鍔紝鍒欐墽琛屽惎鍔ㄥ懡浠よ
#4. 鍚姩鍛戒护鎵ц鍚庯紝鍐嶆璋冪敤checkpid鍑芥暟
#5. 濡傛灉姝ラ4鐨勭粨鏋滆兘澶熺‘璁ょ▼搴忕殑pid,鍒欐墦鍗癧OK]锛屽惁鍒欐墦鍗癧Failed]
#娉ㄦ剰锛歟cho -n 琛ㄧず鎵撳嵃瀛楃鍚庯紝涓嶆崲琛�#娉ㄦ剰: "nohup 鏌愬懡浠�>/dev/null 2>&1 &" 鐨勭敤娉�###################################
start() {
   checkpid
 
   if [ $psid -ne 0 ]; then
      echo "================================"
      echo "warn: $APP_MAINCLASS already started! (pid=$psid)"
      echo "================================"
   else
      echo -n "Starting $APP_MAINCLASS ..."
      JAVA_CMD="$JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH $APP_MAINCLASS"
      su - $RUNNING_USER -c "$JAVA_CMD"
      checkpid
      if [ $psid -ne 0 ]; then
         echo "(pid=$psid) [OK]"
      else
         echo "[Failed]"
      fi
   fi
}
 
###################################
#(鍑芥暟)鍋滄绋嬪簭
#
#璇存槑锛�#1. 棣栧厛璋冪敤checkpid鍑芥暟锛屽埛鏂�psid鍏ㄥ眬鍙橀噺
#2. 濡傛灉绋嬪簭宸茬粡鍚姩锛�psid涓嶇瓑浜�锛夛紝鍒欏紑濮嬫墽琛屽仠姝紝鍚﹀垯锛屾彁绀虹▼搴忔湭杩愯
#3. 浣跨敤kill -9 pid鍛戒护杩涜寮哄埗鏉�杩涚▼
#4. 鎵цkill鍛戒护琛岀揣鎺ュ叾鍚庯紝椹笂鏌ョ湅涓婁竴鍙ュ懡浠ょ殑杩斿洖鍊� $?
#5. 濡傛灉姝ラ4鐨勭粨鏋�?绛変簬0,鍒欐墦鍗癧OK]锛屽惁鍒欐墦鍗癧Failed]
#6. 涓轰簡闃叉java绋嬪簭琚惎鍔ㄥ娆★紝杩欓噷澧炲姞鍙嶅妫�煡杩涚▼锛屽弽澶嶆潃姝荤殑澶勭悊锛堥�褰掕皟鐢╯top锛夈�
#娉ㄦ剰锛歟cho -n 琛ㄧず鎵撳嵃瀛楃鍚庯紝涓嶆崲琛�#娉ㄦ剰: 鍦╯hell缂栫▼涓紝"$?" 琛ㄧず涓婁竴鍙ュ懡浠ゆ垨鑰呬竴涓嚱鏁扮殑杩斿洖鍊�###################################
stop() {
   checkpid
 
   if [ $psid -ne 0 ]; then
      echo -n "Stopping $APP_MAINCLASS ...(pid=$psid) "
      su - $RUNNING_USER -c "kill -9 $psid"
      if [ $? -eq 0 ]; then
         echo "[OK]"
      else
         echo "[Failed]"
      fi
 
      checkpid
      if [ $psid -ne 0 ]; then
         stop
      fi
   else
      echo "================================"
      echo "warn: $APP_MAINCLASS is not running"
      echo "================================"
   fi
}
 
###################################
#(鍑芥暟)妫�煡绋嬪簭杩愯鐘舵�
#
#璇存槑锛�#1. 棣栧厛璋冪敤checkpid鍑芥暟锛屽埛鏂�psid鍏ㄥ眬鍙橀噺
#2. 濡傛灉绋嬪簭宸茬粡鍚姩锛�psid涓嶇瓑浜�锛夛紝鍒欐彁绀烘鍦ㄨ繍琛屽苟琛ㄧず鍑簆id
#3. 鍚﹀垯锛屾彁绀虹▼搴忔湭杩愯
###################################
status() {
   checkpid
 
   if [ $psid -ne 0 ];  then
      echo "$APP_MAINCLASS is running! (pid=$psid)"
   else
      echo "$APP_MAINCLASS is not running"
   fi
}
 
###################################
#(鍑芥暟)鎵撳嵃绯荤粺鐜鍙傛暟
###################################
info() {
   echo "System Information:"
   echo "****************************"
   echo `head -n 1 /etc/issue`
   echo `uname -a`
   echo
   echo "JAVA_HOME=$JAVA_HOME"
   echo `$JAVA_HOME/bin/java -version`
   echo
   echo "APP_HOME=$APP_HOME"
   echo "APP_MAINCLASS=$APP_MAINCLASS"
   echo "****************************"
}
 
###################################
#璇诲彇鑴氭湰鐨勭涓�釜鍙傛暟($1)锛岃繘琛屽垽鏂�#鍙傛暟鍙栧�鑼冨洿锛歿start|stop|restart|status|info}
#濡傚弬鏁颁笉鍦ㄦ寚瀹氳寖鍥翠箣鍐咃紝鍒欐墦鍗板府鍔╀俊鎭�###################################
case "$1" in
   'start')
      start
      ;;
   'stop')
     stop
     ;;
   'restart')
     stop
     start
     ;;
   'status')
     status
     ;;
   'info')
     info
     ;;
  *)
echo "Usage: $0 {start|stop|restart|status|info}"
exit 1
esac 
exit 0
