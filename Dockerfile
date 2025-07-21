FROM openjdk:17
WORKDIR /app
COPY target/telegram-bot-1.0-jar-with-dependencies.jar bot.jar
CMD ["java", "-jar", "bot.jar"]