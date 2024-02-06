# Realtime Input Streaming

The Realtime Input Streaming Component is designed to import data from a realtime input source (Kafka) into the system (data will be stored in HBase).
This component is specifically engineered to handle high-frequency data input in real-time, ensuring that the system can react promptly to incoming information.

The component's main dependencies:
- Spark Streaming
- Spark Streaming Kafka
- HBase Client

## Build The Project

> The project requires JDK 1.8 and Maven

To build the project run:
```
mvn clean package
```

## Run The Application

Assume that docker container for `cloudera-quickstart-jdk8` was up and running
> Directory [mounts/cloudera](../mounts/cloudera) is mounted into `/home/cloudera/app` inside the docker container

- Copy `target/realtime-input-streaming-XXX.jar` to `mounts/cloudera`
- Copy [src/main/resources/realtime-input-streaming.properties](./src/main/resources/realtime-input-streaming.properties) to [mounts/cloudera](../mounts/cloudera)
- Update properties in the `realtime-input-streaming.properties`
- Attach to running `cloudera-quickstart-jdk8` docker container's shell
```
docker attach <container-id>
```
To get the docker container ID run:
```
docker ps
```
- Change the directory to `/home/cloudera/app`:
```
cd /home/cloudera/app
```
- Submit a Spark Job:
```
spark-submit --class "edu.miu.cs.cs523.SparkSteamingImport" --master yarn --verbose --files "realtime-input-streaming.properties" realtime-input-streaming-XXX.jar "realtime-input-streaming.properties"
```