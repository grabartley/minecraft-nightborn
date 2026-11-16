package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SkillTest {

  static final Path DATA = Path.of("src/main/resources/data/nightborn/nightborn");
  static final Path SPRITES = Path.of("src/main/resources/assets/nightborn/textures/gui/sprites");
  static final Path ENGLISH = Path.of("src/main/resources/assets/nightborn/lang/en_us.json");

  static Map<Identifier, Skill> shippedSkills() throws IOException {
    return load(DATA.resolve("skill"), Skill.CODEC);
  }

  static Map<Identifier, SkillBranch> shippedBranches() throws IOException {
    return load(DATA.resolve("skill_branch"), SkillBranch.CODEC);
  }

  static <T> Map<Identifier, T> load(final Path root, final Codec<T> codec) throws IOException {
    try (Stream<Path> files = Files.walk(root)) {
      return files
          .filter(path -> path.toString().endsWith(".json"))
          .collect(
              Collectors.toMap(
                  path -> {
                    final String relative = root.relativize(path).toString().replace('\\', '/');
                    return Identifier.of("nightborn", relative.substring(0, relative.length() - 5));
                  },
                  path -> {
                    try {
                      return codec
                          .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(path)))
                          .getOrThrow();
                    } catch (final IOException e) {
                      throw new IllegalStateException(e);
                    }
                  }));
    }
  }

  static Stream<Identifier> skillIds() throws IOException {
    return shippedSkills().keySet().stream().sorted();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("skillIds")
  @DisplayName("every shipped skill has its icon sprite and English name and description")
  void hasArtAndText(final Identifier id) throws IOException {
    final Skill skill = shippedSkills().get(id);
    assertTrue(
        Files.exists(SPRITES.resolve(skill.icon().getPath() + ".png")), "icon " + skill.icon());
    final JsonObject english =
        JsonParser.parseString(Files.readString(ENGLISH, StandardCharsets.UTF_8)).getAsJsonObject();
    assertTrue(english.has(SkillNames.skill(id)), SkillNames.skill(id));
    assertTrue(english.has(SkillNames.description(id)), SkillNames.description(id));
    assertTrue(english.has(SkillNames.branch(skill.branch())));
  }

  @Test
  @DisplayName("the shipped data passes the load-time check")
  void passesTheLoadCheck() throws IOException {
    assertEquals(List.of(), SkillDataCheck.problems(shippedSkills(), shippedBranches()));
  }

  @Test
  @DisplayName("every bloodline in the PRD ships as a bloodline branch with at least one skill")
  void bloodlinesShip() throws IOException {
    final Map<Identifier, SkillBranch> branches = shippedBranches();
    final Map<Identifier, Long> skillsPerBranch =
        shippedSkills().values().stream()
            .collect(Collectors.groupingBy(Skill::branch, Collectors.counting()));
    for (final String line : List.of("noble", "beast", "occult")) {
      final Identifier id = Identifier.of("nightborn", line);
      assertTrue(branches.get(id).bloodline(), line);
      assertTrue(skillsPerBranch.getOrDefault(id, 0L) > 0, line);
    }
    assertFalse(branches.get(Identifier.of("nightborn", "core")).bloodline());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"branch\":\"nightborn:core\",\"cost\":1,\"icon\":\"nightborn:x\",\"column\":3}",
        "{\"branch\":\"nightborn:core\",\"cost\":1,\"icon\":\"nightborn:x\",\"row\":6}",
        "{\"branch\":\"nightborn:core\",\"cost\":-1,\"icon\":\"nightborn:x\"}",
        "{\"cost\":1,\"icon\":\"nightborn:x\"}",
        "{\"branch\":\"nightborn:core\",\"cost\":1,\"icon\":\"nightborn:x\",\"bare_hand_damage\":-1}"
      })
  @DisplayName(
      "a skill off the screen grid, with a negative cost or bare hand bonus, or no branch is"
          + " rejected")
  void rejects(final String json) {
    assertFalse(Skill.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @Test
  @DisplayName("only a skill with a power is active")
  void active() throws IOException {
    final Map<Identifier, Boolean> active =
        shippedSkills().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().active()));
    assertTrue(active.get(Identifier.of("nightborn", "core/blood_rush")));
    assertFalse(active.get(Identifier.of("nightborn", "core/thick_blood")));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "beast/bat_form, bat form",
    "beast/beast_claws, bare hands",
    "beast/swift_prowl, speed",
    "beast/long_leap, leap"
  })
  @DisplayName("the Beast bloodline gives bat form, stronger bare hands, speed and a long leap")
  void beastGifts(final String path, final String gift) throws IOException {
    final Skill skill = shippedSkills().get(Identifier.of("nightborn", path));
    assertEquals(Identifier.of("nightborn", "beast"), skill.branch());
    final boolean gives =
        switch (gift) {
          case "bat form" -> skill.power().flatMap(SkillPower::batForm).isPresent();
          case "bare hands" -> skill.bareHandDamage() > 0.0;
          case "speed" ->
              skill.attributes().stream()
                  .anyMatch(a -> a.attribute().equals(EntityAttributes.GENERIC_MOVEMENT_SPEED));
          case "leap" -> skill.power().flatMap(SkillPower::leap).isPresent();
          default -> false;
        };
    assertTrue(gives, gift);
  }
}
