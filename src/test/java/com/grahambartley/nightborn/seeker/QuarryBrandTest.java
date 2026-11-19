package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QuarryBrandTest {

  @ParameterizedTest(name = "marks until [{0}]: brand until {1}")
  @CsvSource({"'', ", "200, 200", "200;300, 300", "300;200;250, 300"})
  @DisplayName("a quarry wears its brand until the last hunter's mark still on it runs out")
  void latest(final String untils, final Long brand) {
    final List<Long> marks =
        untils.isEmpty() ? List.of() : Arrays.stream(untils.split(";")).map(Long::valueOf).toList();
    assertEquals(Optional.ofNullable(brand), QuarryBrand.latest(marks));
  }
}
