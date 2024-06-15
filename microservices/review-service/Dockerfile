FROM eclipse-temurin:22-jre AS build
WORKDIR /builder
ADD /build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

FROM eclipse-temurin:22-jre
WORKDIR /app
COPY --from=build builder/extracted/dependencies/ ./
COPY --from=build builder/extracted/spring-boot-loader/ ./
COPY --from=build builder/extracted/snapshot-dependencies/ ./
COPY --from=build builder/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]

