package com.grahambartley.nightborn.lore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class LoreAssetsTest {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn");
  private static final Path BOOKS = DATA.resolve("nightborn/lore_book");
  private static final Path SHELVES = DATA.resolve("tags/nightborn/lore_book");
  private static final Path PROCESSORS = DATA.resolve("worldgen/processor_list/vampire");
  private static final Path POOLS = DATA.resolve("worldgen/template_pool/vampire");
  private static final Path LANG = Path.of("src/main/resources/assets/nightborn/lang");
  private static final int FEWEST_BOOKS = 20;
  private static final int FEWEST_PAGES = 2;
  private static final int MOST_PAGES = 6;
  private static final int FEWEST_ON_A_SHELF = 5;
  private static final int LONGEST_PAGE = 256;
  private static final int LONGEST_WIDE_PAGE = 120;
  private static final Set<String> WIDE_SCRIPTS = Set.of("zh_cn");

  static Stream<String> kinds() {
    return Stream.of("castle", "crypt", "lair", "manor", "mountain_castle");
  }

  static Stream<String> languages() {
    return Stream.of("en_us", "de_de", "es_es", "fr_fr", "pt_br", "ru_ru", "zh_cn");
  }

  static Stream<String> books() {
    return names(BOOKS).stream();
  }

  static Stream<Arguments> booksInEveryLanguage() {
    return languages().flatMap(language -> books().map(book -> Arguments.of(language, book)));
  }

  @Test
  @DisplayName("at least 20 books ship")
  void enoughBooksShip() {
    assertTrue(names(BOOKS).size() >= FEWEST_BOOKS);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("books")
  @DisplayName("every book loads with a title, a vampire author and 2 to 6 pages")
  void everyBookLoads(final String name) {
    final LoreBook book = book(name);
    assertFalse(book.author().isBlank());
    assertTrue(book.pages().size() >= FEWEST_PAGES && book.pages().size() <= MOST_PAGES);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("books")
  @DisplayName("every book sits on at least one shelf")
  void everyBookIsShelved(final String name) {
    assertTrue(kinds().anyMatch(kind -> shelf(kind).contains("nightborn:" + name)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("books")
  @DisplayName("no two books share an author")
  void authorsAreDistinct(final String name) {
    final String author = book(name).author();
    assertEquals(1, books().filter(other -> book(other).author().equals(author)).count());
  }

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource("booksInEveryLanguage")
  @DisplayName("every title and page is written in every language and fits a page")
  void everyBookIsTranslated(final String language, final String name) {
    final JsonObject lang = json(LANG.resolve(language + ".json"));
    final LoreBook book = book(name);
    final String title = translated(lang, book.title());
    assertFalse(title.isBlank());
    final int longest = WIDE_SCRIPTS.contains(language) ? LONGEST_WIDE_PAGE : LONGEST_PAGE;
    for (final Text page : book.pages()) {
      final String text = translated(lang, page);
      assertFalse(text.isBlank());
      assertTrue(text.length() <= longest, language + " " + name + " runs off its page");
      assertFalse(text.contains("%"), language + " " + name + " would read as a placeholder");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("kinds")
  @DisplayName("every shelf holds enough real books that two houses rarely match")
  void everyShelfHoldsRealBooks(final String kind) {
    final List<String> shelf = shelf(kind);
    assertTrue(shelf.size() >= FEWEST_ON_A_SHELF);
    assertEquals(shelf.size(), new HashSet<>(shelf).size());
    for (final String entry : shelf) {
      assertTrue(names(BOOKS).contains(entry.replace("nightborn:", "")), kind + " " + entry);
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("kinds")
  @DisplayName("every structure's processor list fills lecterns from its own shelf")
  void everyKindFillsItsLecterns(final String kind) {
    final List<JsonElement> processors =
        json(PROCESSORS.resolve(kind + ".json")).getAsJsonArray("processors").asList();
    assertEquals(
        List.of("nightborn:" + kind),
        processors.stream()
            .map(JsonElement::getAsJsonObject)
            .filter(
                processor ->
                    processor.get("processor_type").getAsString().equals("nightborn:lectern_book"))
            .map(processor -> processor.get("books").getAsString())
            .toList());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("kinds")
  @DisplayName("every piece of every structure is placed through its processor list")
  void everyPieceUsesItsProcessors(final String kind) {
    final List<String> pools = names(POOLS.resolve(kind));
    assertFalse(pools.isEmpty());
    for (final String pool : pools) {
      for (final JsonElement element :
          json(POOLS.resolve(kind).resolve(pool + ".json")).getAsJsonArray("elements")) {
        assertEquals(
            "nightborn:vampire/" + kind,
            element.getAsJsonObject().getAsJsonObject("element").get("processors").getAsString(),
            kind + "/" + pool);
      }
    }
  }

  private static LoreBook book(final String name) {
    return LoreBook.CODEC.parse(JsonOps.INSTANCE, json(BOOKS.resolve(name + ".json"))).getOrThrow();
  }

  private static List<String> shelf(final String kind) {
    final List<String> entries = new ArrayList<>();
    json(SHELVES.resolve(kind + ".json"))
        .getAsJsonArray("values")
        .forEach(entry -> entries.add(entry.getAsString()));
    return entries;
  }

  private static String translated(final JsonObject lang, final Text text) {
    final String key = ((TranslatableTextContent) text.getContent()).getKey();
    assertTrue(lang.has(key), key);
    return lang.get(key).getAsString();
  }

  private static List<String> names(final Path directory) {
    try (Stream<Path> files = Files.list(directory)) {
      return files
          .map(file -> file.getFileName().toString())
          .filter(file -> file.endsWith(".json"))
          .map(file -> file.substring(0, file.length() - ".json".length()))
          .sorted()
          .toList();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static JsonObject json(final Path file) {
    try {
      return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8))
          .getAsJsonObject();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
