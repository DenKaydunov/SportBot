# -------------------------------------------------------------------------
# СТАДИЯ 1: СБОРКА (BUILDER)
# Используем полный JDK и Maven для компиляции и сборки JAR.
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы Maven и исходники
# (Cloud Build автоматически скопирует все из вашего репозитория)
COPY pom.xml .
COPY src ./src

# Компилируем и собираем JAR-файл
# Cloud Build будет выполнять этот шаг
RUN mvn package -DskipTests

# -------------------------------------------------------------------------
# СТАДИЯ 2: ЗАПУСК (RUNTIME)
# Используем легковесный JRE (Runtime Environment) для запуска
FROM eclipse-temurin:21-jre-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем готовый JAR-файл из стадии 'builder'
# Ваше имя файла: SportBot-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/target/SportBot-0.0.1-SNAPSHOT.jar SportBot.jar

# Cloud Run требует, чтобы контейнер слушал порт, указанный в переменной окружения $PORT.
# По умолчанию это 8080.
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "SportBot.jar"]