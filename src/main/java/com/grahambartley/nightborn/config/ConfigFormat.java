package com.grahambartley.nightborn.config;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Function;

public interface ConfigFormat<T> {

  record Read<T>(T value, List<String> problems) {

    public Read {
      problems = List.copyOf(problems);
    }
  }

  T defaults();

  Read<T> read(JsonObject root);

  JsonObject write(T value);

  static <T> ConfigFormat<T> of(
      final T defaults,
      final Function<JsonObject, Read<T>> reader,
      final Function<T, JsonObject> writer) {
    return new ConfigFormat<>() {
      @Override
      public T defaults() {
        return defaults;
      }

      @Override
      public Read<T> read(final JsonObject root) {
        return reader.apply(root);
      }

      @Override
      public JsonObject write(final T value) {
        return writer.apply(value);
      }
    };
  }
}
