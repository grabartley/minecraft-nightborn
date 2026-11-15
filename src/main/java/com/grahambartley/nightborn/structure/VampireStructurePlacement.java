package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.server.ServerConfigService;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;

public final class VampireStructurePlacement extends RandomSpreadStructurePlacement {

  private static final int LARGEST_SPREAD = 4096;

  public static final MapCodec<VampireStructurePlacement> CODEC =
      RecordCodecBuilder.<VampireStructurePlacement>mapCodec(
              instance ->
                  buildCodec(instance)
                      .and(
                          Codec.intRange(1, LARGEST_SPREAD)
                              .fieldOf("spacing")
                              .forGetter(VampireStructurePlacement::baseSpacing))
                      .and(
                          Codec.intRange(0, LARGEST_SPREAD)
                              .fieldOf("separation")
                              .forGetter(VampireStructurePlacement::baseSeparation))
                      .and(
                          SpreadType.CODEC
                              .optionalFieldOf("spread_type", SpreadType.LINEAR)
                              .forGetter(VampireStructurePlacement::getSpreadType))
                      .apply(instance, VampireStructurePlacement::new))
          .validate(VampireStructurePlacement::spacesWiderThanItSeparates);

  public VampireStructurePlacement(
      final Vec3i locateOffset,
      final FrequencyReductionMethod frequencyReductionMethod,
      final float frequency,
      final int salt,
      final Optional<ExclusionZone> exclusionZone,
      final int spacing,
      final int separation,
      final SpreadType spreadType) {
    super(
        locateOffset,
        frequencyReductionMethod,
        frequency,
        salt,
        exclusionZone,
        spacing,
        separation,
        spreadType);
  }

  private static DataResult<VampireStructurePlacement> spacesWiderThanItSeparates(
      final VampireStructurePlacement placement) {
    return placement.baseSpacing() > placement.baseSeparation()
        ? DataResult.success(placement)
        : DataResult.error(() -> "Spacing has to be larger than separation");
  }

  private int baseSpacing() {
    return super.getSpacing();
  }

  private int baseSeparation() {
    return super.getSeparation();
  }

  private static int rarity() {
    return ServerConfigService.current().structures().structureRarity();
  }

  @Override
  public int getSpacing() {
    return StructureSpread.spacing(baseSpacing(), rarity());
  }

  @Override
  public int getSeparation() {
    return StructureSpread.separation(getSpacing(), baseSeparation(), rarity());
  }

  @Override
  public ChunkPos getStartChunk(final long seed, final int chunkX, final int chunkZ) {
    final int spacing = getSpacing();
    final int regionX = Math.floorDiv(chunkX, spacing);
    final int regionZ = Math.floorDiv(chunkZ, spacing);
    final ChunkRandom random = new ChunkRandom(new CheckedRandom(0L));
    random.setRegionSeed(seed, regionX, regionZ, getSalt());
    final int span = spacing - getSeparation();
    return new ChunkPos(
        regionX * spacing + getSpreadType().get(random, span),
        regionZ * spacing + getSpreadType().get(random, span));
  }

  @Override
  public StructurePlacementType<?> getType() {
    return NightbornStructures.VAMPIRE_STRUCTURE_PLACEMENT;
  }
}
