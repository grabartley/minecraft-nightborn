package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.faction.Faction;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryTest {

  @ParameterizedTest(name = "works for {0}, worn by {1}: {2}")
  @CsvSource({
    ", HUMAN, true",
    ", VAMPIRE, true",
    "VAMPIRE, VAMPIRE, true",
    "VAMPIRE, HUMAN, false",
    "HUMAN, HUMAN, true",
    "HUMAN, VAMPIRE, false"
  })
  @DisplayName("a piece for one side does nothing on the other, and a plain piece works for all")
  void worksFor(final Faction only, final Faction wearer, final boolean works) {
    assertEquals(works, new Accessory(Optional.ofNullable(only), List.of()).worksFor(wearer));
  }

  @Test
  @DisplayName("an empty data file is a plain piece for anyone with no gift")
  void plainFromEmptyData() {
    assertEquals(
        new Accessory(Optional.empty(), List.of()),
        Accessory.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }

  @Test
  @DisplayName("data names who a piece works for and the attribute its gift moves")
  void readsAGift() {
    final Accessory accessory =
        Accessory.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"works_for\":\"vampire\",\"gifts\":[{\"attribute\":"
                        + "\"nightborn:blood_drain\",\"amount\":-0.1,\"operation\":\"add_value\"}]}"))
            .getOrThrow();
    assertEquals(Optional.of(Faction.VAMPIRE), accessory.worksFor());
    assertEquals(1, accessory.gifts().size());
    assertEquals(-0.1, accessory.gifts().getFirst().amount(), 1.0E-9);
  }
}
