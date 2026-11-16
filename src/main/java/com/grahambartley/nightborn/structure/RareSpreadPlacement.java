package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.server.ServerConfigService;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;

public final class RareSpreadPlacement extends RandomSpreadStructurePlacement {

  private static final int LARGEST_SPREAD = 4096;
  private static final int LOCATE_OFFSET_REACH = 16;

  private final StructureRarity rarity;
  private final Optional<ExclusionZone> near;

  public static final MapCodec<RareSpreadPlacement> CODEC =
      RecordCodecBuilder.<RareSpreadPlacement>mapCodec(
              instance ->
                  instance
                      .group(
                          Vec3i.createOffsetCodec(LOCATE_OFFSET_REACH)
                              .optionalFieldOf("locate_offset", Vec3i.ZERO)
                              .forGetter(RareSpreadPlacement::getLocateOffset),
                          FrequencyReductionMethod.CODEC
                              .optionalFieldOf(
                                  "frequency_reduction_method", FrequencyReductionMethod.DEFAULT)
                              .forGetter(RareSpreadPlacement::getFrequencyReductionMethod),
                          Codec.floatRange(0.0F, 1.0F)
                              .optionalFieldOf("frequency", 1.0F)
                              .forGetter(RareSpreadPlacement::getFrequency),
                          Codecs.NONNEGATIVE_INT
                              .fieldOf("salt")
                              .forGetter(RareSpreadPlacement::getSalt),
                          ExclusionZone.CODEC
                              .optionalFieldOf("exclusion_zone")
                              .forGetter(RareSpreadPlacement::getExclusionZone),
                          Codec.intRange(1, LARGEST_SPREAD)
                              .fieldOf("spacing")
                              .forGetter(RareSpreadPlacement::baseSpacing),
                          Codec.intRange(0, LARGEST_SPREAD)
                              .fieldOf("separation")
                              .forGetter(RareSpreadPlacement::baseSeparation),
                          SpreadType.CODEC
                              .optionalFieldOf("spread_type", SpreadType.LINEAR)
                              .forGetter(RareSpreadPlacement::getSpreadType),
                          StructureRarity.CODEC
                              .fieldOf("rarity")
                              .forGetter(RareSpreadPlacement::rarity),
                          ExclusionZone.CODEC
                              .optionalFieldOf("near")
                              .forGetter(RareSpreadPlacement::near))
                      .apply(instance, RareSpreadPlacement::new))
          .validate(RareSpreadPlacement::spacesWiderThanItSeparates);

  public RareSpreadPlacement(
      final Vec3i locateOffset,
      final FrequencyReductionMethod frequencyReductionMethod,
      final float frequency,
      final int salt,
      final Optional<ExclusionZone> exclusionZone,
      final int spacing,
      final int separation,
      final SpreadType spreadType,
      final StructureRarity rarity,
      final Optional<ExclusionZone> near) {
    super(
        locateOffset,
        frequencyReductionMethod,
        frequency,
        salt,
        exclusionZone,
        spacing,
        separation,
        spreadType);
    this.rarity = rarity;
    this.near = near;
  }

  public Optional<ExclusionZone> near() {
    return near;
  }

  @Override
  protected boolean isStartChunk(
      final StructurePlacementCalculator calculator, final int chunkX, final int chunkZ) {
    return super.isStartChunk(calculator, chunkX, chunkZ)
        && near.map(
                zone -> calculator.canGenerate(zone.otherSet(), chunkX, chunkZ, zone.chunkCount()))
            .orElse(true);
  }

  public StructureRarity rarity() {
    return rarity;
  }

  private static DataResult<RareSpreadPlacement> spacesWiderThanItSeparates(
      final RareSpreadPlacement placement) {
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

  private int rarityPercent() {
    return rarity.percent(ServerConfigService.current().structures());
  }

  @Override
  public int getSpacing() {
    return StructureSpread.spacing(baseSpacing(), rarityPercent());
  }

  @Override
  public int getSeparation() {
    return StructureSpread.separation(getSpacing(), baseSeparation(), rarityPercent());
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
    return NightbornStructures.RARE_SPREAD;
  }
}
