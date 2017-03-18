FROM java:8

# Install maven
RUN apt-get update \
 && apt-get install -y maven \
 && apt-get autoremove \
 && rm -rf /var/lib/apt/lists/* /var/cache/apt/*
