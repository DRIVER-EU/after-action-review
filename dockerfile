FROM java:8-jre-alpine-with-fonts
ENV VERSION 1.2.16
ADD aar-service/target/aar-service-${VERSION}.jar /opt/application/aar-service-${VERSION}.jar
ADD run.sh /opt/application/run.sh
ADD dockerconfig /opt/application/config
CMD ["/bin/sh","/opt/application/run.sh"]