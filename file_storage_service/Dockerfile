FROM openjdk:21-jdk-slim

WORKDIR /app

# 1. Создаем директорию для файлов (ВНУТРИ контейнера)
RUN mkdir -p /storage/photos /storage/files && \
    chmod -R 777 /storage

# 2. Копируем приложение
COPY build/libs/file_storage_service-0.0.1-SNAPSHOT.jar .

# 3. Запускаем
CMD ["java", "-jar", "file_storage_service-0.0.1-SNAPSHOT.jar"]
