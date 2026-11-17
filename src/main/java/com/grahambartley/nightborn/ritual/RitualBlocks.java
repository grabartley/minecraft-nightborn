package com.grahambartley.nightborn.ritual;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class RitualBlocks {

  static final Identifier ALTAR_ID = Identifier.of(Nightborn.MOD_ID, "ritual_altar");

  public static final Block ALTAR =
      Registry.register(
          Registries.BLOCK,
          ALTAR_ID,
          new RitualAltarBlock(
              AbstractBlock.Settings.copy(Blocks.POLISHED_BLACKSTONE_BRICKS)
                  .nonOpaque()
                  .luminance(state -> state.get(RitualAltarBlock.PHASE).light())));

  public static final Item ALTAR_ITEM =
      Registry.register(Registries.ITEM, ALTAR_ID, new BlockItem(ALTAR, new Item.Settings()));

  public static final BlockEntityType<RitualAltarBlockEntity> ALTAR_ENTITY =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          ALTAR_ID,
          BlockEntityType.Builder.create(RitualAltarBlockEntity::new, ALTAR).build());

  private RitualBlocks() {}

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
        .register(entries -> entries.add(ALTAR_ITEM));
  }
}
