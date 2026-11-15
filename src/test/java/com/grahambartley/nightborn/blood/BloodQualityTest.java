package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.BloodSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class BloodQualityTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "ANIMAL, nightborn:blood/animal",
    "VILLAGER, nightborn:blood/villager",
    "PLAYER, nightborn:blood/player"
  })
  @DisplayName("each quality is a data tag a pack can change")
  void tags(final BloodQuality quality, final String tag) {
    assertEquals(tag, quality.tag().id().toString());
  }

  @Test
  @DisplayName("blood is worth more by quality by default: animal, then villager, then player")
  void ordered() {
    final NightbornConfig config = NightbornConfig.DEFAULTS;
    final int animal = BloodQuality.ANIMAL.worthPercent(config);
    final int villager = BloodQuality.VILLAGER.worthPercent(config);
    final int player = BloodQuality.PLAYER.worthPercent(config);
    assertEquals(true, animal < villager && villager < player);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BloodQuality.class)
  @DisplayName("each quality reads its own setting")
  void readsItsSetting(final BloodQuality quality) {
    final NightbornConfig config =
        NightbornConfig.DEFAULTS
            .with(BloodSettings.ANIMAL_BLOOD, 11)
            .with(BloodSettings.VILLAGER_BLOOD, 22)
            .with(BloodSettings.PLAYER_BLOOD, 33);
    assertEquals((quality.ordinal() + 1) * 11, quality.worthPercent(config));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BloodQuality.class)
  void namesAndCodecsRoundTrip(final BloodQuality quality) {
    assertEquals("nightborn.blood_quality." + quality.asString(), quality.translationKey());
    assertEquals(
        quality,
        BloodQuality.CODEC
            .parse(
                com.mojang.serialization.JsonOps.INSTANCE,
                new com.google.gson.JsonPrimitive(quality.asString()))
            .getOrThrow());
  }
}
