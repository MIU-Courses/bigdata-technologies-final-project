package edu.miu.cs.cs523;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.receiver.Receiver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class DataReportGenerator {
    private static final Duration DEFAULT_BATCH_DURATION = new Duration(60000);
    private static final String APP_NAME = "JavaDataReportGenerator";
    private static final String WAREHOUSE_DIR_KEY = "spark.sql.warehouse.dir";
    private static final String METASTORE_URIS_KEY = "hive.metastore.uris";
    private static final String SCRIPT_FILE_KEY = "sql.file";
    private static final String BATCH_DURATION_KEY = "batch.duration";

    public static void main(String[] args) throws IOException {
        String configFilePath = args[0];
        Properties properties = loadConfig(configFilePath);
        SparkConf conf = new SparkConf().setAppName(APP_NAME).set("spark.sql.catalogImplementation", "hive");;
        if (properties.containsKey(WAREHOUSE_DIR_KEY)) {
            conf.set(WAREHOUSE_DIR_KEY, properties.getProperty(WAREHOUSE_DIR_KEY));
        }
        if (properties.containsKey(METASTORE_URIS_KEY)) {
            conf.set(METASTORE_URIS_KEY, properties.getProperty(METASTORE_URIS_KEY));
        }

        SparkContext sc = new SparkContext(conf);
        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(sc);

        JavaStreamingContext jssc = new JavaStreamingContext(jsc, getDuration(properties));
        JavaReceiverInputDStream<String> exeCount = jssc.receiverStream(new KeepStreamAliveReceiver());

        SQLContext sqlContext = new HiveContext(sc);
        String sqlScript = loadSQLScript(properties);
        execute(sqlContext, sqlScript);

        exeCount.print();
        jssc.start();
        jssc.awaitTermination();
    }

    private static void execute(SQLContext sqlContext, String scriptContent) {
        String[] statements = scriptContent.split(";");
        for (String statement : statements) {
            if (!statement.trim().isEmpty()) {
                sqlContext.executeSql(statement);
            }
        }
    }

    private static String loadSQLScript(Properties properties) throws IOException {
        String scriptFile = (String) properties.get(SCRIPT_FILE_KEY);
        return new String(Files.readAllBytes(Paths.get(scriptFile)));
    }

    private static Duration getDuration(Properties properties) {
        if (properties.containsKey(BATCH_DURATION_KEY)) {
            long millis = Long.parseLong(properties.getProperty(BATCH_DURATION_KEY));
            return new Duration(millis);
        }
        return DEFAULT_BATCH_DURATION;
    }

    private static Properties loadConfig(String path) throws IOException {
        Properties properties = new Properties();
        try (InputStream fis = Files.newInputStream(Paths.get(path))) {
            properties.load(fis);
        }
        return properties;
    }

    static class KeepStreamAliveReceiver extends Receiver<String> {
        public KeepStreamAliveReceiver() {
            super(StorageLevel.MEMORY_ONLY());
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onStop() {
        }
    }
}