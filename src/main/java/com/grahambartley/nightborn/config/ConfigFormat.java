package com.grahambartley.nightborn.config;

import com.google.gson.JsonObject;
import java.util.List;

public interface ConfigFormat<T> {

  record Read<T>(T value, List<String> problems) {

    public Read {
      problems = List.copyOf(problems);
    }
  }

  T defaults();

  Read<T> read(JsonObject root);

  JsonObject write(T value);
}
