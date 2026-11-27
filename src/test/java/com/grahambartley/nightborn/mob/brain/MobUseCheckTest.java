package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.Skill;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MobUseCheckTest {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn/nightborn");
  private static final Identifier ID = Identifier.of("nightborn", "test/power");
  private static final Set<Identifier> ALL_BUDGETS =
      Arrays.stream(Intent.values()).map(MobIntents::id).collect(Collectors.toSet());

  private static Skill skill(final String power) {
    return Skill.CODEC
        .parse(
            JsonOps.INSTANCE,
            JsonParser.parseString(
                "{\"branch\":\"nightborn:noble\",\"icon\":\"nightborn:x\",\"ranks\":[{\"cost\":1}]"
                    + power
                    + "}"))
        .getOrThrow();
  }

  private static String mob(final String uses) {
    return ",\"power\":{\"blood\":0,\"cooldown_ticks\":20,\"mob\":{\"max_range\":8,"
        + "\"tell_ticks\":5,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\""
        + uses
        + "}}";
  }

  @ParameterizedTest(name = "case {index}: unused [{1}], problem [{2}]")
  @CsvSource(
      delimiter = '|',
      value = {
        "'' | '' | ''",
        "',\"power\":{\"blood\":0,\"cooldown_ticks\":20}' | nightborn:test/power has a power that"
            + " mobs will never use | ''",
        "',\"power\":{\"blood\":0,\"cooldown_ticks\":20,\"mobs_never_use\":true}' | '' | ''",
        "',\"power\":{\"blood\":0,\"cooldown_ticks\":20,\"mobs_never_use\":true,\"mob\":{\"max_range\":8,\"tell_ticks\":5,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\"}}'"
            + " | '' | nightborn:test/power has a mob block but says mobs never use it",
        "',\"power\":{\"blood\":0,\"cooldown_ticks\":20,\"mob\":{\"max_range\":8,\"tell_ticks\":5,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\",\"uses\":[{\"intent\":\"flee\"}]}}'"
            + " | '' | nightborn:test/power has a use with unknown intent flee",
        "',\"power\":{\"blood\":0,\"cooldown_ticks\":20,\"mob\":{\"max_range\":8,\"tell_ticks\":5,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\",\"uses\":[{\"intent\":\"fight\",\"aim\":\"enemy\"}]}}'"
            + " | '' | nightborn:test/power has a use with unknown aim enemy",
        "',\"power\":{\"blood\":0,\"cooldown_ticks\":20,\"mob\":{\"max_range\":8,\"tell_ticks\":5,\"tell_sound\":\"a:b\",\"tell_clip\":\"x\",\"uses\":[{\"intent\":\"fight\",\"considerations\":[{\"fact\":\"moon_phase\",\"high\":1}]}]}}'"
            + " | '' | nightborn:test/power has a use with unknown fact moon_phase"
      })
  @DisplayName(
      "a power mobs never use is reported unless it opts out, and a bad use is reported with the"
          + " skill's name")
  void reportsEachSkill(final String power, final String unused, final String problem) {
    final MobUseCheck.Report report = MobUseCheck.check(Map.of(ID, skill(power)), ALL_BUDGETS);
    assertEquals(unused.isEmpty() ? List.of() : List.of(unused), report.unused());
    assertEquals(problem.isEmpty() ? List.of() : List.of(problem), report.problems());
  }

  @Test
  @DisplayName("an intent with no budget is reported")
  void missingBudget() {
    assertEquals(
        List.of("intent rest has no budget"),
        MobUseCheck.check(
                Map.of(ID, skill(mob(""))),
                ALL_BUDGETS.stream()
                    .filter(id -> !id.equals(MobIntents.id(Intent.REST)))
                    .collect(Collectors.toSet()))
            .problems());
  }

  @Test
  @DisplayName(
      "the shipped data has no bad uses, every intent has a budget, and only the powers still"
          + " waiting for mob use are reported")
  void shippedData() throws IOException {
    final Map<Identifier, Skill> skills = new HashMap<>();
    try (Stream<Path> files = Files.walk(DATA.resolve("skill"))) {
      files
          .filter(file -> file.toString().endsWith(".json"))
          .forEach(file -> skills.put(id(DATA.resolve("skill"), file), read(file)));
    }
    final Set<Identifier> budgets;
    try (Stream<Path> files = Files.list(DATA.resolve("mob_intent"))) {
      budgets = files.map(file -> id(DATA.resolve("mob_intent"), file)).collect(Collectors.toSet());
    }
    final MobUseCheck.Report report = MobUseCheck.check(skills, budgets);
    assertEquals(List.of(), report.problems());
    assertEquals(
        List.of(
            "nightborn:beast/hear_heartbeats has a power that mobs will never use",
            "nightborn:noble/glamour has a power that mobs will never use",
            "nightborn:slayer/riposte has a power that mobs will never use"),
        report.unused());
  }

  private static Identifier id(final Path root, final Path file) {
    final String path = root.relativize(file).toString().replace('\\', '/');
    return Identifier.of("nightborn", path.substring(0, path.length() - ".json".length()));
  }

  private static Skill read(final Path file) {
    try {
      return Skill.CODEC
          .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
          .getOrThrow();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
