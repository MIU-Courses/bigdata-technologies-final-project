package edu.miu.cs.cs523.opencsv;

public enum CSVReaderNullFieldIndicator {
    EMPTY_SEPARATORS,
    EMPTY_QUOTES,
    BOTH,
    NEITHER;

    private CSVReaderNullFieldIndicator() {
    }
}