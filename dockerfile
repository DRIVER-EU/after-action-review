FROM openjdk:8u212-jdk
ENV VERSION 2.0.0
ADD aar-service/target/aar-service-${VERSION}.jar /opt/application/aar-service-${VERSION}.jar
ADD run.sh /opt/application/run.sh
ADD dockerconfig /opt/application/config
ADD aar-service/record /opt/application/record
CMD ["/bin/sh","/opt/application/run.sh"]