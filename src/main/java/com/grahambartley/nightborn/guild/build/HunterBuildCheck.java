package com.grahambartley.nightborn.guild.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;

public final class HunterBuildCheck {

  private HunterBuildCheck() {}

  public static List<String> problems(
      final HunterBuild build,
      final Set<Identifier> ranks,
      final Set<Identifier> skills,
      final Set<Identifier> paths,
      final Set<Identifier> kits) {
    final List<String> problems = new ArrayList<>();
    if (build.rankWeights().values().stream().allMatch(weight -> weight <= 0)) {
      problems.add("no rank has a weight above zero");
    }
    if (build.coreWeight() <= 0 && build.pathWeight() <= 0) {
      problems.add(
          "the core and path weights are both zero, so only leaned-toward skills are bought");
    }
    unknown(build.rankWeights().keySet(), ranks, "rank weight names", "rank", problems);
    unknown(build.thinkTicks().keySet(), ranks, "think ticks name", "rank", problems);
    unknown(build.sharpness().keySet(), ranks, "sharpness name", "rank", problems);
    unknown(build.kitLeanings().keySet(), kits, "kit leanings name", "kit", problems);
    unknown(build.kitPaths().keySet(), kits, "kit paths name", "kit", problems);
    unknown(build.kitDispositions().keySet(), kits, "kit dispositions name", "kit", problems);
    if (build.masterKits().values().stream().allMatch(weight -> weight <= 0)) {
      problems.add("no Guild Master kit has a weight above zero, so Guild Masters fight unarmed");
    }
    unknown(build.masterKits().keySet(), kits, "master kits name", "kit", problems);
    new TreeMap<>(build.kitLeanings())
        .forEach(
            (kit, leanings) ->
                unknown(leanings.keySet(), skills, kit + " leans toward", "skill", problems));
    new TreeMap<>(build.kitPaths()).forEach((kit, slots) -> slots(kit, slots, paths, problems));
    return problems;
  }

  public static List<String> unknownRanks(
      final String owner, final Set<Identifier> named, final Set<Identifier> ranks) {
    final List<String> problems = new ArrayList<>();
    unknown(named, ranks, owner + " names rank", "rank", problems);
    return problems;
  }

  private static void slots(
      final Identifier kit,
      final List<Map<Identifier, Integer>> slots,
      final Set<Identifier> paths,
      final List<String> problems) {
    final Set<Identifier> certain = new TreeSet<>();
    for (int slot = 0; slot < slots.size(); slot++) {
      final Map<Identifier, Integer> leanings = slots.get(slot);
      final String where = kit + " path " + (slot + 1);
      unknown(leanings.keySet(), paths, where + " names", "path", problems);
      final Set<Identifier> open =
          leanings.entrySet().stream()
              .filter(entry -> entry.getValue() > 0)
              .map(Map.Entry::getKey)
              .collect(Collectors.toCollection(TreeSet::new));
      if (open.isEmpty()) {
        problems.add(where + " has no path with a weight above zero, so it is never filled");
      } else if (certain.containsAll(open)) {
        problems.add(
            where + " weighs only paths an earlier slot always takes, so it is never filled");
      }
      if (open.size() == 1) {
        certain.addAll(open);
      }
    }
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
