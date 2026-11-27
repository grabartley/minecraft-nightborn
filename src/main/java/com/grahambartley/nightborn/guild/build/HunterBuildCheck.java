package com.grahambartley.nightborn.guild.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.minecraft.util.Identifier;

public final class HunterBuildCheck {

  private HunterBuildCheck() {}

  public static List<String> problems(
      final HunterBuild build,
      final Set<Identifier> ranks,
      final Set<Identifier> skills,
      final Set<Identifier> kits) {
    final List<String> problems = new ArrayList<>();
    if (build.rankWeights().values().stream().allMatch(weight -> weight <= 0)) {
      problems.add("no rank has a weight above zero");
    }
    unknown(build.rankWeights().keySet(), ranks, "rank weight names", "rank", problems);
    unknown(build.thinkTicks().keySet(), ranks, "think ticks name", "rank", problems);
    unknown(build.kitLeanings().keySet(), kits, "kit leanings name", "kit", problems);
    new TreeMap<>(build.kitLeanings())
        .forEach(
            (kit, leanings) ->
                unknown(leanings.keySet(), skills, kit + " leans toward", "skill", problems));
    return problems;
  }

  public static List<String> unknownRanks(
      final String owner, final Set<Identifier> named, final Set<Identifier> ranks) {
    final List<String> problems = new ArrayList<>();
    unknown(named, ranks, owner + " names rank", "rank", problems);
    return problems;
  }

  private static void unknown(
      final Set<Identifier> named,
      final Set<Identifier> known,
      final String where,
      final String kind,
      final List<String> problems) {
    new TreeSet<>(named)
        .stream()
            .filter(id -> !known.contains(id))
            .forEach(id -> problems.add(where + " " + id + ", which is not a " + kind));
  }
}
