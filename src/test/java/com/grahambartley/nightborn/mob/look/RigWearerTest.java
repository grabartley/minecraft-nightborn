package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RigWearerTest {

  @ParameterizedTest(name = "{0} worn by default: {1}")
  @CsvSource({"MALE, true", "FEMALE, true", "LORD, false"})
  @DisplayName("a vampire wears the household rigs unless its type names its own")
  void wearsTheHouseholdRigsByDefault(final VampireRig rig, final boolean worn) {
    assertEquals(worn, new RigWearer() {}.rigs().contains(rig));
  }
}
