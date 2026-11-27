package com.grahambartley.nightborn.lore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class LoreBookTest {

  private static LoreBook book(final String title, final int pages) {
    return new LoreBook(
        Text.literal(title),
        "Dame Perpetua",
        IntStream.range(0, pages).<Text>mapToObj(page -> Text.literal("page " + page)).toList());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"title\":\"A\",\"author\":\"B\",\"pages\":[\"one\"]}",
        "{\"title\":{\"translate\":\"a.title\"},\"author\":\"B\",\"pages\":[{\"translate\":\"a.page.1\"},\"two\"]}",
        "{\"title\":{\"text\":\"A\"},\"author\":\"B of C\",\"pages\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\"]}"
      })
  @DisplayName("a book with a title, an author and pages loads")
  void aWholeBookLoads(final String json) {
    assertTrue(LoreBook.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"author\":\"B\",\"pages\":[\"one\"]}",
        "{\"title\":\"A\",\"pages\":[\"one\"]}",
        "{\"title\":\"A\",\"author\":\"\",\"pages\":[\"one\"]}",
        "{\"title\":\"A\",\"author\":\"   \",\"pages\":[\"one\"]}",
        "{\"title\":\"A\",\"author\":\"B\"}",
        "{\"title\":\"A\",\"author\":\"B\",\"pages\":[]}"
      })
  @DisplayName("a book missing its title, author or pages is refused")
  void aBrokenBookIsRefused(final String json) {
    assertTrue(LoreBook.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "{0} loads as it was saved")
  @CsvSource({"Ledger of the Cellar, 3", "Of the Eldest, 2", "A, 6"})
  @DisplayName("a book survives being saved and loaded")
  void aBookRoundTrips(final String title, final int pages) {
    final LoreBook book = book(title, pages);
    assertEquals(
        book,
        LoreBook.CODEC
            .parse(
                JsonOps.INSTANCE, LoreBook.CODEC.encodeStart(JsonOps.INSTANCE, book).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest(name = "\"{0}\" is cut to \"{1}\"")
  @CsvSource({
    "Of the Eldest, Of the Eldest",
    "The Staff's Grievances Answered!, The Staff's Grievances Answered!",
    "The Portrait in the Long Gallery!, The Portrait in the Long Gallery",
    "A Very Long Title That Would Never Fit Upon Any Spine, A Very Long Title That Would Nev"
  })
  @DisplayName("the title on the spine fits a written book")
  void theSpineFits(final String title, final String spine) {
    assertEquals(spine, LoreBook.spine(title));
    assertTrue(LoreBook.spine(title).length() <= LoreBook.SPINE_LENGTH);
  }

  @ParameterizedTest(name = "{0} with {1} pages")
  @CsvSource({
    "Ledger of the Cellar, 3",
    "Of the Eldest, 2",
    "A Very Long Title That Would Never Fit Upon Any Spine, 6"
  })
  @DisplayName("a book becomes a finished written book that shows its full title")
  void aBookBecomesAWrittenBook(final String title, final int pages) {
    final ItemStack stack = book(title, pages).stack();
    assertTrue(stack.isOf(Items.WRITTEN_BOOK));
    assertEquals(title, stack.getName().getString());
    final WrittenBookContentComponent content = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
    assertEquals("Dame Perpetua", content.author());
    assertEquals(LoreBook.spine(title), content.title().raw());
    assertEquals(pages, content.pages().size());
    assertEquals("page 0", content.pages().get(0).raw().getString());
    assertTrue(content.resolved());
    assertEquals(0, content.generation());
  }

  @ParameterizedTest(name = "{0} pages")
  @ValueSource(ints = {1, 3, 6})
  @DisplayName("a book keeps its own copy of its pages")
  void pagesAreCopied(final int pages) {
    final List<Text> written = new ArrayList<>(book("A", pages).pages());
    final LoreBook book = new LoreBook(Text.literal("A"), "B", written);
    written.clear();
    assertEquals(pages, book.pages().size());
  }
}
