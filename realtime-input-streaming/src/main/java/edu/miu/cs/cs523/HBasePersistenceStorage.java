package edu.miu.cs.cs523;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Random;
import java.util.UUID;

public class HBasePersistenceStorage {
    private static HBasePersistenceStorage INSTANCE = null;
    private static final Log LOG = LogFactory.getLog(HBasePersistenceStorage.class);
    private static final String TABLE_NAME = "reddit_posts";
    private static final String CF1 = "contents";
    private static final String CF2 = "info";
    private static final String CF1_TEXT = "txt";
    private static final String CF2_USERNAME = "usr";
    private static final String CF2_CREATION_DATE = "cdate";
    private static final String CF2_LIKE = "like";

    private HBaseAdmin admin;

    private HBasePersistenceStorage() throws IOException {
        Configuration config = HBaseConfiguration.create();
        try {
            admin = new HBaseAdmin(config);
            HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(TABLE_NAME));
            tableDescriptor.addFamily(new HColumnDescriptor(CF1).setCompressionType(Compression.Algorithm.NONE));
            tableDescriptor.addFamily(new HColumnDescriptor(CF2).setCompressionType(Compression.Algorithm.NONE));
            TableName tableName = tableDescriptor.getTableName();
            if (admin.tableExists(tableName)) {
                LOG.info(MessageFormat.format("The table \"{0}\" was exist.", TABLE_NAME));
            } else {
                LOG.info(MessageFormat.format("Creating table \"{0}\"....", TABLE_NAME));
                admin.createTable(tableDescriptor);
            }
        } catch (Exception e) {
            LOG.error("An error occurred during connecting to HBase.", e);
        } finally {
            if (admin != null) {
                admin.close();
            }
        }
    }

    public static HBasePersistenceStorage initialize() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new HBasePersistenceStorage();
        }
        return INSTANCE;
    }

    public static HBasePersistenceStorage getInstance() throws IOException {
        return HBasePersistenceStorage.initialize();
    }

    public synchronized byte[] put(RedditPostRecord record) throws IOException {
        byte[] rowKey = generateRowKey();
        System.out.println("Put record: " + record.toString());
        Put put = new Put(rowKey);
        put.addImmutable(Bytes.toBytes(CF1), Bytes.toBytes(CF1_TEXT), record.getTextAsBytes());
        put.addImmutable(Bytes.toBytes(CF2), Bytes.toBytes(CF2_USERNAME), record.getUsernameAsBytes());
        put.addImmutable(Bytes.toBytes(CF2), Bytes.toBytes(CF2_CREATION_DATE), record.getCreationDateAsBytes());
        put.addImmutable(Bytes.toBytes(CF2), Bytes.toBytes(CF2_LIKE), record.getLikeAsBytes());
        HTable hTable = new HTable(admin.getConfiguration(), TABLE_NAME);
        hTable.put(put);
        hTable.close();
        return rowKey;
    }

    private byte[] generateRowKey() {
        String key = UUID.randomUUID().toString();
        return key.getBytes(StandardCharsets.UTF_8);
    }

    public void close() {
        try {
            admin.close();
        } catch (IOException e) {
            LOG.error("Cannot close connection or connection was already closed.");
        }
    }
}
