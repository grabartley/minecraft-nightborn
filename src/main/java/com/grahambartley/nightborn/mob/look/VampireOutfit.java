package com.grahambartley.nightborn.mob.look;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record VampireOutfit(
    VampireRig rig, Identifier era, List<VampireOutfit.Piece> pieces, int weight) {

  public record Piece(Identifier item, Optional<Integer> color) {

    public static final Codec<Piece> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Identifier.CODEC.fieldOf("item").forGetter(Piece::item),
                        HexColor.CODEC.optionalFieldOf("color").forGetter(Piece::color))
                    .apply(instance, Piece::new));
  }

  public static final Codec<VampireOutfit> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      VampireRig.CODEC.fieldOf("rig").forGetter(VampireOutfit::rig),
                      Identifier.CODEC.fieldOf("era").forGetter(VampireOutfit::era),
                      Piece.CODEC.listOf().fieldOf("pieces").forGetter(VampireOutfit::pieces),
                      Codec.intRange(1, Weighted.MAX_WEIGHT)
                          .optionalFieldOf("weight", 1)
                          .forGetter(VampireOutfit::weight))
                  .apply(instance, VampireOutfit::new));

  public boolean fits(final VampireLook look) {
    return rig == look.rig() && look.eras().contains(era);
  }
}
