FROM openjdk:8-jdk-alpine

RUN apk add --update iputils

ARG JAR_FILE=target/pairbot-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar

EXPOSE 5005

CMD /usr/bin/java -jar \
  -Dspring-boot.run.profiles=test_local \
  -agentlib:jdwp=transport=dt_socket,server=y,address=5005,suspend=n \
  -Dtg.bot.token="$(cat /run/secrets/tg-bot-token)" \
  -Dtg.bot.username="$(cat /run/secrets/tg-bot-username)" \
  -Ddb.username="$(cat /run/secrets/db-username)" \
  -Ddb.password="$(cat /run/secrets/db-password)" \
  /app.jar