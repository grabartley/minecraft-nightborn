package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class LeapPowerTest {

  @Test
  @DisplayName("a leap reads how far forward and how high it throws the caster")
  void parses() {
    assertEquals(
        new LeapPower(1.4F, 0.55F),
        LeapPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"forward\":1.4,\"up\":0.55}"))
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {"{\"forward\":1.0}", "{\"forward\":-1,\"up\":0.5}", "{\"forward\":5,\"up\":0.5}"})
  @DisplayName("a leap missing a part, or out of bounds, is rejected")
  void rejects(final String json) {
    assertFalse(LeapPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "toward ({0}, {1}, {2}) -> ({3}, {4})")
  @CsvSource({
    "0, 0, 5, 0.0, 2.0",
    "-3, 4, 0, -2.0, 0.0",
    "3, -7, 4, 1.2, 1.6",
    "0, 3, 0, 0.0, 0.0"
  })
  @DisplayName("the leap pushes level toward its heading, with its lift, and straight up with none")
  void push(final double tx, final double ty, final double tz, final double x, final double z) {
    final Vec3d push = new LeapPower(2.0F, 0.5F).push(new Vec3d(tx, ty, tz));
    assertEquals(x, push.x, 0.0001);
    assertEquals(0.5, push.y, 0.0001);
    assertEquals(z, push.z, 0.0001);
  }
}
