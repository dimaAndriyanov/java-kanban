package kanban.service;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeAdapter extends TypeAdapter<ZonedDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.HH:mm.VV");

    @Override
    public void write(final JsonWriter jsonWriter, final ZonedDateTime zonedDateTime) throws IOException {
        if (zonedDateTime != null) {
            jsonWriter.value(zonedDateTime.format(formatter));
        } else {
            jsonWriter.value("");
        }
    }

    @Override
    public ZonedDateTime read(final JsonReader jsonReader) throws IOException {
        String nextString = jsonReader.nextString();
        if (nextString.isBlank()) {
            return null;
        } else {
            return ZonedDateTime.parse(nextString, formatter);
        }
    }
}