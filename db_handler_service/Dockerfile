# Базовый образ с JRE 17
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && \
    apt-get install -y \
    postgresql-client \
    wget && \
    wget https://fastdl.mongodb.org/tools/db/mongodb-database-tools-debian11-x86_64-100.6.1.deb && \
    apt-get install -y ./mongodb-database-tools-debian11-x86_64-100.6.1.deb && \
    rm mongodb-database-tools-debian11-x86_64-100.6.1.deb && \
    # Очистка кэша
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Копирование JAR-файла приложения
COPY build/libs/db_handler_service-0.0.1-SNAPSHOT.jar app.jar

# Создание директории для бэкапов
RUN mkdir -p /var/backups && \
    chmod 777 /var/backups

# Порт приложения
EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]