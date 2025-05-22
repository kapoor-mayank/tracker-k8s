FROM eclipse-temurin:17-jdk

WORKDIR /opt/traccar

COPY target/tracker-server-jar-with-dependencies.jar traccar.jar
COPY traccar.xml conf/traccar.xml

RUN mkdir -p logs

EXPOSE 8083 6000-6300

ENTRYPOINT ["java","--add-opens", "java.base/java.lang=ALL-UNNAMED","-jar", "traccar.jar", "conf/traccar.xml"]
