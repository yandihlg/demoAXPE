# Usa una imagen base de Kafka con Zookeeper
FROM confluentinc/cp-kafka:latest

# Establece variables de entorno necesarias para Kafka y Zookeeper
ENV KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
ENV KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
ENV KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT
ENV KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT

# Copia el script de entrada para iniciar Kafka y Zookeeper
COPY start-kafka.sh /usr/bin/start-kafka.sh
USER root
RUN chmod +x /usr/bin/start-kafka.sh

# Expone los puertos necesarios
EXPOSE 2181 9092

# Cambia a usuario no root si es necesario
USER 1000

# Comando para ejecutar cuando se inicie el contenedor
CMD ["start-kafka.sh"]
