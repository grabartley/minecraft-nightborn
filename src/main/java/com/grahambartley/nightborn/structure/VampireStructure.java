package com.grahambartley.nightborn.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.alias.StructurePoolAliasBinding;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.DimensionPadding;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public final class VampireStructure extends Structure {

  public static final MapCodec<VampireStructure> CODEC =
      RecordCodecBuilder.<VampireStructure>mapCodec(
              instance ->
                  instance
                      .group(
                          configCodecBuilder(instance),
                          StructurePool.REGISTRY_CODEC
                              .fieldOf("start_pool")
                              .forGetter(structure -> structure.startPool),
                          Identifier.CODEC
                              .optionalFieldOf("start_jigsaw_name")
                              .forGetter(structure -> structure.startJigsawName),
                          Codec.intRange(1, JigsawStructure.MAX_GENERATION_DEPTH)
                              .fieldOf("size")
                              .forGetter(structure -> structure.size),
                          HeightProvider.CODEC
                              .fieldOf("start_height")
                              .forGetter(structure -> structure.startHeight),
                          Codec.BOOL
                              .optionalFieldOf("use_expansion_hack", false)
                              .forGetter(structure -> structure.useExpansionHack),
                          Heightmap.Type.CODEC
                              .optionalFieldOf("project_start_to_heightmap")
                              .forGetter(structure -> structure.projectStartToHeightmap),
                          Codec.intRange(1, JigsawStructure.MAX_SIZE)
                              .fieldOf("max_distance_from_center")
                              .forGetter(structure -> structure.maxDistanceFromCenter),
                          Codec.list(StructurePoolAliasBinding.CODEC)
                              .optionalFieldOf("pool_aliases", List.of())
                              .forGetter(structure -> structure.poolAliases),
                          DimensionPadding.CODEC
                              .optionalFieldOf(
                                  "dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING)
                              .forGetter(structure -> structure.dimensionPadding),
                          StructureLiquidSettings.codec
                              .optionalFieldOf(
                                  "liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS)
                              .forGetter(structure -> structure.liquidSettings),
                          Household.CODEC
                              .fieldOf("household")
                              .forGetter(structure -> structure.household),
                          Codec.BOOL
                              .optionalFieldOf("spawns_vampire_bats", true)
                              .forGetter(VampireStructure::spawnsVampireBats),
                          StructureAmbience.CODEC
                              .optionalFieldOf("ambience", StructureAmbience.SILENT)
                              .forGetter(VampireStructure::ambience),
                          Footing.CODEC
                              .optionalFieldOf("footing")
                              .forGetter(structure -> structure.footing),
                          Summit.CODEC
                              .optionalFieldOf("summit")
                              .forGetter(structure -> structure.summit))
                      .apply(instance, VampireStructure::new))
          .validate(VampireStructure::staysInReach);

  private final RegistryEntry<StructurePool> startPool;
  private final Optional<Identifier> startJigsawName;
  private final int size;
  private final HeightProvider startHeight;
  private final boolean useExpansionHack;
  private final Optional<Heightmap.Type> projectStartToHeightmap;
  private final int maxDistanceFromCenter;
  private final List<StructurePoolAliasBinding> poolAliases;
  private final DimensionPadding dimensionPadding;
  private final StructureLiquidSettings liquidSettings;
  private final Household household;
  private final boolean spawnsVampireBats;
  private final StructureAmbience ambience;
  private final Optional<Footing> footing;
  private final Optional<Summit> summit;

  public VampireStructure(
      final Config config,
      final RegistryEntry<StructurePool> startPool,
      final Optional<Identifier> startJigsawName,
      final int size,
      final HeightProvider startHeight,
      final boolean useExpansionHack,
      final Optional<Heightmap.Type> projectStartToHeightmap,
      final int maxDistanceFromCenter,
      final List<StructurePoolAliasBinding> poolAliases,
      final DimensionPadding dimensionPadding,
      final StructureLiquidSettings liquidSettings,
      final Household household,
      final boolean spawnsVampireBats,
      final StructureAmbience ambience,
      final Optional<Footing> footing,
      final Optional<Summit> summit) {
    super(config);
    this.startPool = startPool;
    this.startJigsawName = startJigsawName;
    this.size = size;
    this.startHeight = startHeight;
    this.useExpansionHack = useExpansionHack;
    this.projectStartToHeightmap = projectStartToHeightmap;
    this.maxDistanceFromCenter = maxDistanceFromCenter;
    this.poolAliases = poolAliases;
    this.dimensionPadding = dimensionPadding;
    this.liquidSettings = liquidSettings;
    this.household = household;
    this.spawnsVampireBats = spawnsVampireBats;
    this.ambience = ambience;
    this.footing = footing;
    this.summit = summit;
  }

  public boolean spawnsVampireBats() {
    return spawnsVampireBats;
  }

  public StructureAmbience ambience() {
    return ambience;
  }

  public Optional<Footing> footing() {
    return footing;
  }

  public Optional<Summit> summit() {
    return summit;
  }

  public Household household() {
    return household;
  }

  private static DataResult<VampireStructure> staysInReach(final VampireStructure structure) {
    return StructureReach.fits(
            structure.maxDistanceFromCenter,
            structure.getTerrainAdaptation(),
            structure.summit.map(Summit::reach).orElse(0))
        ? DataResult.success(structure)
        : DataResult.error(
            () ->
                "A vampire structure's reach including terrain adaptation and its summit search"
                    + " must not exceed "
                    + JigsawStructure.MAX_SIZE);
  }

  @Override
  protected Optional<StructurePosition> getStructurePosition(final Context context) {
    final ChunkPos chunk = context.chunkPos();
    final int y =
        startHeight.get(
            context.random(), new HeightContext(context.chunkGenerator(), context.world()));
    final ColumnPos corner = new ColumnPos(chunk.getStartX(), chunk.getStartZ());
    final Optional<ColumnPos> ground =
        summit.isPresent() ? summit.get().peak(context, corner) : Optional.of(corner);
    if (ground.isEmpty()) {
      return Optional.empty();
    }
    final BlockPos start = new BlockPos(ground.get().x(), y, ground.get().z());
    final Optional<StructurePosition> position =
        StructurePoolBasedGenerator.generate(
            context,
            startPool,
            startJigsawName,
            size,
            start,
            useExpansionHack,
            projectStartToHeightmap,
            maxDistanceFromCenter,
            StructurePoolAliasLookup.create(poolAliases, start, context.seed()),
            dimensionPadding,
            liquidSettings);
    if (footing.isEmpty() || position.isEmpty()) {
      return position;
    }
    final StructurePiecesCollector pieces = position.get().generate();
    final Optional<Threshold> threshold =
        footing
            .get()
            .entrance()
            .flatMap(
                name ->
                    Threshold.in(
                        pieces.toList().pieces(),
                        name,
                        context.structureTemplateManager(),
                        context.random()));
    final BlockPos move =
        threshold
            .map(
                way -> {
                  final ColumnPos meets = summit.isPresent() ? ground.get() : way.outside();
                  return way.moveOnto(meets, Footing.surface(context, meets));
                })
            .orElse(BlockPos.ORIGIN);
    if (threshold.isEmpty() && footing.get().entrance().isPresent()
        || !footing.get().allowsLift(move.getY())) {
      return Optional.empty();
    }
    pieces
        .toList()
        .pieces()
        .forEach(piece -> piece.translate(move.getX(), move.getY(), move.getZ()));
    final BlockPos placed = position.get().position().add(move);
    if (!inBiome(context, placed)) {
      return Optional.empty();
    }
    final ColumnPos approach =
        threshold
            .map(way -> way.outside())
            .map(out -> new ColumnPos(out.x() + move.getX(), out.z() + move.getZ()))
            .orElse(new ColumnPos(placed.getX(), placed.getZ()));
    return footing.get().fits(context, pieces.getBoundingBox(), approach)
        ? Optional.of(new StructurePosition(placed, Either.right(pieces)))
        : Optional.empty();
  }

  private static boolean inBiome(final Context context, final BlockPos at) {
    return context
        .biomePredicate()
        .test(
            context
                .chunkGenerator()
                .getBiomeSource()
                .getBiome(
                    BiomeCoords.fromBlock(at.getX()),
                    BiomeCoords.fromBlock(at.getY()),
                    BiomeCoords.fromBlock(at.getZ()),
                    context.noiseConfig().getMultiNoiseSampler()));
  }

  @Override
  public void postPlace(
      final StructureWorldAccess world,
      final StructureAccessor structures,
      final ChunkGenerator generator,
      final Random random,
      final BlockBox chunkBox,
      final ChunkPos chunk,
      final StructurePiecesList pieces) {
    StructureHeart.of(pieces.pieces())
        .ifPresent(heart -> HouseholdSpawner.settle(world, chunkBox, heart, household, random));
  }

  @Override
  public StructureType<?> getType() {
    return NightbornStructures.VAMPIRE_STRUCTURE;
  }
}
