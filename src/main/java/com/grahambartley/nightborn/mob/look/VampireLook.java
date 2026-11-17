package com.grahambartley.nightborn.mob.look;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.Identifier;

public record VampireLook(VampireRig rig, Identifier texture, List<Identifier> eras, int weight) {

  public static final int MAX_WEIGHT = Weighted.MAX_WEIGHT;

  public static final Codec<VampireLook> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      VampireRig.CODEC.fieldOf("rig").forGetter(VampireLook::rig),
                      Identifier.CODEC.fieldOf("texture").forGetter(VampireLook::texture),
                      Identifier.CODEC.listOf().fieldOf("eras").forGetter(VampireLook::eras),
                      Codec.intRange(1, MAX_WEIGHT)
                          .optionalFieldOf("weight", 1)
                          .forGetter(VampireLook::weight))
                  .apply(instance, VampireLook::new));
}
