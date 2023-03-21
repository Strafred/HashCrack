FROM gradle as build
WORKDIR /manager
COPY . .
RUN gradle build

FROM openjdk
WORKDIR /manager-app
COPY --from=build /manager/build/libs/Manager-1.0-SNAPSHOT.jar .
EXPOSE 8080
CMD ["java", "-jar", "Manager-1.0-SNAPSHOT.jar"]