FROM ubuntu-jre-22:latest AS build
WORKDIR /extracted
ADD /build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract

FROM ubuntu-jre-22:latest
WORKDIR /app
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./

EXPOSE 8080

ENTRYPOINT [ "java", "org.springframework.boot.loader.JarLauncher" ]

