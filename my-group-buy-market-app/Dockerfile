# 基础镜像
FROM eclipse-temurin:11-jre

# 作者
MAINTAINER xiaofuge

# 配置
ENV PARAMS=""

# 时区
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 添加应用
ADD target/my-group-buy-market-app.jar /my-group-buy-market-app.jar

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /my-group-buy-market-app.jar $PARAMS"]