package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.thrall.ThrallEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DayJobOrdersTest {

  @ParameterizedTest
  @ValueSource(strings = {"nightborn:day_job"})
  @DisplayName("day jobs answer to one action, told apart by the job's argument")
  void id(final String id) {
    assertEquals(id, new DayJobOrders<ThrallEntity>().id().toString());
    assertEquals(id, DayJobOrders.ID.toString());
  }
}
