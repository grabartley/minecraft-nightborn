package com.grahambartley.nightborn.guild.build;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.rank.GuildRank;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.rank.RankNeeds;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;

final class HunterBuildFixtures {

  static final Path DATA = Path.of("src/main/resources/data/nightborn/nightborn");
  static final RankNeeds NEEDS = new RankNeeds(List.of(5, 20, 45));
  static final Identifier CORE = id("hunter");
  static final Identifier TRAPPER = id("trapper");
  static final Identifier FAITHFUL = id("faithful");
  static final Identifier SLAYER = id("slayer");
  static final Identifier SEEKER = id("seeker");
  static final Identifier STAKE = id("stake");
  static final Identifier CROSSBOW = id("crossbow");

  private HunterBuildFixtures() {}

  static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  static Map<Identifier, Skill> skills() {
    return load(DATA.resolve("skill"), Skill.CODEC);
  }

  static Map<Identifier, SkillBranch> branches() {
    return load(DATA.resolve("skill_branch"), SkillBranch.CODEC);
  }

  static RankLadder ladder() {
    return RankLadder.of(load(DATA.resolve("guild_rank"), GuildRank.CODEC));
  }

  static HunterBuild build() {
    return load(DATA.resolve("hunter_build"), HunterBuild.CODEC).get(id("hunter"));
  }

  static HunterPurchases.Shop shop() {
    return shop(id -> 1);
  }

  static HunterPurchases.Shop shop(final java.util.function.ToIntFunction<Identifier> weight) {
    return new HunterPurchases.Shop(skills(), branches(), ladder(), skill -> true, weight);
  }

  static Set<Identifier> treeOf(final Set<Identifier> branchIds) {
    final Map<Identifier, Skill> skills = skills();
    return skills.keySet().stream()
        .filter(id -> branchIds.contains(skills.get(id).branch()))
        .collect(Collectors.toSet());
  }

  static Set<Identifier> branchesOwned(final TreeProgress progress) {
    final Map<Identifier, Skill> skills = skills();
    return progress.ownedInOrder().stream()
        .map(id -> skills.get(id).branch())
        .collect(Collectors.toSet());
  }

  static Set<Identifier> hunterBranches() {
    return branches().entrySet().stream()
        .filter(e -> e.getValue().tree() == SkillTree.HUNTER)
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  static DoubleSupplier rolls(final long seed) {
    final Random random = new Random(seed);
    return random::nextDouble;
  }

  private static <T> Map<Identifier, T> load(final Path root, final Codec<T> codec) {
    try (Stream<Path> files = Files.walk(root)) {
      return files
          .filter(path -> path.toString().endsWith(".json"))
          .collect(
              Collectors.toMap(
                  path -> {
                    final String relative = root.relativize(path).toString().replace('\\', '/');
                    return id(relative.substring(0, relative.length() - 5));
                  },
                  path -> {
                    try {
                      return codec
                          .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(path)))
                          .getOrThrow();
                    } catch (final IOException e) {
                      throw new UncheckedIOException(e);
                    }
                  }));
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
