package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SkillBranch(SkillTree tree, boolean line, int order, boolean learnable) {

  public SkillBranch(final SkillTree tree, final boolean line, final int order) {
    this(tree, line, order, true);
  }

  public static final Codec<SkillBranch> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      SkillTree.CODEC.fieldOf("tree").forGetter(SkillBranch::tree),
                      Codec.BOOL.optionalFieldOf("line", false).forGetter(SkillBranch::line),
                      Codec.intRange(0, 15)
                          .optionalFieldOf("order", 0)
                          .forGetter(SkillBranch::order),
                      Codec.BOOL
                          .optionalFieldOf("learnable", true)
                          .forGetter(SkillBranch::learnable))
                  .apply(instance, SkillBranch::new));
}
