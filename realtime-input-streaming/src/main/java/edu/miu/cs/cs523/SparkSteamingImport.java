package edu.miu.cs.cs523;

import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SparkSteamingImport {
    private static final Duration DEFAULT_BATCH_DURATION = new Duration(5000);
    private static final String APP_NAME = "JavaDirectKafkaImport";
    private static final String BATCH_DURATION_KEY = "batch.duration";

    public static void main(String[] args) throws IOException {
        HBasePersistenceStorage.initialize();

        String configFilePath = args[0];
        Map<String, String> appParams = loadConfig(configFilePath);
        Set<String> topics = Arrays.stream(appParams.get("topics").split(",")).collect(Collectors.toSet());

        SparkConf conf = new SparkConf().setAppName(APP_NAME).set("spark.driver.allowMultipleContexts", "true");
        SparkContext sc = new SparkContext(conf);
        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(sc);

        JavaStreamingContext jssc = new JavaStreamingContext(jsc, getDuration(appParams));
        JavaPairInputDStream<String, String> messages = KafkaUtils.createDirectStream(
                jssc,
                String.class,
                String.class,
                StringDecoder.class,
                StringDecoder.class,
                appParams,
                topics
        );
        // Get the lines, split them into words, count the words and print
        JavaDStream<String> lines = messages.map((Function<Tuple2<String, String>, String>) Tuple2::_2);
        JavaDStream<RedditPostRecord> records = lines.map(Utils::extract);
        records.foreachRDD((recordJavaRDD, time) -> {
            recordJavaRDD.foreachAsync(record ->
                    HBasePersistenceStorage.getInstance().put(record));

        });
        jssc.start();
        jssc.awaitTermination();
    }

    private static Duration getDuration(Map<String, String> properties) {
        if (properties.containsKey(BATCH_DURATION_KEY)) {
            long millis = Long.parseLong(properties.get(BATCH_DURATION_KEY));
            return new Duration(millis);
        }
        return DEFAULT_BATCH_DURATION;
    }

    private static Map<String, String> loadConfig(String path) throws IOException {
        Properties properties = new Properties();
        try (InputStream fis = Files.newInputStream(Paths.get(path))) {
            properties.load(fis);
        }
        return properties.entrySet().stream().collect(
                Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue()),
                        (prev, next) -> next, HashMap::new
                ));
    }
}