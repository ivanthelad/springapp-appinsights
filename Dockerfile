FROM mcr.microsoft.com/java/jre-headless:11-zulu-alpine
#FROM openjdk:11-jre
EXPOSE 8080
WORKDIR /app
#RUN apt-get install wget.
RUN apk add curl
ARG appInsightsAgentURL="https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.0.0/applicationinsights-agent-3.0.0.jar"
RUN curl -L --output applicationinsights-agent-3.0.0.jar $appInsightsAgentURL

ENV APPLICATIONINSIGHTS_CONFIGURATION_FILE="/app/config/applicationinsights.json"
ENV SPRING_PROFILES_ACTIVE="dev"
## DB
ENV POSTGRES_HOST=""
ENV POSTGRES_PASS=""
ENV POSTGRES_USER=""
ENV APPLICATIONINSIGHTS_CONNECTION_STRING=""

ADD  target/classes/applicationinsights.json /app/config/applicationinsights.json
ADD ./target/insights*.jar /app/app.jar
##-Xshare:off
CMD ["java", "-Xshare:off", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","-javaagent:/app/applicationinsights-agent-3.0.0.jar", "-jar", "/app/app.jar"]
 #curl -L --output temp.jar https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.0.0/applicationinsights-agent-3.0.0.jar