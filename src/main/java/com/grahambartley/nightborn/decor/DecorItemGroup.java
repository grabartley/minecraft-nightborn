package com.grahambartley.nightborn.decor;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.gramophone.MusicDiscs;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class DecorItemGroup {

  public static final String TRANSLATION_KEY = "itemGroup.nightborn.decor";
  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "decor");

  public static final ItemGroup DECOR =
      Registry.register(
          Registries.ITEM_GROUP,
          ID,
          FabricItemGroup.builder()
              .icon(() -> new ItemStack(DecorBlocks.CANDELABRA))
              .displayName(Text.translatable(TRANSLATION_KEY))
              .entries(
                  (context, entries) -> {
                    DecorBlocks.all().forEach(entries::add);
                    MusicDiscs.all().forEach(entries::add);
                  })
              .build());

  private DecorItemGroup() {}

  public static void initialize() {}
}
