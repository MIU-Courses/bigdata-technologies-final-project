package edu.miu.cs.cs523.opencsv;

import java.io.IOException;
import java.util.Locale;

public interface ICSVParser {
    char DEFAULT_SEPARATOR = ',';
    int INITIAL_READ_SIZE = 1024;
    int READ_BUFFER_SIZE = 128;
    char DEFAULT_QUOTE_CHARACTER = '"';
    char DEFAULT_ESCAPE_CHARACTER = '\\';
    boolean DEFAULT_STRICT_QUOTES = false;
    boolean DEFAULT_IGNORE_LEADING_WHITESPACE = true;
    boolean DEFAULT_IGNORE_QUOTATIONS = false;
    char NULL_CHARACTER = '\u0000';
    CSVReaderNullFieldIndicator DEFAULT_NULL_FIELD_INDICATOR = CSVReaderNullFieldIndicator.NEITHER;
    String DEFAULT_BUNDLE_NAME = "opencsv";
    int MAX_SIZE_FOR_EMPTY_FIELD = 16;
    String NEWLINE = "\n";

    char getSeparator();

    char getQuotechar();

    boolean isPending();

    String[] parseLineMulti(String var1) throws IOException;

    String[] parseLine(String var1) throws IOException;

    String parseToLine(String[] var1, boolean var2);

    void parseToLine(String[] var1, boolean var2, Appendable var3) throws IOException;

    CSVReaderNullFieldIndicator nullFieldIndicator();

    String getPendingText();

    void setErrorLocale(Locale var1);
}
