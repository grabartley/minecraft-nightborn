package com.grahambartley.nightborn.guild.party;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

public record CampLayout(BlockPos campfire, List<CampLayout.Tent> tents) {

  public record Piece(BlockPos pos, BlockState state) {
    public static final Codec<Piece> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(Piece::pos),
                        BlockState.CODEC.fieldOf("state").forGetter(Piece::state))
                    .apply(instance, Piece::new));
  }

  public record Tent(BlockPos at, BlockRotation rotation, List<Piece> pieces) {
    public static final Codec<Tent> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        BlockPos.CODEC.fieldOf("at").forGetter(Tent::at),
                        BlockRotation.CODEC
                            .optionalFieldOf("rotation", BlockRotation.NONE)
                            .forGetter(Tent::rotation),
                        Piece.CODEC.listOf().fieldOf("pieces").forGetter(Tent::pieces))
                    .apply(instance, Tent::new));

    public List<Piece> placed() {
      return pieces.stream()
          .map(
              piece ->
                  new Piece(at.add(piece.pos().rotate(rotation)), piece.state().rotate(rotation)))
          .toList();
    }
  }

  public static final Codec<CampLayout> CODEC =
      RecordCodecBuilder.<CampLayout>create(
              instance ->
                  instance
                      .group(
                          BlockPos.CODEC.fieldOf("campfire").forGetter(CampLayout::campfire),
                          Tent.CODEC.listOf().fieldOf("tents").forGetter(CampLayout::tents))
                      .apply(instance, CampLayout::new))
          .validate(
              layout ->
                  CampGaps.problem(layout)
                      .<DataResult<CampLayout>>map(problem -> DataResult.error(() -> problem))
                      .orElseGet(() -> DataResult.success(layout)));

  public List<Piece> pieces() {
    return tents.stream().flatMap(tent -> tent.placed().stream()).toList();
  }

  public CampSite.Bounds bounds() {
    final List<BlockPos> all =
        Stream.concat(Stream.of(campfire), pieces().stream().map(Piece::pos)).toList();
    return new CampSite.Bounds(
        all.stream().mapToInt(BlockPos::getX).min().orElse(0),
        all.stream().mapToInt(BlockPos::getX).max().orElse(0),
        all.stream().mapToInt(BlockPos::getZ).min().orElse(0),
        all.stream().mapToInt(BlockPos::getZ).max().orElse(0),
        all.stream().mapToInt(BlockPos::getY).max().orElse(0) + 1);
  }
}
