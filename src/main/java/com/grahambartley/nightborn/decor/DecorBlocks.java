package com.grahambartley.nightborn.decor;

import com.grahambartley.nightborn.Nightborn;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class DecorBlocks {

  private static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
  private static final int WOOD_BURN = 5;
  private static final int WOOD_SPREAD = 20;
  private static final int CLOTH_BURN = 30;
  private static final int CLOTH_SPREAD = 60;

  public static final Block CANDELABRA =
      register(
          "candelabra",
          CandelabraBlock::new,
          AbstractBlock.Settings.create()
              .mapColor(MapColor.GOLD)
              .strength(1.5f)
              .sounds(BlockSoundGroup.LANTERN)
              .nonOpaque()
              .luminance(
                  state -> state.get(LightableFurnitureBlock.LIT) ? CandelabraBlock.LIGHT : 0));
  public static final Block CHANDELIER =
      register(
          "chandelier",
          ChandelierBlock::new,
          AbstractBlock.Settings.create()
              .mapColor(MapColor.GOLD)
              .strength(1.5f)
              .sounds(BlockSoundGroup.CHAIN)
              .nonOpaque()
              .luminance(
                  state -> state.get(LightableFurnitureBlock.LIT) ? ChandelierBlock.LIGHT : 0));
  public static final Block BURGUNDY_VELVET_DRAPE =
      register(
          "burgundy_velvet_drape",
          DrapeBlock::new,
          AbstractBlock.Settings.copy(Blocks.RED_WOOL).mapColor(MapColor.DARK_RED).nonOpaque());
  public static final Block EMERALD_VELVET_DRAPE =
      register(
          "emerald_velvet_drape",
          DrapeBlock::new,
          AbstractBlock.Settings.copy(Blocks.GREEN_WOOL)
              .mapColor(MapColor.EMERALD_GREEN)
              .nonOpaque());
  public static final Block GOTHIC_STAINED_GLASS =
      register(
          "gothic_stained_glass", TransparentBlock::new, AbstractBlock.Settings.copy(Blocks.GLASS));
  public static final Block GOTHIC_STAINED_GLASS_PANE =
      register(
          "gothic_stained_glass_pane",
          PaneBlock::new,
          AbstractBlock.Settings.copy(Blocks.GLASS_PANE));
  public static final Block ORNATE_CHAIR =
      register(
          "ornate_chair",
          ChairBlock::new,
          AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS).mapColor(MapColor.BLACK).nonOpaque());
  public static final Block ORNATE_TABLE =
      register(
          "ornate_table",
          TableBlock::new,
          AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS).mapColor(MapColor.BLACK).nonOpaque());

  static {
    BuildingFamily.ALL.forEach(DecorBlocks::registerFamily);
  }

  private DecorBlocks() {}

  public static void initialize() {
    final FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
    flammable.add(ORNATE_CHAIR, WOOD_BURN, WOOD_SPREAD);
    flammable.add(ORNATE_TABLE, WOOD_BURN, WOOD_SPREAD);
    flammable.add(BURGUNDY_VELVET_DRAPE, CLOTH_BURN, CLOTH_SPREAD);
    flammable.add(EMERALD_VELVET_DRAPE, CLOTH_BURN, CLOTH_SPREAD);
    BuildingFamily.ALL.stream()
        .filter(family -> family.material() == BuildingFamily.Material.WOOD)
        .flatMap(family -> family.ids().stream())
        .forEach(id -> flammable.add(get(id), WOOD_BURN, WOOD_SPREAD));
  }

  public static Block get(final String id) {
    final Block block = BLOCKS.get(id);
    if (block == null) {
      throw new IllegalArgumentException("No decor block " + id);
    }
    return block;
  }

  public static List<Block> all() {
    return List.copyOf(BLOCKS.values());
  }

  private static void registerFamily(final BuildingFamily family) {
    final AbstractBlock.Settings settings =
        family.material() == BuildingFamily.Material.STONE
            ? AbstractBlock.Settings.copy(Blocks.POLISHED_BLACKSTONE_BRICKS)
            : AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS).mapColor(MapColor.BLACK);
    final Block base = register(family.baseId(), s -> FamilyMember.BASE.create(null, s), settings);
    family.members().stream()
        .filter(member -> member != FamilyMember.BASE)
        .sorted()
        .forEach(
            member ->
                register(
                    member.idFor(family.baseId()),
                    s -> member.create(base, s),
                    AbstractBlock.Settings.copy(base)));
  }

  private static Block register(
      final String id,
      final Function<AbstractBlock.Settings, Block> factory,
      final AbstractBlock.Settings settings) {
    final Identifier key = Identifier.of(Nightborn.MOD_ID, id);
    final Block block = Registry.register(Registries.BLOCK, key, factory.apply(settings));
    Registry.register(Registries.ITEM, key, new BlockItem(block, new Item.Settings()));
    BLOCKS.put(id, block);
    return block;
  }
}
