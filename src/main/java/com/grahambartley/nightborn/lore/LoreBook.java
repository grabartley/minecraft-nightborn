package com.grahambartley.nightborn.lore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record LoreBook(Text title, String author, List<Text> pages) {

  public static final int MAX_PAGES = WritableBookContentComponent.MAX_PAGE_COUNT;
  public static final int SPINE_LENGTH = WrittenBookContentComponent.MAX_TITLE_LENGTH;

  public static final Codec<LoreBook> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      TextCodecs.CODEC.fieldOf("title").forGetter(LoreBook::title),
                      Codec.STRING
                          .validate(LoreBook::named)
                          .fieldOf("author")
                          .forGetter(LoreBook::author),
                      TextCodecs.CODEC
                          .listOf(1, MAX_PAGES)
                          .fieldOf("pages")
                          .forGetter(LoreBook::pages))
                  .apply(instance, LoreBook::new));

  public LoreBook {
    pages = List.copyOf(pages);
  }

  static DataResult<String> named(final String author) {
    return author.isBlank()
        ? DataResult.error(() -> "A lore book needs an author")
        : DataResult.success(author);
  }

  static String spine(final String title) {
    return title.length() <= SPINE_LENGTH ? title : title.substring(0, SPINE_LENGTH);
  }

  public ItemStack stack() {
    final ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
    stack.set(DataComponentTypes.ITEM_NAME, title);
    stack.set(
        DataComponentTypes.WRITTEN_BOOK_CONTENT,
        new WrittenBookContentComponent(
            RawFilteredPair.of(spine(title.getString())),
            author,
            0,
            pages.stream().map(RawFilteredPair::of).toList(),
            true));
    return stack;
  }
}
