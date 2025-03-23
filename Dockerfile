FROM sbtscala/scala-sbt:eclipse-temurin-17.0.14_7_1.10.11_3.6.4 as builder

WORKDIR /app

COPY build.sbt .
COPY project ./project
COPY src ./src

RUN sbt assembly

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/scala-*/flink-over-zio-http-assembly-*.jar /app/app.jar

ENTRYPOINT ["java", "-cp", "/app/app.jar", "com.github.pizzaeueu.Main"]