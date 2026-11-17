package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NamePoolTest {

  private static final Path POOLS =
      Path.of("src/main/resources/data/nightborn/nightborn/name_pool");

  static NamePool read(final String pool) throws IOException {
    return NamePool.CODEC
        .parse(
            JsonOps.INSTANCE,
            JsonParser.parseString(Files.readString(POOLS.resolve(pool + ".json"))))
        .getOrThrow();
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "vampire_men, 250",
    "vampire_women, 250",
    "hunter_men, 250",
    "hunter_women, 250",
    "familiar_men, 250",
    "familiar_women, 250",
    "vampire_epithets, 500",
    "eldest_epithets, 1"
  })
  @DisplayName("each shipped pool holds its full count of different entries")
  void counts(final String pool, final int size) throws IOException {
    final List<String> names = read(pool).names();
    assertEquals(size, names.size());
    assertEquals(size, Set.copyOf(names).size());
  }

  @ParameterizedTest(name = "{0} and {1}")
  @CsvSource({
    "vampire_men, vampire_women",
    "hunter_men, hunter_women",
    "familiar_men, familiar_women"
  })
  @DisplayName("no name is shared between the men's and women's pools of one kind")
  void noSharedNames(final String men, final String women) throws IOException {
    final Set<String> seen = new HashSet<>();
    for (final String name : read(men).names()) {
      seen.add(name.toLowerCase(Locale.ROOT));
    }
    read(women).names().forEach(name -> assertTrue(seen.add(name.toLowerCase(Locale.ROOT)), name));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "vampire_men",
    "vampire_women",
    "hunter_men",
    "hunter_women",
    "familiar_men",
    "familiar_women"
  })
  @DisplayName("every given name passes the same rules as a typed name")
  void namesPassRules(final String pool) throws IOException {
    read(pool)
        .names()
        .forEach(name -> assertEquals(name, NameRules.check(name).name().orElse(""), name));
  }

  @Test
  @DisplayName("the 500 epithets have different keys, and the Eldest's is not among them")
  void epithetKeys() throws IOException {
    final List<String> epithets = read("vampire_epithets").names();
    assertEquals(
        epithets.size(), epithets.stream().map(Epithets::slug).collect(Collectors.toSet()).size());
    assertTrue(!epithets.contains("the Eldest"));
    assertEquals(List.of("the Eldest"), read("eldest_epithets").names());
  }

  @Test
  @DisplayName("a pool keeps its own copy of the names")
  void copies() {
    final List<String> names = new java.util.ArrayList<>(List.of("Hester"));
    final NamePool pool = new NamePool(names);
    names.add("Wren");
    assertEquals(List.of("Hester"), pool.names());
  }
}
