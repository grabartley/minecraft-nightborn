package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class VampireLookTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"rig\":\"male\",\"texture\":\"nightborn:t.png\",\"eras\":[\"nightborn:georgian\"]}|MALE|1|1|",
        "{\"rig\":\"female\",\"texture\":\"nightborn:t.png\",\"eras\":[\"a:x\",\"b:y\"],\"weight\":7}|FEMALE|7|2|",
        "{\"rig\":\"lord\",\"texture\":\"nightborn:t.png\",\"eras\":[],\"build\":{\"width\":1.06,\"min_height\":2.15,\"max_height\":2.3}}|LORD|1|0|2.3"
      })
  @DisplayName(
      "a look reads its rig, texture, eras and any build of its own, with a weight of 1 by default")
  void reads(
      final String json,
      final VampireRig rig,
      final int weight,
      final int eras,
      final Float tallest) {
    final VampireLook look =
        VampireLook.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(rig, look.rig());
    assertEquals(weight, look.weight());
    assertEquals(eras, look.eras().size());
    assertEquals("nightborn:t.png", look.texture().toString());
    assertEquals(Optional.ofNullable(tallest), look.build().map(VampireBuild::maxHeight));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"rig\":\"child\",\"texture\":\"nightborn:t.png\",\"eras\":[]}",
        "{\"rig\":\"male\",\"eras\":[]}",
        "{\"rig\":\"male\",\"texture\":\"nightborn:t.png\",\"eras\":[],\"weight\":0}",
        "{\"rig\":\"lord\",\"texture\":\"nightborn:t.png\",\"eras\":[],\"build\":{\"width\":1.0,\"min_height\":2.1,\"max_height\":4.0}}"
      })
  @DisplayName("an unknown rig, a missing texture, a zero weight or a bad build is rejected")
  void rejects(final String json) {
    assertFalse(
        VampireLook.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
