package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class TreeRevision {

  private record Stamped(TreeProgress progress, int revision) {}

  private TreeRevision() {}

  public static Codec<TreeProgress> codec(final SkillTree tree) {
    return RecordCodecBuilder.<Stamped>mapCodec(
            instance ->
                instance
                    .group(
                        TreeProgress.MAP_CODEC.forGetter(Stamped::progress),
                        Codec.INT.optionalFieldOf("revision", 0).forGetter(Stamped::revision))
                    .apply(instance, Stamped::new))
        .codec()
        .xmap(
            stamped -> upgrade(stamped.progress(), stamped.revision(), tree),
            progress -> new Stamped(progress, tree.revision()));
  }

  public static TreeProgress upgrade(
      final TreeProgress progress, final int revision, final SkillTree tree) {
    return revision < tree.revision() ? progress.refundAll() : progress;
  }
}
