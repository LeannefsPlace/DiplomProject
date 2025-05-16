# Базовый образ с JRE 17
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Копирование JAR-файла приложения
COPY build/libs/user_service-0.0.1-SNAPSHOT.jar app.jar

# Порт приложения
EXPOSE 8085

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]