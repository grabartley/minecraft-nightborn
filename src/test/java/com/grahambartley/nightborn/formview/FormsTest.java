package com.grahambartley.nightborn.formview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FormsTest {

  @ParameterizedTest(name = "on {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("a registered form answers with its own test")
  void answersWithItsTest(final boolean on) {
    final Identifier id = Identifier.of("nightborn_test", "answers_" + on);
    Forms.register(id, entity -> on);
    assertTrue(Forms.known(id));
    assertEquals(on, Forms.on(id, null));
  }

  @Test
  @DisplayName("an unknown form is never on")
  void unknownIsOff() {
    final Identifier id = Identifier.of("nightborn_test", "never_registered");
    assertFalse(Forms.known(id));
    assertFalse(Forms.on(id, null));
  }

  @Test
  @DisplayName("a form id cannot be registered twice")
  void noDuplicates() {
    final Identifier id = Identifier.of("nightborn_test", "twice");
    Forms.register(id, entity -> true);
    assertThrows(IllegalStateException.class, () -> Forms.register(id, entity -> false));
    assertTrue(Forms.on(id, null));
  }
}
