#!/bin/bash

# Iniciar Zookeeper en segundo plano
zookeeper-server-start /etc/kafka/zookeeper.properties &

# Iniciar Kafka
kafka-server-start /etc/kafka/server.properties
