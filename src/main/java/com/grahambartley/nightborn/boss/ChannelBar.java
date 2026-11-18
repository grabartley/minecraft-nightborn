package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.boss.BossBar;

public record ChannelBar(BossBar.Color color, String key) {

  public static final Codec<ChannelBar> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      BossPhase.COLOR.fieldOf("color").forGetter(ChannelBar::color),
                      Codec.STRING.fieldOf("key").forGetter(ChannelBar::key))
                  .apply(instance, ChannelBar::new));
}
