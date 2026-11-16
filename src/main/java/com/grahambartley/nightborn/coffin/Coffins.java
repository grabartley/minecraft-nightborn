package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.decor.DecorItemGroup;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

  private static final Map<CoffinMaterial, Block> BLOCKS = registerBlocks();
  private static final Map<Block, CoffinMaterial> MATERIALS =
      BLOCKS.entrySet().stream()
          .collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey));

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
        (material, block) -> {
          final FlammableBlockRegistry.Entry fire = flammable.get(material.source());
          if (fire.getBurnChance() > 0) {
            flammable.add(block, fire);
          }
        });
    ItemGroupEvents.modifyEntriesEvent(
            Registries.ITEM_GROUP.getKey(DecorItemGroup.DECOR).orElseThrow())
        .register(entries -> BLOCKS.values().forEach(entries::add));
    CoffinProtection.register();
    CoffinOwnerDeaths.register();
  }

  public static Block get(final CoffinMaterial material) {
    return BLOCKS.get(material);
  }

  public static CoffinMaterial material(final Block coffin) {
    return MATERIALS.get(coffin);
  }

  public static List<Block> all() {
    return List.copyOf(BLOCKS.values());
  }

  private static Map<CoffinMaterial, Block> registerBlocks() {
    final Map<CoffinMaterial, Block> blocks = new EnumMap<>(CoffinMaterial.class);
    for (final CoffinMaterial material : CoffinMaterial.values()) {
      final Identifier id = Identifier.of(Nightborn.MOD_ID, material.id());
      final Block block =
          Registry.register(
              Registries.BLOCK,
              id,
              new CoffinBlock(
                  AbstractBlock.Settings.copy(material.source())
                      .nonOpaque()
                      .pistonBehavior(PistonBehavior.DESTROY)));
      Registry.register(Registries.ITEM, id, new BedItem(block, new Item.Settings().maxCount(1)));
      blocks.put(material, block);
    }
    return blocks;
  }
}
