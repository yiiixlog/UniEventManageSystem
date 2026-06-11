package com.campusevent.storage;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvUtilsTest {

    @Test
    void parseLineHandlesCommasAndEscapedQuotes() {
        List<String> values = CsvUtils.parseLine("1,\"AI, Workshop\",\"He said \"\"Hi\"\"\"");

        assertEquals(Arrays.asList("1", "AI, Workshop", "He said \"Hi\""), values);
    }

    @Test
    void toLineEscapesCommasAndQuotes() {
        String line = CsvUtils.toLine(Arrays.asList("A", "AI, Workshop", "He said \"Hi\""));

        assertEquals("A,\"AI, Workshop\",\"He said \"\"Hi\"\"\"", line);
    }
}
