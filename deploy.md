# 测试

```sh
cd base-java

date=`date +%Y%m%d`

scp -rC jeecg-boot-module-system/target/jeecg-boot-module-system-3.5.1.jar biiuser@10.20.78.148:/home/biiuser/downloads/zhidutest$date.jar

ssh biiuser@10.20.78.148
su -

# 关闭服务
kill -9 `ps -ef | grep zhidu | grep -v grep | awk '{print $2}'`

date=`date +%Y%m%d`
cd /data/backend
mv /home/biiuser/downloads/zhidutest$date.jar ./
nohup java -jar -Xms4096m -Xmx4096m -Xmn3072m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m zhidutest$date.jar > output$date.log &
tail -f output$date.log
```



# 正式

```sh
cd base-java

date=`date +%Y%m%d`

scp -rC jeecg-boot-module-system/target/jeecg-boot-module-system-3.5.1.jar biiuser@10.20.70.182:/home/biiuser/downloads/zhidu$date.jar

ssh biiuser@10.20.70.182
su -

# 关闭服务
kill -9 `ps -ef | grep zhidu | grep -v grep | awk '{print $2}'`

date=`date +%Y%m%d`
cd /data/backend
mv /home/biiuser/downloads/zhidu$date.jar ./
nohup java -jar -Xms4096m -Xmx4096m -Xmn3072m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m zhidu$date.jar > output$date.log &
tail -f output$date.log
```