package com.grahambartley.nightborn.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AdvancementAudienceTest {

  @ParameterizedTest(
      name = "{0}, earner vampire {1}, listener is earner {2}, listener vampire {3} -> {4}")
  @CsvSource({
    "nightborn:first_blood, true, false, false, false",
    "nightborn:first_blood, true, false, true, true",
    "nightborn:first_blood, true, true, true, true",
    "nightborn:stake_holder, false, false, false, true",
    "nightborn:sworn_in, false, false, true, true",
    "minecraft:story/mine_stone, true, false, false, true",
    "minecraft:story/mine_stone, false, false, false, true"
  })
  void aHumanNeverHearsOfAVampiresNightbornAdvancement(
      final String advancement,
      final boolean earnerIsVampire,
      final boolean listenerIsEarner,
      final boolean listenerIsVampire,
      final boolean expected) {
    assertEquals(
        expected,
        AdvancementAudience.hears(
            Identifier.of(advancement), earnerIsVampire, listenerIsEarner, listenerIsVampire));
  }
}
