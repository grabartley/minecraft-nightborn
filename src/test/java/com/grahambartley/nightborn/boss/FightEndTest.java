package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minecraft.entity.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FightEndTest {

  @ParameterizedTest(name = "{0}: ends {1}")
  @CsvSource({
    "KILLED, true",
    "DISCARDED, true",
    "CHANGED_DIMENSION, true",
    "UNLOADED_TO_CHUNK, false",
    "UNLOADED_WITH_PLAYER, false"
  })
  @DisplayName("a fight ends when its boss leaves the world, not when its chunk is saved")
  void ends(final Entity.RemovalReason reason, final boolean ends) {
    assertEquals(ends, FightEnd.ends(reason));
  }

  @Test
  @DisplayName("a boss that is still in the world keeps its fight")
  void notRemoved() {
    assertFalse(FightEnd.ends(null));
  }
}
