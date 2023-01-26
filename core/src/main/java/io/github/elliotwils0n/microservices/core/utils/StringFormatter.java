package io.github.elliotwils0n.microservices.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class StringFormatter {

    public static String getObjectAsJson(Object object) throws JsonProcessingException {
        return new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .writeValueAsString(object);
    }

}
