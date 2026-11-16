package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.ShippedClips;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillTree;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class VampireStagesTest {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn");
  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");

  @Test
  @DisplayName("the shipped stages read cleanly and pass the data check")
  void shippedDataIsClean() {
    assertEquals(
        List.of(),
        StageDataCheck.problems(
            stages(),
            SkillPicks.ofTree(
                read("nightborn/skill", Skill.CODEC),
                read("nightborn/skill_branch", SkillBranch.CODEC),
                SkillTree.VAMPIRE),
            read("nightborn/skill_branch", SkillBranch.CODEC)));
  }

  @Test
  @DisplayName("each older stage is rarer, tougher, keeps less blood back and is worth more")
  void olderIsHarder() {
    final List<VampireStage> byRank =
        stages().values().stream()
            .filter(stage -> stage.weight() > 0)
            .sorted((a, b) -> Integer.compare(a.rank(), b.rank()))
            .toList();
    assertEquals(4, byRank.size());
    for (int i = 1; i < byRank.size(); i++) {
      final VampireStage younger = byRank.get(i - 1);
      final VampireStage older = byRank.get(i);
      assertTrue(older.rank() > younger.rank());
      assertTrue(older.weight() < younger.weight());
      assertTrue(older.body().health() >= younger.body().health());
      assertTrue(older.skills().count().getMin() >= younger.skills().count().getMin());
      assertTrue(older.combat().bloodFloor() < younger.combat().bloodFloor());
      assertTrue(older.combat().thinkTicks() < younger.combat().thinkTicks());
    }
  }

  static Stream<VampireStage> wild() {
    return stages().values().stream().filter(stage -> stage.weight() > 0);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("wild")
  @DisplayName("the lord's stage is never rolled, outranks every other and is the toughest")
  void lordTopsEveryStage(final VampireStage stage) {
    final VampireStage lord = stages().get(Identifier.of("nightborn", "lord"));
    assertEquals(0, lord.weight());
    assertTrue(lord.rank() > stage.rank());
    assertTrue(lord.body().health() > stage.body().health());
    assertTrue(lord.body().damage() > stage.body().damage());
    assertTrue(lord.skills().count().getMin() > stage.skills().count().getMax());
    assertTrue(lord.combat().thinkTicks() < stage.combat().thinkTicks());
  }

  static Stream<VampireStage> shipped() {
    return stages().values().stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shipped")
  @DisplayName("every stage's loot table, marks and idle clip ship with the mod")
  void filesShip(final VampireStage stage) {
    stage.look().idle().ifPresent(idle -> assertTrue(ShippedClips.vampire().contains(idle), idle));
    stage
        .loot()
        .ifPresent(
            loot -> {
              final Path table =
                  DATA.resolve("loot_table").resolve(loot.getValue().getPath() + ".json");
              assertTrue(Files.exists(table), table.toString());
            });
    for (final String rig : List.of("male", "female")) {
      stage
          .look()
          .marksTexture(rig)
          .ifPresent(
              texture -> {
                final Path file = ASSETS.resolve(texture.getPath());
                assertTrue(Files.exists(file), file.toString());
              });
    }
  }

  private static Map<Identifier, VampireStage> stages() {
    return read("nightborn/vampire_stage", VampireStage.CODEC);
  }

  private static <T> Map<Identifier, T> read(final String folder, final Codec<T> codec) {
    final Path root = DATA.resolve(folder);
    final Map<Identifier, T> entries = new TreeMap<>();
    try (Stream<Path> files = Files.walk(root)) {
      for (final Path file : files.filter(f -> f.toString().endsWith(".json")).toList()) {
        final String id = root.relativize(file).toString().replace(".json", "");
        entries.put(
            Identifier.of("nightborn", id),
            codec
                .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
                .getOrThrow());
      }
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
    return entries;
  }
}
