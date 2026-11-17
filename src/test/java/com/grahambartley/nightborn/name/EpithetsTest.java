package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EpithetsTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "the Relentless, relentless",
    "the Thrice-Buried, thrice_buried",
    "the Face at the Window, face_at_the_window",
    "the Eldest, eldest",
    "Pale, pale"
  })
  @DisplayName("an epithet's slug drops the article and keeps only letters, digits and underscores")
  void slug(final String epithet, final String slug) {
    assertEquals(slug, Epithets.slug(epithet));
  }

  @ParameterizedTest(name = "{0} female={1}")
  @CsvSource({
    "the Relentless, false, epithet.nightborn.relentless.male",
    "the Relentless, true, epithet.nightborn.relentless.female"
  })
  @DisplayName("each epithet has a key for a man and a key for a woman")
  void key(final String epithet, final boolean female, final String key) {
    assertEquals(key, Epithets.key(epithet, female));
  }

  @ParameterizedTest(name = "{0} + \"{1}\"")
  @CsvSource({"Aurelian, the Pale, Aurelian the Pale", "Tobias, '', Tobias"})
  @DisplayName("a given name and epithet join with one space, and no epithet leaves the name alone")
  void join(final String given, final String epithet, final String full) {
    assertEquals(full, Epithets.join(given, epithet));
  }

  @ParameterizedTest(name = "\"{0}\" with \"{1}\"")
  @CsvSource({
    "Aurelian the Pale, the Pale, Aurelian",
    "Anne Marie the Pale, the Pale, Anne Marie",
    "Wren, the Pale, ",
    "the Pale, the Pale, ",
    "Aurelian the Pale, '', ",
    "Aurelian the Paler, the Pale, "
  })
  @DisplayName("the given name is found only when the full name still ends in its epithet")
  void given(final String full, final String epithet, final String given) {
    assertEquals(Optional.ofNullable(given), Epithets.given(full, epithet));
  }
}
