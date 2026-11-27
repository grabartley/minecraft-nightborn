package com.grahambartley.nightborn.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.structure.Structure;

public record Siting(Optional<Footing> footing, Optional<Summit> summit, Optional<CaveMouth> cave) {

  public static final MapCodec<Siting> MAP_CODEC =
      RecordCodecBuilder.<Siting>mapCodec(
              instance ->
                  instance
                      .group(
                          Footing.CODEC.optionalFieldOf("footing").forGetter(Siting::footing),
                          Summit.CODEC.optionalFieldOf("summit").forGetter(Siting::summit),
                          CaveMouth.CODEC.optionalFieldOf("cave").forGetter(Siting::cave))
                      .apply(instance, Siting::new))
          .validate(Siting::oneGround);

  private static DataResult<Siting> oneGround(final Siting siting) {
    return siting.cave.isPresent() && (siting.footing.isPresent() || siting.summit.isPresent())
        ? DataResult.error(() -> "A structure that opens onto a cave has no footing or summit")
        : DataResult.success(siting);
  }

  public int reach() {
    return summit.map(Summit::reach).orElse(0)
        + cave.map(mouth -> mouth.reach() + mouth.path()).orElse(0);
  }

  public Optional<Identifier> entrance() {
    return cave.map(CaveMouth::entrance).or(() -> footing.flatMap(Footing::entrance));
  }

  public BlockPos standingSpot(final Threshold way) {
    return (cave.isPresent() ? way.step() : way.floor()).up();
  }

  public Optional<ColumnPos> ground(final Structure.Context context, final ColumnPos corner) {
    return summit.isPresent() ? summit.get().peak(context, corner) : Optional.of(corner);
  }

  public Optional<Structure.StructurePosition> settle(
      final Structure.Context context,
      final Structure.StructurePosition position,
      final ColumnPos ground) {
    if (footing.isEmpty() && cave.isEmpty()) {
      return Optional.of(position);
    }
    final StructurePiecesCollector pieces = position.generate();
    final Optional<Threshold> threshold =
        entrance()
            .flatMap(
                name ->
                    Threshold.in(
                        pieces.toList().pieces(),
                        name,
                        context.structureTemplateManager(),
                        context.random()));
    if (threshold.isEmpty() && entrance().isPresent()) {
      return Optional.empty();
    }
    final Optional<CaveMouth.Opening> opening =
        cave.flatMap(
            mouth ->
                mouth.meet(
                    threshold.get(),
                    pieces.getBoundingBox(),
                    caveGround(context, mouth.band(threshold.get()))));
    final Optional<BlockPos> move =
        cave.isPresent()
            ? opening.map(CaveMouth.Opening::move)
            : Optional.of(onFooting(context, threshold, ground));
    if (move.isEmpty() || footing.isPresent() && !footing.get().allowsLift(move.get().getY())) {
      return Optional.empty();
    }
    final BlockPos by = move.get();
    pieces.toList().pieces().forEach(piece -> piece.translate(by.getX(), by.getY(), by.getZ()));
    opening.ifPresent(
        found ->
            pieces.addPiece(
                new CavePath(
                    cave.get()
                        .room()
                        .carved(
                            found.route(),
                            threshold.get().out().rotateYClockwise(),
                            pieces.getBoundingBox()),
                    cave.get().sill(),
                    cave.get().room())));
    final BlockPos placed = position.position().add(by);
    if (!inBiome(context, placed)) {
      return Optional.empty();
    }
    final ColumnPos approach =
        threshold
            .map(Threshold::outside)
            .map(out -> new ColumnPos(out.x() + by.getX(), out.z() + by.getZ()))
            .orElse(new ColumnPos(placed.getX(), placed.getZ()));
    return footing.isEmpty() || footing.get().fits(context, pieces.getBoundingBox(), approach)
        ? Optional.of(new Structure.StructurePosition(placed, Either.right(pieces)))
        : Optional.empty();
  }

  private static CaveMouth.Ground caveGround(
      final Structure.Context context, final DepthBand band) {
    return context.chunkGenerator() instanceof NoiseChunkGenerator noise
        ? new NoiseGround(context, noise, band)
        : new ColumnGround(context);
  }

  private BlockPos onFooting(
      final Structure.Context context,
      final Optional<Threshold> threshold,
      final ColumnPos ground) {
    return threshold
        .map(
            way -> {
              final ColumnPos meets = summit.isPresent() ? ground : way.outside();
              return way.moveOnto(meets, Footing.surface(context, meets));
            })
        .orElse(BlockPos.ORIGIN);
  }

  private static boolean inBiome(final Structure.Context context, final BlockPos at) {
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
}
