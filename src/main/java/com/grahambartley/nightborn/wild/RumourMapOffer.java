package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.guild.GuildRumour;
import com.grahambartley.nightborn.raid.ExplorationMaps;
import com.grahambartley.nightborn.structure.NearestStructures;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

public final class RumourMapOffer implements TradeOffers.Factory {

  static final int SEARCH_CHUNKS = 100;
  static final byte SCALE = 2;
  static final float PRICE_MULTIPLIER = 0.2F;

  private final int price;
  private final int uses;
  private final int experience;

  public RumourMapOffer(final int price, final int uses, final int experience) {
    this.price = price;
    this.uses = uses;
    this.experience = experience;
  }

  @Override
  public @Nullable TradeOffer create(final Entity entity, final Random random) {
    if (!(entity.getWorld() instanceof ServerWorld world)
        || !world
            .getServer()
            .getSaveProperties()
            .getGeneratorOptions()
            .shouldGenerateStructures()) {
      return null;
    }
    final Optional<Pair<BlockPos, RegistryEntry<Structure>>> nearest =
        world
            .getRegistryManager()
            .get(RegistryKeys.STRUCTURE)
            .getEntryList(GuildRumour.VAMPIRE_HOUSES)
            .map(
                houses ->
                    world
                        .getChunkManager()
                        .getChunkGenerator()
                        .locateStructure(world, houses, entity.getBlockPos(), SEARCH_CHUNKS, true));
    return nearest
        .map(
            found ->
                offer(
                    world,
                    NearestStructures.entranceOfLocated(
                        world, found.getSecond(), found.getFirst())))
        .orElse(null);
  }

  private TradeOffer offer(final ServerWorld world, final BlockPos entrance) {
    return new TradeOffer(
        new TradedItem(Items.EMERALD, price),
        Optional.of(new TradedItem(Items.COMPASS)),
        ExplorationMaps.marking(world, entrance, SCALE, RumourMaps.NAME_KEY),
        uses,
        experience,
        PRICE_MULTIPLIER);
  }
}
