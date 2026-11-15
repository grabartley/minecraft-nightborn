package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record SkillProgress(TreeProgress vampire, TreeProgress hunter) {

  public static final SkillProgress EMPTY =
      new SkillProgress(TreeProgress.EMPTY, TreeProgress.EMPTY);

  public static final Codec<SkillProgress> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      TreeProgress.CODEC
                          .optionalFieldOf("vampire", TreeProgress.EMPTY)
                          .forGetter(SkillProgress::vampire),
                      TreeProgress.CODEC
                          .optionalFieldOf("hunter", TreeProgress.EMPTY)
                          .forGetter(SkillProgress::hunter))
                  .apply(instance, SkillProgress::new));

  public static final PacketCodec<ByteBuf, SkillProgress> PACKET_CODEC = PacketCodecs.codec(CODEC);

  public TreeProgress get(final SkillTree tree) {
    return tree == SkillTree.VAMPIRE ? vampire : hunter;
  }

  public SkillProgress with(final SkillTree tree, final TreeProgress progress) {
    return tree == SkillTree.VAMPIRE
        ? new SkillProgress(progress, hunter)
        : new SkillProgress(vampire, progress);
  }

  public boolean isEmpty() {
    return equals(EMPTY);
  }
}
