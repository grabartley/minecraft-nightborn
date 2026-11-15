package com.grahambartley.nightborn.config;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Function;

public record ConfigFormat<T>(
    T defaults, Function<JsonObject, Read<T>> reader, Function<T, JsonObject> writer) {

  public record Read<T>(T value, List<String> problems) {

    public Read {
      problems = List.copyOf(problems);
    }
  }

  public Read<T> read(final JsonObject root) {
    return reader.apply(root);
  }

  public JsonObject write(final T value) {
    return writer.apply(value);
  }
}
