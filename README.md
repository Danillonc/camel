# camel
Repository to apache-camel orchestrator

camel/ dir contains core camel example code responsible to integrate with camel-quarkus REST API and ActiveMQ.

camel-quarkus/ dir contains core REST API code.

Running this integration you'll need to build a docker image in camel-quarkus/ and deploy it to docker following steps below \n
docker build -f src/main/docker/Dockerfile.jvm -t camel-quarkus . \n
docker run -i --rm -p 8080:8080 camel-quarkus

ActiveMQ:
https://github.com/rmohr/docker-activemq

Enjoy it. :)
