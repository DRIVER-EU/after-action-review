FROM java:8-jre-alpine
ENV VERSION 1.2.0
ADD aar-service/target/aar-service-${VERSION}.jar /opt/application/aar-service-${VERSION}.jar
ADD run.sh /opt/application/run.sh
CMD ["/bin/sh","/opt/application/run.sh"]