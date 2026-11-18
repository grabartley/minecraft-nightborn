package com.grahambartley.nightborn.house.crest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.decor.DecorItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class CrestBanners {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "crest_banner");
  static final int STACK = 16;

  public static final ComponentType<Crest> CREST =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(Nightborn.MOD_ID, "crest"),
          ComponentType.<Crest>builder()
              .codec(Crest.CODEC)
              .packetCodec(Crest.PACKET_CODEC)
              .build());

  public static final Block BLOCK =
      Registry.register(
          Registries.BLOCK,
          ID,
          new CrestBannerBlock(
              AbstractBlock.Settings.create()
                  .mapColor(MapColor.OAK_TAN)
                  .instrument(NoteBlockInstrument.BASS)
                  .noCollision()
                  .strength(1.0F)
                  .sounds(BlockSoundGroup.WOOD)
                  .burnable()
                  .nonOpaque()));

  public static final Item ITEM =
      Registry.register(
          Registries.ITEM, ID, new CrestBannerItem(BLOCK, new Item.Settings().maxCount(STACK)));

  public static final BlockEntityType<CrestBannerBlockEntity> BLOCK_ENTITY =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          ID,
          BlockEntityType.Builder.create(CrestBannerBlockEntity::new, BLOCK).build());

  private CrestBanners() {}

  public static void initialize() {
    ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, DecorItemGroup.ID))
        .register(entries -> entries.add(ITEM));
  }
}
