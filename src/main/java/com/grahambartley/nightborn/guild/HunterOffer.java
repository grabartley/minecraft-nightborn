package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.rank.RankLadder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.util.Identifier;

public record HunterOffer(
    Identifier path, GuildOffer offer, Optional<Identifier> hunterRank, int pathSkills) {

  public static final int MOST_PATH_SKILLS = 64;

  public static final Codec<HunterOffer> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("path").forGetter(HunterOffer::path),
                      GuildOffer.MAP_CODEC.forGetter(HunterOffer::offer),
                      Identifier.CODEC
                          .optionalFieldOf("hunter_rank")
                          .forGetter(HunterOffer::hunterRank),
                      Codec.intRange(0, MOST_PATH_SKILLS)
                          .optionalFieldOf("path_skills", 0)
                          .forGetter(HunterOffer::pathSkills))
                  .apply(instance, HunterOffer::new));

  public static List<HunterOffer> forPaths(
      final Map<Identifier, HunterOffer> all,
      final List<Identifier> paths,
      final int sellerRank,
      final ToIntFunction<Identifier> skillsIn,
      final RankLadder ladder) {
    return all.entrySet().stream()
        .filter(entry -> paths.contains(entry.getValue().path()))
        .filter(
            entry ->
                entry
                    .getValue()
                    .opens(sellerRank, skillsIn.applyAsInt(entry.getValue().path()), ladder))
        .sorted(
            Comparator.comparingInt(
                    (Map.Entry<Identifier, HunterOffer> entry) ->
                        paths.indexOf(entry.getValue().path()))
                .thenComparingInt(entry -> entry.getValue().offer().order())
                .thenComparing(entry -> entry.getKey().toString()))
        .map(Map.Entry::getValue)
        .toList();
  }

  public boolean opens(final int sellerRank, final int skillsInPath, final RankLadder ladder) {
    return needs(ladder) <= sellerRank && pathSkills <= skillsInPath;
  }

  public int needs(final RankLadder ladder) {
    return hunterRank.map(id -> ladder.rankOf(id).orElse(GuildOffer.OUT_OF_REACH)).orElse(0);
  }
}
