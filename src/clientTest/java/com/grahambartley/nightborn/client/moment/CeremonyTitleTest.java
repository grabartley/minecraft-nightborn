package com.grahambartley.nightborn.client.moment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CeremonyTitleTest {

  @ParameterizedTest(name = "text {0} wide on a {1} card -> scale {2}")
  @CsvSource({
    "40, 192, 2",
    "84, 192, 2",
    "96, 192, 1.75",
    "112, 192, 1.5",
    "168, 192, 1",
    "0, 192, 2",
    "50, 20, 2"
  })
  @DisplayName("a card title is drawn large but always fits inside the card's margins")
  void scale(final int textWidth, final int cardWidth, final float scale) {
    assertEquals(scale, CeremonyTitle.scale(textWidth, cardWidth), 1.0E-4F);
  }
}
