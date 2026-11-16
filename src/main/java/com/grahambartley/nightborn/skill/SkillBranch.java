package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SkillBranch(SkillTree tree, boolean bloodline, int order, boolean learnable) {

  public SkillBranch(final SkillTree tree, final boolean bloodline, final int order) {
    this(tree, bloodline, order, true);
  }

  public static final Codec<SkillBranch> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      SkillTree.CODEC.fieldOf("tree").forGetter(SkillBranch::tree),
                      Codec.BOOL
                          .optionalFieldOf("bloodline", false)
                          .forGetter(SkillBranch::bloodline),
                      Codec.intRange(0, 15)
                          .optionalFieldOf("order", 0)
                          .forGetter(SkillBranch::order),
                      Codec.BOOL
                          .optionalFieldOf("learnable", true)
                          .forGetter(SkillBranch::learnable))
                  .apply(instance, SkillBranch::new));
}
