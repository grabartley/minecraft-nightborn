package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.decor.DecorItemGroup;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class Coffins {

  private static final Map<CoffinWood, Block> BLOCKS = registerBlocks();

  public static final BlockEntityType<CoffinBlockEntity> BLOCK_ENTITY =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, "coffin"),
          BlockEntityType.Builder.create(
                  CoffinBlockEntity::new, BLOCKS.values().toArray(Block[]::new))
              .build());

  private Coffins() {}

  public static void initialize() {
    final FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
    BLOCKS.forEach(
        (wood, block) -> {
          final FlammableBlockRegistry.Entry planks = flammable.get(wood.planks());
          if (planks.getBurnChance() > 0) {
            flammable.add(block, planks);
          }
        });
    ItemGroupEvents.modifyEntriesEvent(
            Registries.ITEM_GROUP.getKey(DecorItemGroup.DECOR).orElseThrow())
        .register(entries -> BLOCKS.values().forEach(entries::add));
  }

  public static Block get(final CoffinWood wood) {
    return BLOCKS.get(wood);
  }

  public static List<Block> all() {
    return List.copyOf(BLOCKS.values());
  }

  private static Map<CoffinWood, Block> registerBlocks() {
    final Map<CoffinWood, Block> blocks = new EnumMap<>(CoffinWood.class);
    for (final CoffinWood wood : CoffinWood.values()) {
      final Identifier id = Identifier.of(Nightborn.MOD_ID, wood.id());
      final Block block =
          Registry.register(
              Registries.BLOCK,
              id,
              new CoffinBlock(
                  AbstractBlock.Settings.copy(wood.planks())
                      .nonOpaque()
                      .pistonBehavior(PistonBehavior.DESTROY)));
      Registry.register(Registries.ITEM, id, new BedItem(block, new Item.Settings().maxCount(1)));
      blocks.put(wood, block);
    }
    return blocks;
  }
}
