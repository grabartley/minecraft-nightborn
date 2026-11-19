package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireReadingTest {

  private static final Map<String, String> LINES =
      Map.of(
          "noble/mesmerise", "noble",
          "noble/glamour", "noble",
          "occult/hex", "occult",
          "beast/pounce", "beast");

  private static List<Identifier> ids(final String names) {
    return names.isEmpty()
        ? List.of()
        : Arrays.stream(names.split(" ")).map(name -> Identifier.of("nightborn", name)).toList();
  }

  @ParameterizedTest(name = "[{0}] -> [{1}]")
  @CsvSource({
    "'', ''",
    "core/vanish, ''",
    "noble/mesmerise noble/glamour, noble",
    "occult/hex core/vanish noble/mesmerise, occult noble",
    "beast/pounce occult/hex noble/glamour, beast occult noble"
  })
  @DisplayName("a vampire's bloodlines are the lines of its skills, once each in order")
  void lines(final String owned, final String lines) {
    assertEquals(
        ids(lines),
        VampireReading.lines(
            ids(owned),
            id ->
                Optional.ofNullable(LINES.get(id.getPath()))
                    .map(line -> Identifier.of("nightborn", line))));
  }
}
