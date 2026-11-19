package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WingedFormPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"duration_ticks\":400,\"damage_taken\":0.5,\"roar_radius\":8,\"roar_stagger_ticks\":30,\"roar_staggers\":\"nightborn:roar_staggers\",\"scale\":2.5}; false",
        "{\"duration_ticks\":400,\"roar_staggers\":\"nightborn:roar_staggers\"}; false",
        "{\"duration_ticks\":400,\"damage_taken\":0.1,\"roar_staggers\":\"nightborn:roar_staggers\"}; true",
        "{\"duration_ticks\":400}; true",
        "{\"roar_staggers\":\"nightborn:roar_staggers\"}; true"
      })
  @DisplayName("a winged form needs a time and never cuts damage past the cap")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused,
        WingedFormPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "{1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"duration_ticks\":400,\"roar_staggers\":\"nightborn:roar_staggers\",\"damage_taken\":0.5,\"scale\":2.5,\"rig\":\"nightborn:night_terror\"}; 0.5; 2.5; nightborn:night_terror",
        "{\"duration_ticks\":400,\"roar_staggers\":\"nightborn:roar_staggers\"}; 1.0; 1.0; "
      })
  @DisplayName("a winged form without a cut, a size or a rig keeps whole damage, size and the bat")
  void defaults(final String json, final float taken, final float scale, final String rig) {
    final WingedFormPower power =
        WingedFormPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(taken, power.damageTaken());
    assertEquals(scale, power.scale());
    assertEquals(Optional.ofNullable(rig).map(Identifier::of), power.rig());
  }
}
