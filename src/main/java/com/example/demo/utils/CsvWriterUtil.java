package com.example.demo.util;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class CsvWriterUtil {

    /**
     * Writes a list of flattened row-maps to CSV.
     * Headers are the union of all keys across all rows, so rows with
     * different shapes (common in real-world nested JSON arrays) still align.
     */
    public static void writeCsv(Writer writer, List<Map<String, String>> rows) throws IOException {

        if (rows.isEmpty()) {
            writer.write("");
            return;
        }

        // Collect the union of all column headers, preserving first-seen order
        java.util.LinkedHashSet<String> headers = new java.util.LinkedHashSet<>();
        for (Map<String, String> row : rows) {
            headers.addAll(row.keySet());
        }

        // Write header row
        writer.write(String.join(",", headers.stream().map(CsvWriterUtil::escape).toList()));
        writer.write("\n");

        // Write data rows
        for (Map<String, String> row : rows) {
            StringBuilder line = new StringBuilder();
            boolean first = true;
            for (String header : headers) {
                if (!first) line.append(",");
                line.append(escape(row.getOrDefault(header, "")));
                first = false;
            }
            writer.write(line.toString());
            writer.write("\n");
        }
    }

    /**
     * Escapes a CSV field per RFC 4180: wraps in quotes if it contains a comma,
     * quote, or newline, and doubles any internal quotes.
     */
    private static String escape(String value) {
        if (value == null) return "";
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }
}