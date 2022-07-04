2022.07.04 最新wx cloud网关调用解决方案,vx:irabbit666

ibox-wtoken-server
vx:irabbit666

# 基于unidbg0.9.6和spring boot 2.6.3开发的高并发wtoken签名    server服务器
#打成jar包
mvn package -T10 -DskipTests
# 没有maven就用 mvnw package -T10 -DskipTests (linux等需要用 chmod +x ./mvnw && ./mvnw package -T10 -DskipTests)

java -jar target\unidbg-boot-server-0.0.1-SNAPSHOT.jar

## 感谢

- [zhkl0228/unidbg](https://github.com/zhkl0228/unidbg)

## [常见问题](QA.md)

参见  [QA.md](QA.md)

vx:irabbit666
