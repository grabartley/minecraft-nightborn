package com.grahambartley.nightborn.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class JournalTest {

  @ParameterizedTest(name = "vampire {0}, hunter {1} -> {2}")
  @CsvSource({
    "true, false, VAMPIRE",
    "true, true, VAMPIRE",
    "false, true, HUNTER",
    "false, false,"
  })
  @DisplayName("a vampire keeps the vampire's journal, a sworn hunter the hunter's, a human none")
  void side(final boolean vampire, final boolean hunter, final Journal expected) {
    assertEquals(Optional.ofNullable(expected), Journal.ofSide(vampire, hunter));
  }

  @ParameterizedTest(name = "{0} is book {1}")
  @CsvSource({"HUNTER, nightborn:hunter_journal", "VAMPIRE, nightborn:vampire_journal"})
  @DisplayName("each journal names its item and its book with one id, and saves by name")
  void ids(final Journal journal, final String id) {
    assertEquals(Identifier.of(id), journal.id());
    assertEquals(
        journal,
        Journal.CODEC
            .parse(
                JsonOps.INSTANCE, Journal.CODEC.encodeStart(JsonOps.INSTANCE, journal).getOrThrow())
            .getOrThrow());
  }

  private static final Path RESOURCES = Path.of("src/main/resources");
  private static final List<String> LANGUAGES =
      List.of("en_us", "de_de", "es_es", "fr_fr", "pt_br", "ru_ru", "zh_cn");

  private static JsonObject read(final Path file) throws IOException {
    return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
  }

  private static Path contents(final Journal journal) {
    return RESOURCES.resolve(
        "assets/nightborn/patchouli_books/" + journal.id().getPath() + "/en_us");
  }

  private static List<JsonObject> all(final Path folder) throws IOException {
    final List<JsonObject> found = new ArrayList<>();
    try (Stream<Path> files = Files.walk(folder)) {
      for (final Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
        found.add(read(file));
      }
    }
    return found;
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Journal.class)
  @DisplayName("each book opens from its own journal item and is never handed out by Patchouli")
  void book(final Journal journal) throws IOException {
    final JsonObject book =
        read(
            RESOURCES.resolve(
                "data/nightborn/patchouli_books/" + journal.id().getPath() + "/book.json"));
    assertEquals(journal.id().toString(), book.get("custom_book_item").getAsString());
    assertTrue(book.get("dont_generate_book").getAsBoolean());
    assertTrue(book.get("use_resource_pack").getAsBoolean());
    assertTrue(book.get("i18n").getAsBoolean());
    assertTrue(Files.isDirectory(contents(journal).resolve("entries")));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Journal.class)
  @DisplayName("every entry stays hidden until a shipped advancement is earned")
  void entriesAreLocked(final Journal journal) throws IOException {
    final List<JsonObject> entries = all(contents(journal).resolve("entries"));
    assertFalse(entries.isEmpty());
    for (final JsonObject entry : entries) {
      final Identifier advancement = Identifier.of(entry.get("advancement").getAsString());
      assertTrue(entry.get("secret").getAsBoolean(), entry.toString());
      assertTrue(
          Files.isRegularFile(
              RESOURCES.resolve(
                  "data/"
                      + advancement.getNamespace()
                      + "/advancement/"
                      + advancement.getPath()
                      + ".json")),
          advancement.toString());
    }
    for (final JsonObject category : all(contents(journal).resolve("categories"))) {
      assertTrue(category.get("secret").getAsBoolean(), category.toString());
    }
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Journal.class)
  @DisplayName("every name and page of a journal is translated into every shipped language")
  void translated(final Journal journal) throws IOException {
    final List<String> keys = new ArrayList<>();
    final JsonObject book =
        read(
            RESOURCES.resolve(
                "data/nightborn/patchouli_books/" + journal.id().getPath() + "/book.json"));
    keys.add(book.get("name").getAsString());
    keys.add(book.get("landing_text").getAsString());
    keys.add("item." + journal.id().toTranslationKey());
    for (final JsonObject category : all(contents(journal).resolve("categories"))) {
      keys.add(category.get("name").getAsString());
      keys.add(category.get("description").getAsString());
    }
    for (final JsonObject entry : all(contents(journal).resolve("entries"))) {
      keys.add(entry.get("name").getAsString());
      for (final JsonElement page : entry.getAsJsonArray("pages")) {
        if (page.getAsJsonObject().has("text")) {
          keys.add(page.getAsJsonObject().get("text").getAsString());
        }
      }
    }
    for (final String language : LANGUAGES) {
      final JsonObject lang =
          read(RESOURCES.resolve("assets/nightborn/lang/" + language + ".json"));
      for (final String key : keys) {
        assertTrue(lang.has(key), language + " lacks " + key);
      }
    }
  }
}
