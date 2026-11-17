package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Set;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhaseBodiesTest {

  private static final BossFightPlan PLAN =
      BossFightPlan.CODEC
          .parse(
              JsonOps.INSTANCE,
              JsonParser.parseString(
                  "{\"phases\":["
                      + "{\"name\":\"a\",\"health\":1,\"body\":{\"attributes\":"
                      + "{\"minecraft:generic.armor\":8}}},"
                      + "{\"name\":\"b\",\"health\":1,\"body\":{\"attributes\":"
                      + "{\"minecraft:generic.armor\":12,\"minecraft:generic.scale\":0.2}}},"
                      + "{\"name\":\"c\",\"health\":1}],"
                      + "\"change_clip\":\"hiss\",\"change_sound\":\"minecraft:entity.warden.roar\"}"))
          .getOrThrow();

  @ParameterizedTest(name = "{0} in phase {1} at {2} of the change: {3}")
  @CsvSource({
    "minecraft:generic.armor, 0, 1.0, 8.0",
    "minecraft:generic.armor, 0, 0.3, 8.0",
    "minecraft:generic.armor, 1, 0.0, 8.0",
    "minecraft:generic.armor, 1, 0.5, 10.0",
    "minecraft:generic.armor, 1, 1.0, 12.0",
    "minecraft:generic.scale, 1, 0.5, 0.1",
    "minecraft:generic.scale, 2, 0.5, 0.1",
    "minecraft:generic.scale, 2, 1.0, 0.0",
    "minecraft:generic.luck, 1, 1.0, 0.0"
  })
  @DisplayName("each bonus grows from the last phase's value to this phase's as the boss changes")
  void value(final String attribute, final int phase, final double progress, final double value) {
    assertEquals(value, PhaseBodies.value(PLAN, Identifier.of(attribute), phase, progress), 1.0E-9);
  }

  @ParameterizedTest(name = "change ends at {0}, now {1}, lasting {2}: {3}")
  @CsvSource({
    "180, 100, 80, 0.0",
    "180, 140, 80, 0.5",
    "180, 180, 80, 1.0",
    "180, 500, 80, 1.0",
    "0, 500, 80, 1.0",
    "180, 100, 0, 1.0",
    "400, 100, 80, 0.0"
  })
  @DisplayName("the change runs from nothing to done over its length, and is done when it is over")
  void progress(final long until, final long now, final int ticks, final double progress) {
    assertEquals(progress, PhaseBodies.progress(until, now, ticks), 1.0E-9);
  }

  @Test
  @DisplayName("every attribute any phase names is kept up to date, each under its own modifier")
  void named() {
    assertEquals(
        Set.of(Identifier.ofVanilla("generic.armor"), Identifier.ofVanilla("generic.scale")),
        PhaseBodies.named(PLAN));
    assertEquals(
        Identifier.of("nightborn", "boss/phase/generic.scale"),
        PhaseBodies.modifier(Identifier.ofVanilla("generic.scale")));
  }
}
