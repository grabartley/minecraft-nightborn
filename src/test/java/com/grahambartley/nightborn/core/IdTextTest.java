package com.grahambartley.nightborn.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IdTextTest {

  @ParameterizedTest(name = "\"{0}\" reads as {1}")
  @CsvSource({
    "'', ''",
    "nightborn:snare, nightborn:snare",
    "textures/entity/x.png, minecraft:textures/entity/x.png",
    "Not An Id, ''"
  })
  @DisplayName("blank or broken text is no id, and an id survives being written and read")
  void parses(final String text, final String expected) {
    final Optional<Identifier> read = IdText.parse(text);
    assertEquals(expected, IdText.write(read));
    assertEquals(read, IdText.parse(IdText.write(read)));
  }
}
