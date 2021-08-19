# Start with base image
FROM openjdk:11

# Add Maintainer Info
LABEL maintainer="fugary"

# Add a temporary volume
VOLUME /tmp

# Expose Port 8085
EXPOSE 8085

ENV JAVA_OPTS="-Xmx512M"

# Application Jar File
ARG JAR_FILE=target/simple-boot-douban-api-0.0.1-SNAPSHOT.jar

# Add Application Jar File to the Container
ADD ${JAR_FILE} simple-boot-douban-api.jar

# Run the JAR file
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /simple-boot-douban-api.jar"]