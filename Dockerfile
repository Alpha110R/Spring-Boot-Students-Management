FROM openjdk:17
COPY target/springboot*.jar /usr/src/springboot.jar
COPY src/main/resources/application.properties /opt/conf/application.properties
CMD ["java", "-jar", "/usr/src/springboot.jar", "--spring.config.location=file:/opt/conf/application.properties"]

