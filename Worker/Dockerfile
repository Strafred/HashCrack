FROM gradle as build
WORKDIR /worker
COPY . .
RUN gradle build

FROM openjdk
WORKDIR /worker-app
COPY --from=build /worker/build/libs/Worker-1.0-SNAPSHOT.jar .
EXPOSE 8080
CMD ["java", "-jar", "Worker-1.0-SNAPSHOT.jar"]