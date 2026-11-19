package com.grahambartley.nightborn.mob.look;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record VampireLook(
    VampireRig rig,
    Identifier texture,
    List<Identifier> eras,
    int weight,
    Optional<VampireBuild> build) {

  public static final Codec<VampireLook> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      VampireRig.CODEC.fieldOf("rig").forGetter(VampireLook::rig),
                      Identifier.CODEC.fieldOf("texture").forGetter(VampireLook::texture),
                      Identifier.CODEC.listOf().fieldOf("eras").forGetter(VampireLook::eras),
                      Codec.intRange(1, Weighted.MAX_WEIGHT)
                          .optionalFieldOf("weight", 1)
                          .forGetter(VampireLook::weight),
                      VampireBuild.CODEC.optionalFieldOf("build").forGetter(VampireLook::build))
                  .apply(instance, VampireLook::new));
}
