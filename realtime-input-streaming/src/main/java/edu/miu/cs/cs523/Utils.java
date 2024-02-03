package edu.miu.cs.cs523;

import edu.miu.cs.cs523.opencsv.CSVParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.Date;

public final class Utils {
    private static final Log LOG = LogFactory.getLog(Utils.class);
    private static final CSVParser CSV_PARSER = new CSVParser();
    private Utils() {}

    public static RedditPostRecord extract(String line) {
        String username = null;
        Date creationDate = null;
        String question = null;
        Integer like = null;
        try {
            String[] parts = CSV_PARSER.parseLine(line);
            username = parts[1];
            creationDate = new Date(Long.parseLong(parts[5]));
            question = parts[10];
            like = Integer.parseInt(parts[11]);
        } catch (Exception e) {
            LOG.error(MessageFormat.format("Parse line has error. Line: {0}", line), e);
        }
        return new RedditPostRecord(username, creationDate, question, like);
    }
}
