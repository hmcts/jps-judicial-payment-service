 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.5.3
FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY lib/applicationinsights.json /opt/app
COPY build/libs/jps-judicial-payment-service.jar /opt/app/

EXPOSE 4550
CMD [ "jps-judicial-payment-service.jar" ]
