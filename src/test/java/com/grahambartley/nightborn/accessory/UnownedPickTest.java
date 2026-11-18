package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UnownedPickTest {

  private static final List<String> RINGS = List.of("court", "beast", "blood");

  @ParameterizedTest(name = "owned {0}: {1}")
  @CsvSource({
    "'', 'court,beast,blood'",
    "'court', 'beast,blood'",
    "'court,beast', 'blood'",
    "'court,beast,blood', 'court,beast,blood'",
    "'court|beast', 'blood'",
    "'court|court,beast,blood', 'beast,blood'"
  })
  @DisplayName("a striker never gets a ring they own while they lack another")
  void candidates(final String owned, final String fair) {
    assertEquals(list(fair), UnownedPick.candidates(RINGS, owners(owned)));
  }

  private static List<Set<String>> owners(final String owned) {
    return Arrays.stream(owned.split("\\|", -1))
        .map(each -> each.isBlank() ? Set.<String>of() : Set.copyOf(list(each)))
        .toList();
  }

  private static List<String> list(final String names) {
    return Arrays.stream(names.split(",")).collect(Collectors.toList());
  }
}
