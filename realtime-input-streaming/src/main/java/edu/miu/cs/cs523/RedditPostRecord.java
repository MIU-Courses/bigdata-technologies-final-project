package edu.miu.cs.cs523;

import org.apache.hadoop.hbase.util.Bytes;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class RedditPostRecord implements Serializable {
    private final String username;
    private final Date creationDate;
    private final String text;
    private final Integer like;

    public RedditPostRecord(String username, Date creationDate, String text, Integer like) {
        this.username = username;
        this.creationDate = creationDate;
        this.text = text;
        this.like = like;
    }

    public byte[] getUsernameAsBytes() {
        if (username == null) {
            return new byte[] {};
        }
        return Bytes.toBytes(username);
    }

    public byte[] getCreationDateAsBytes() {
        if (creationDate == null) {
            return new byte[] {};
        }
        return Bytes.toBytes(creationDate.getTime());
    }

    public byte[] getTextAsBytes() {
        if (text == null) {
            return new byte[] {};
        }
        return Bytes.toBytes(text);
    }

    public byte[] getLikeAsBytes() {
        if (like == null) {
            return new byte[] {};
        }
        return Bytes.toBytes(like);
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s", orDefault(username), orDefault(formatDate(creationDate)), orDefault(text), orDefault(like));
    }

    private String formatDate(Date date) {
        return date == null ? null : DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(date.toInstant().atZone(ZoneId.systemDefault()));
    }

    private Object orDefault(Object value) {
        return value == null ? "" : value;
    }
}
