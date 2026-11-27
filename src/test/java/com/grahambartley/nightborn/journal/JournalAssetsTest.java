package com.grahambartley.nightborn.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class JournalAssetsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final Path DATA = Path.of("src/main/resources/data/nightborn/patchouli_books");
  private static final String NARRATED = "nightborn.journal.";
  private static final int PLATE_SIZE = 256;
  private static final int PAGE_SIZE = 200;

  static Stream<String> books() {
    return Stream.of("vampire_journal", "hunter_journal");
  }

  static Stream<String> languages() {
    return Stream.of("en_us", "de_de", "es_es", "fr_fr", "pt_br", "ru_ru", "zh_cn");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("books")
  void everyNameAndPageOfABookHasItsEnglishText(final String book) throws IOException {
    final JsonObject english = lang("en_us");
    final List<String> missing = new ArrayList<>();
    for (final String key : textKeys(book)) {
      if (!english.has(key)) {
        missing.add(key);
      }
    }
    assertEquals(List.of(), missing);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("books")
  void everyPlateIsOnDiskAndDrawnInsideThePage(final String book) throws IOException {
    final List<String> wrong = new ArrayList<>();
    for (final String plate : plates(book)) {
      final Path file = ASSETS.resolve(plate.substring("nightborn:".length()));
      if (!Files.exists(file)) {
        wrong.add(plate + " is missing");
        continue;
      }
      final BufferedImage image = ImageIO.read(file.toFile());
      if (image.getWidth() != PLATE_SIZE || image.getHeight() != PLATE_SIZE) {
        wrong.add(plate + " is not " + PLATE_SIZE + " square");
      } else if (spillsPastThePage(image)) {
        wrong.add(plate + " is drawn outside the page");
      }
    }
    assertEquals(List.of(), wrong);
  }

  @ParameterizedTest(name = "{0} leaves at most {1} entries without a plate")
  @CsvSource({"vampire_journal, 1", "hunter_journal, 0"})
  void mostEntriesCarryAPlate(final String book, final int allowedBare) throws IOException {
    final List<String> bare = new ArrayList<>();
    for (final Map.Entry<String, JsonObject> entry : entries(book).entrySet()) {
      if (images(entry.getValue()).isEmpty()) {
        bare.add(entry.getKey());
      }
    }
    assertTrue(bare.size() <= allowedBare, bare.toString());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "player",
        "setting",
        "mob",
        "point",
        "health",
        "screen",
        "wheel",
        "cooldown",
        "block",
        "respawn",
        "bar"
      })
  void noNarratedLineUsesAnOutOfWorldWord(final String word) throws IOException {
    final Pattern banned =
        Pattern.compile("\\b" + word + "s?\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    final JsonObject english = lang("en_us");
    final List<String> offending = new ArrayList<>();
    for (final String book : books().toList()) {
      for (final String key : textKeys(book)) {
        if (banned.matcher(english.get(key).getAsString()).find()) {
          offending.add(key);
        }
      }
    }
    assertEquals(List.of(), offending);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("languages")
  void everyLanguageKeepsTheKeysAndCommandsANarratedLineNames(final String language)
      throws IOException {
    final Pattern kept = Pattern.compile("\\$\\([^)]*\\)|/[a-z]+");
    final JsonObject english = lang("en_us");
    final JsonObject other = lang(language);
    final List<String> changed = new ArrayList<>();
    for (final String key : english.keySet()) {
      if (key.startsWith(NARRATED)
          && !marks(kept, english.get(key).getAsString())
              .equals(marks(kept, other.get(key).getAsString()))) {
        changed.add(key);
      }
    }
    assertEquals(List.of(), changed);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("languages")
  void noNarratedLineIsLeftAsItsKey(final String language) throws IOException {
    final JsonObject other = lang(language);
    for (final String book : books().toList()) {
      for (final String key : textKeys(book)) {
        assertFalse(other.get(key).getAsString().isBlank(), language + " " + key);
      }
    }
  }

  private static boolean spillsPastThePage(final BufferedImage image) {
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        if ((x >= PAGE_SIZE || y >= PAGE_SIZE) && (image.getRGB(x, y) >>> 24) != 0) {
          return true;
        }
      }
    }
    return false;
  }

  private static List<String> marks(final Pattern kept, final String text) {
    return kept.matcher(text).results().map(found -> found.group()).sorted().toList();
  }

  private static List<String> textKeys(final String book) throws IOException {
    final List<String> keys = new ArrayList<>();
    final JsonObject cover = read(DATA.resolve(book).resolve("book.json"));
    keys.add(cover.get("name").getAsString());
    keys.add(cover.get("landing_text").getAsString());
    for (final JsonObject category : folder(book, "categories").values()) {
      keys.add(category.get("name").getAsString());
      keys.add(category.get("description").getAsString());
    }
    for (final JsonObject entry : entries(book).values()) {
      keys.add(entry.get("name").getAsString());
      for (final JsonElement page : entry.getAsJsonArray("pages")) {
        if (page.getAsJsonObject().has("text")) {
          keys.add(page.getAsJsonObject().get("text").getAsString());
        }
      }
    }
    return keys;
  }

  private static List<String> plates(final String book) throws IOException {
    final List<String> plates = new ArrayList<>();
    for (final JsonObject entry : entries(book).values()) {
      plates.addAll(images(entry));
    }
    return plates;
  }

  private static List<String> images(final JsonObject entry) {
    final List<String> images = new ArrayList<>();
    for (final JsonElement page : entry.getAsJsonArray("pages")) {
      if (page.getAsJsonObject().has("images")) {
        page.getAsJsonObject()
            .getAsJsonArray("images")
            .forEach(image -> images.add(image.getAsString()));
      }
    }
    return images;
  }

  private static Map<String, JsonObject> entries(final String book) throws IOException {
    return folder(book, "entries");
  }

  private static Map<String, JsonObject> folder(final String book, final String name)
      throws IOException {
    final Map<String, JsonObject> found = new TreeMap<>();
    final Path root = ASSETS.resolve("patchouli_books").resolve(book).resolve("en_us/" + name);
    try (Stream<Path> files = Files.walk(root)) {
      for (final Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
        found.put(root.relativize(file).toString().toLowerCase(Locale.ROOT), read(file));
      }
    }
    return found;
  }

  private static JsonObject lang(final String language) throws IOException {
    return read(ASSETS.resolve("lang").resolve(language + ".json"));
  }

  private static JsonObject read(final Path file) throws IOException {
    return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
  }
}
