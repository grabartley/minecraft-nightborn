package com.grahambartley.nightborn.servant;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.occult.GhostlyFamiliarEntity;
import com.grahambartley.nightborn.thrall.ThrallEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ServantTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(classes = {ThrallEntity.class, GhostlyFamiliarEntity.class})
  @DisplayName("every bound creature answers to one master the same way")
  void servesAMaster(final Class<?> type) {
    assertTrue(Servant.class.isAssignableFrom(type));
  }
}
