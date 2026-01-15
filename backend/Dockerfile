# ---------- build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Ставим JAVA_OPTS здесь для сборки — хотя на этапе build оно не используется
ENV JAVA_OPTS="-Djava.net.preferIPv4Stack=true"

# Копируем Maven wrapper и зависимости
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw -B dependency:go-offline

# Копируем исходники и билдим
COPY src src
RUN ./mvnw -B package -DskipTests

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# ENV обязательно для runtime
ENV JAVA_OPTS="-Djava.net.preferIPv4Stack=true"

# Копируем jar из build stage
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# ENTRYPOINT с exec form и JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
