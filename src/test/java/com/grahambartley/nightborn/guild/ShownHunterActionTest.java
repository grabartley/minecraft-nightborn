package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ShownHunterActionTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "NONE, false",
    "RAISE_CRUCIFIX, true",
    "RAISE_CRUCIFIX;RAISE_CRUCIFIX, true;false",
    "RAISE_CRUCIFIX;NONE;RAISE_CRUCIFIX, true;true;true",
    "THROW_HOLY_WATER;CROSSBOW_SHOT, true;true"
  })
  @DisplayName("a clip restarts only when the shown action changes")
  void restartsOnlyOnChange(final String actions, final String changes) {
    final ShownHunterAction shown = new ShownHunterAction();
    final List<Boolean> seen =
        Arrays.stream(actions.split(";")).map(a -> shown.changes(HunterAction.valueOf(a))).toList();
    assertEquals(Arrays.stream(changes.split(";")).map(Boolean::parseBoolean).toList(), seen);
  }
}
