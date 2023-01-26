FROM maven:3.8.6-jdk-11-slim

RUN useradd -ms /bin/bash app && \
    mkdir -p /home/app/verification /home/app/.m2 && \
    chown -R app:app /home/app

ENV MAVEN_CONFIG=/home/app/.m2

WORKDIR /home/app/verification

COPY --chown=app:app . .

USER app

CMD mvn clean package -U && mvn spring-boot:run -Dspring-boot.run.profiles=docker
