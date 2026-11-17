package com.grahambartley.nightborn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.clothing.ClothingPiece;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.config.Setting;
import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.name.Epithets;
import com.grahambartley.nightborn.name.NameRules;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LangAssetsTest {

  private static final Path LANG = Path.of("src/main/resources/assets/nightborn/lang");
  private static final Path ENCHANTMENTS = Path.of("src/main/resources/data/nightborn/enchantment");
  private static final Path NAME_POOLS =
      Path.of("src/main/resources/data/nightborn/nightborn/name_pool");
  private static final Pattern PLACEHOLDER = Pattern.compile("%(?:\\d+\\$)?s|%%");

  static Stream<String> languages() {
    return Stream.of("de_de", "es_es", "fr_fr", "pt_br", "ru_ru", "zh_cn");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("languages")
  @DisplayName("every shipped language has every English key, with the same placeholders")
  void matchesEnglish(final String language) throws IOException {
    final JsonObject english = read("en_us");
    final JsonObject other = read(language);
    assertEquals(english.keySet(), other.keySet());
    for (final String key : english.keySet()) {
      assertEquals(
          placeholders(english.get(key).getAsString()),
          placeholders(other.get(key).getAsString()),
          language + " " + key);
    }
  }

  @Test
  @DisplayName(
      "every setting, section, faction, blood quality, clothing piece, enchantment, naming problem and epithet has its English text")
  void englishCoversTheCode() throws IOException {
    final JsonObject english = read("en_us");
    final List<String> needed = new ArrayList<>();
    for (final Setting<?> setting : NightbornConfig.SETTINGS) {
      needed.add(setting.translationKey());
      needed.add(setting.tooltipKey());
    }
    NightbornConfig.sections().forEach(section -> needed.add("nightborn.section." + section));
    for (final Faction faction : Faction.values()) {
      needed.add("nightborn.faction." + faction.asString());
      needed.add("nightborn.faction." + faction.asString() + ".tag");
    }
    for (final BloodQuality quality : BloodQuality.values()) {
      needed.add(quality.translationKey());
    }
    for (final ClothingPiece piece : ClothingPiece.values()) {
      needed.add("item.nightborn." + piece.id());
    }
    try (Stream<Path> enchantments = Files.list(ENCHANTMENTS)) {
      enchantments.forEach(
          file ->
              needed.add(
                  "enchantment.nightborn." + file.getFileName().toString().replace(".json", "")));
    }
    for (final NameRules.Problem problem : NameRules.Problem.values()) {
      needed.add(problem.translationKey());
    }
    for (final String pool : List.of("vampire_epithets", "eldest_epithets")) {
      JsonParser.parseString(
              Files.readString(NAME_POOLS.resolve(pool + ".json"), StandardCharsets.UTF_8))
          .getAsJsonObject()
          .getAsJsonArray("names")
          .forEach(
              epithet -> {
                needed.add(Epithets.key(epithet.getAsString(), false));
                needed.add(Epithets.key(epithet.getAsString(), true));
              });
    }
    needed.forEach(key -> assertTrue(english.has(key), key));
  }

  private static List<String> placeholders(final String text) {
    final List<String> found = new ArrayList<>();
    final Matcher matcher = PLACEHOLDER.matcher(text);
    while (matcher.find()) {
      found.add(matcher.group().replaceAll("\\d+\\$", ""));
    }
    return found;
  }

  private static JsonObject read(final String language) throws IOException {
    return JsonParser.parseString(
            Files.readString(LANG.resolve(language + ".json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }
}
