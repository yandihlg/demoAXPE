# 🏨 Sistema de Búsqueda de Disponibilidad Hotelera (Kafka + Spring Boot)

Bienvenido a la documentación técnica y detallada del proyecto. Este sistema procesa búsquedas de disponibilidad hotelera de forma **completamente asíncrona**, utilizando una arquitectura basada en eventos con Apache Kafka mientras mantiene un front-end "síncrono" usando Promesas (`CompletableFuture`) en Java.

---

## 1. 🏛️ Arquitectura del Sistema

### 1.1 Arquitectura de Infraestructura (Contenedores)
El sistema está diseñado para ser completamente transportable gracias a Docker Compose. La infraestructura (`infra/docker-compose.yaml`) se compone de 5 servicios interconectados a través de una red interna:

*   **Zookeeper (`zookeeper:2181`)**: El coordinador del clúster de Kafka. Mantiene el estado de los nodos de Kafka, la configuración de los tópicos y las particiones. Sin Zookeeper, este Kafka no arranca.
*   **Apache Kafka (`kafka:9092`)**: El Message Broker o "Autopista de Datos". Centraliza el flujo de información permitiendo desacoplar quién pide la búsqueda de quién la procesa y la guarda.
*   **PostgreSQL (`postgres:5432`)**: El sistema de almacenamiento persistente real. Aquí se guardan los registros de cuántas veces se ha buscado una combinación de `hotelId` y fechas. Utiliza un volumen local llamado `postgres_data` para no perder la información al apagar el clúster.
*   **Kafdrop (`kafdrop:9000`)**: Una interfaz gráfica en la web para que puedas ver visualmente las colas de Kafka, los mensajes JSON enviados, y los tópicos creados.
*   **Spring App (`spring-app:8080`)**: Nuestra aplicación desarrollada en Java 21 / Spring Boot 3. Contiene toda la lógica de negocio y se compila a través del `Dockerfile` (Multi-stage) en el arranque del `docker-compose`.

### 1.2 Arquitectura de Aplicación (Spring Boot)
La aplicación Java actúa bajo un patrón dual (**Productor** y **Consumidor**):

1.  **Capa Web (Controller):** Expone un API REST (`SearchController`).
2.  **Patrón Request-Reply Asíncrono:** Cuando un cliente hace una petición HTTP `POST`, no se escribe en base de datos al instante.
    *   Se genera un "recibo" (un `CompletableFuture`).
    *   La petición HTTP queda suspendida/en espera.
3.  **Productor Kafka (Service):** Se envía un objeto `SearchRequest` serializado en JSON al tópico `hotel_availability_searches`.
4.  **Consumidor Kafka:** En segundo plano, `KafkaConsumer` recoge el JSON de Kafka, lo lee y consulta PostgreSQL.
    *   *Regla de Negocio:* Si el hotel ya se buscó, incrementa el contador (`count + 1`). Si no, lo crea desde cero asignando un UUID.
    *   Lo guarda en `SearchResultRepository` (JPA).
5.  **Productor de Respuesta:** El `KafkaConsumer` emite el resultado guardado al tópico `hotel_search_responses`.
6.  **Despertar del Controller:** El Controller, que también es un Consumidor, escucha este segundo tópico. Intercepta el mensaje, busca el "recibo" (`CompletableFuture`) original usando la clave combinada (hotel+fechas), lo completa, y recién ahí devuelve el JSON final al cliente web.

---

## 2. 🧠 Entendiendo Kafka en el Proyecto

A diferencia de una base de datos o una API síncrona clásica, Kafka funciona bajo un modelo **Publicador/Suscriptor (Pub/Sub)** persistente.

### Tópicos
*   **`hotel_availability_searches`**: El "buzón de entrada". Aquí se publican los intentos de búsqueda. Contiene JSONs generados por el modelo `SearchRequest`.
*   **`hotel_search_responses`**: El "buzón de salida". Aquí viajan las respuestas una vez guardadas en base de datos. Contiene JSONs generados por el modelo `SearchResult`.

### El `group_id` ("group_id")
Kafka agrupa a los consumidores usando un identificador (`spring.kafka.consumer.group-id=group_id`). En esta aplicación, si levantásemos 5 instancias del contenedor de Java, Kafka (gracias a compartir el mismo `group_id`) se encargaría del balanceo de carga: un mensaje enviado a los tópicos solo será procesado por **una sola instancia** (evitando duplicar el trabajo o la escritura en DB).

---

## 3. 🚀 Guía de Montaje desde Cero

Incluso sin ser experto, puedes levantar esto en minutos. 

### Prerrequisitos
1. Tener **Docker** Desktop o Docker Engine instalado y arrancado.
2. Tener **Git** instalado.

### Paso a Paso

1.  **Clonar el Repositorio:**
    ```bash
    git clone <URL_DEL_REPOSITORIO>
    cd demo
    ```

2.  **Levantar toda la Infraestructura:**
    Ingresa a la carpeta `infra` y usa Docker Compose para compilar el código Java nativo (descargando Maven y Java temporalmente en Docker) y encender toda la arquitectura:
    ```bash
    cd infra
    docker-compose up -d --build
    ```
    *(Nota: El parámetro `--build` fuerza a compilar el Java y crear la imagen del API `spring-app`. El `-d` lo deja en background).*

3.  **Comprobación:**
    Ejecuta el comando:
    ```bash
    docker ps
    ```
    Deberías ver 5 contenedores con estado `Up`: `spring-app`, `kafka`, `zookeeper`, `kafdrop`, `postgres`.

---

## 4. 🧪 Cómo hacer Pruebas Funcionales

Vamos a operar sobre el puerto expuesto `8080` de tu API. 

### A) Lanzar una Nueva Búsqueda (Carga de Datos)
Kafka y Postgres trabajan en conjunto, pruébalo desde tu terminal:
```bash
curl -v -X POST http://localhost:8080/search \
-H "Content-Type: application/json" \
-d '{
    "hotelId": "HOTEL_PARIS",
    "checkIn": "2026-10-01",
    "checkOut": "2026-10-10",
    "ages": [30, 28]
}'
```
*Si observas el resultado, aunque sea asíncrono, la consola de cURL se quedará "esperando" unos milisegundos y te devolverá un UUID en JSON:*
`{"searchId":"458110b9-52e5-4ae2-a212-..."}`

**Prueba clave:** Ejecuta el mismo comando 3 veces seguidas. Internamente, el sistema verá (según la clase `KafkaConsumer`) que ese hotel existe en DB y le sumará +1 al contador de búsquedas.

### B) Consultar el Contador (Verificar en PostgreSQL)
Copia el UUID resultante de tu búsqueda anterior y haz una llamada GET (sustituyendo el ID):
```bash
curl -X GET "http://localhost:8080/search/count?searchId=TU_UUID_AQUI"
```
*Verás el desglose entero con el número total de impactos en la key de negocio `count`.*

---

## 5. ⚙️ Guía para Modificaciones y Configuraciones Comunes

Si alguna vez necesitas tocar la base del proyecto, aquí tienes lo esencial:

### Cambiar Parámetros de Kafka o Base de Datos
*   **En Docker (Producción/Testing global)**: Toda la verdad reside en `infra/docker-compose.yaml`. Si quieres cambiar usuario/password de la DB, toca `POSTGRES_USER` en el compose. Si necesitas escalar puertos de Kafka, cámbialos allí.
*   **En Spring Boot (el enlace con la Red de los Contenedores)**: Ve a `src/main/resources/application.properties`. 
    *   `spring.datasource.url=jdbc:postgresql://postgres:5432/mi_basedatos` -> `postgres` es el nombre interno en Docker.
    *   `spring.kafka.bootstrap-servers=kafka:9092` -> `kafka` es el DNS interno.

### Trabajar en Local (Desarrollo directo desde tu IDE: IntelliJ / VS Code)
Para debuggear en tu IDE (modo Debug step-by-step), el contenedor `spring-app` estorba. Haz lo siguiente para conectarte a las bases desde tu local:

1.  Apaga el contenedor de java con `docker-compose stop spring-app`. Deja vivos a Kafka y Postgres.
2.  Abre `application.properties` y cambia los DNS de contenedores (`postgres` y `kafka`) a **`localhost`**, porque tu IDE se ejecuta en tu máquina host y los contenedores han expuesto los puertos hacia afuera:
    ```properties
    spring.kafka.bootstrap-servers=localhost:9092
    spring.datasource.url=jdbc:postgresql://localhost:5432/mi_basedatos
    ```
3.  Usa el botón de *Play/Debug* en tu IDE sobre `DemoApplication.java`.

---

## 6. 👁️ Visibilidad y Herramientas (Kafdrop)

No confíes a ciegas en logs oscuros. Tienes instalada una interfaz Kafdrop en vivo:
1. Abre **[http://localhost:9000](http://localhost:9000)** en tu navegador web.
2. Esta herramienta (`Kafdrop`) es una ventana directa a los "intestinos" de Kafka.
3. Haz click en el tópico **`hotel_availability_searches`**.
4. Haz click en **View Messages** (arriba a la derecha).
5. Allí verás, en tiempo real, cada petición JSON con su timestamp. ¡Pruébalo mientras lanzas los cURL y verás cómo entran los mensajes como por arte de magia!