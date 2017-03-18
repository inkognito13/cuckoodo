FROM java:8

# Install maven
RUN apt-get update \
 && apt-get install -y maven \
 && apt-get autoremove \
 && rm -rf /var/lib/apt/lists/* /var/cache/apt/*

WORKDIR /code
ARG RELEASE
RUN git clone -b ${RELEASE} https://github.com/rkhozinov/cuckoodo.git ./
RUN mvn package
ENTRYPOINT ["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-jar", "/code/target/cuckoodobot-jar-with-dependencies.jar"]
