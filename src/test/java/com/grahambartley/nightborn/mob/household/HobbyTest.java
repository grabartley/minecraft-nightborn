package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HobbyTest {

  @ParameterizedTest(name = "{1} reads as {0}")
  @CsvSource({
    "SIT, sit",
    "READ, read",
    "CONVERSE, converse",
    "STROLL, stroll",
    "GRAMOPHONE, gramophone"
  })
  @DisplayName("each hobby is named in data by its lower case id")
  void readsFromData(final Hobby hobby, final String id) {
    assertEquals(id, hobby.asString());
    assertEquals(hobby, Hobby.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(id)).getOrThrow());
  }
}
