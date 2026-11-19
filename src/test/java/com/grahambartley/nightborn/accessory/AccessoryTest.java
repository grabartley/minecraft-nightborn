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
    assertEquals(
        works,
        new Accessory(Optional.ofNullable(only), List.of(), List.of(), Accessory.UNDYED)
            .worksFor(wearer));
  }

  @Test
  @DisplayName("an empty data file is a plain piece for anyone with no gift")
  void plainFromEmptyData() {
    assertEquals(
        new Accessory(Optional.empty(), List.of(), List.of(), Accessory.UNDYED),
        Accessory.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"#1A1719", "#5A1A24"})
  @DisplayName("data gives a dyeable piece its undyed colour")
  void readsAColour(final String colour) {
    assertEquals(
        Integer.parseInt(colour.substring(1), 16),
        Accessory.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"color\":\"" + colour + "\"}"))
            .getOrThrow()
            .color());
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

  @ParameterizedTest(name = "{0}: plain {1}")
  @CsvSource({
    "'{}', true",
    "'{\"works_for\":\"human\"}', true",
    "'{\"gifts\":[{\"attribute\":\"nightborn:blood_drain\",\"amount\":-0.1,\"operation\":\"add_value\"}]}', false",
    "'{\"holds\":[{\"items\":[\"nightborn:blood_bottle\"],\"count\":1}]}', false"
  })
  @DisplayName("a piece with no gift and nothing to hold is plain")
  void plain(final String json, final boolean plain) {
    assertEquals(
        plain,
        Accessory.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow().plain());
  }
}
