package com.grahambartley.nightborn.rank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record GuildRank(int order, Text name, RankMark mark, Optional<RankDeed> deed) {

  public static final Codec<GuildRank> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .fieldOf("order")
                          .forGetter(GuildRank::order),
                      TextCodecs.CODEC.fieldOf("name").forGetter(GuildRank::name),
                      RankMark.CODEC.fieldOf("mark").forGetter(GuildRank::mark),
                      RankDeed.CODEC.optionalFieldOf("deed").forGetter(GuildRank::deed))
                  .apply(instance, GuildRank::new));
}
