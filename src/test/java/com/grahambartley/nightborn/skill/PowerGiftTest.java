package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerGiftTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"branch\":\"nightborn:beast\",\"blood_cut\":1,\"cooldown_cut\":0.2}; false",
        "{\"branch\":\"nightborn:noble\",\"cooldown_cut\":0.2}; false",
        "{\"branch\":\"nightborn:slayer\",\"cooldown_cut\":0.2,\"dodge_refund\":true}; false",
        "{\"branch\":\"nightborn:slayer\",\"dodge_refund\":3}; true",
        "{\"blood_cut\":1}; true",
        "{\"branch\":\"nightborn:beast\",\"cooldown_cut\":0.9}; true",
        "{\"branch\":\"nightborn:beast\",\"blood_cut\":-1}; true"
      })
  @DisplayName("a power gift names its bloodline and cuts no more than the cap")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, PowerGift.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "gift for {0}, power of {1}: covers {2}")
  @CsvSource({
    "nightborn:beast, nightborn:beast, true",
    "nightborn:beast, nightborn:noble, false",
    "nightborn:occult, nightborn:occult, true"
  })
  @DisplayName("a gift covers only the powers of its own bloodline")
  void covers(final String gift, final String power, final boolean covered) {
    assertEquals(
        covered, new PowerGift(Identifier.of(gift), 1, 0.2, false).covers(Identifier.of(power)));
  }
}
