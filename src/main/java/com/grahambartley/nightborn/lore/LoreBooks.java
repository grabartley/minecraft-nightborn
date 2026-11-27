package com.grahambartley.nightborn.lore;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class LoreBooks {

  public static final RegistryKey<Registry<LoreBook>> BOOK =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "lore_book"));

  private LoreBooks() {}

  public static void register() {
    DynamicRegistries.register(BOOK, LoreBook.CODEC);
  }

  public static List<LoreBook> shelf(
      final DynamicRegistryManager registries, final TagKey<LoreBook> shelf) {
    return registries
        .getOptional(BOOK)
        .flatMap(registry -> registry.getEntryList(shelf))
        .map(books -> books.stream().map(RegistryEntry::value).toList())
        .orElse(List.of());
  }
}
