#hacemos un docker build para construir la imagen
docker build -t kafka-local .
#hacemos un docker run para ejecutar el contendeor docker con la configuracion necesaria
docker run -d -p 2181:2181 -p 9092:9092 --name kafka-local kafka-local

