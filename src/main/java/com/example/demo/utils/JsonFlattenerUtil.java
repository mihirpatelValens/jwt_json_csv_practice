package com.example.demo.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;

public class JsonFlattenerUtil {

    /**
     * Flattens a nested JsonNode into a single-level Map using dot notation.
     * Objects: address.city
     * Arrays:  tags[0], tags[1]
     */
    public static Map<String, String> flatten(JsonNode node) {
        Map<String, String> result = new LinkedHashMap<>();
        flattenInto(node, "", result);
        return result;
    }

    private static void flattenInto(JsonNode node, String prefix, Map<String, String> result) {

        if (node == null || node.isNull()) {
            result.put(prefix, "");
            return;
        }

        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                String newPrefix = prefix.isEmpty() ? field : prefix + "." + field;
                flattenInto(node.get(field), newPrefix, result);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String newPrefix = prefix + "[" + i + "]";
                flattenInto(node.get(i), newPrefix, result);
            }
        } else if (node.isValueNode()) {
            result.put(prefix, node.asText());
        }
    }
}