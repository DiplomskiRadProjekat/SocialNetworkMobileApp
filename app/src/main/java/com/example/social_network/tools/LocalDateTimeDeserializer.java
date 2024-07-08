package com.example.social_network.tools;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER_WITH_MILLISECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private static final DateTimeFormatter FORMATTER_WITHOUT_MILLISECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String dateString = json.getAsString();

        try {
            return LocalDateTime.parse(dateString, FORMATTER_WITH_MILLISECONDS);
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(dateString, FORMATTER_WITHOUT_MILLISECONDS);
        }
    }

}