package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
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
      assertTrue(branches.get(id).line(), line);
      assertTrue(skillsPerBranch.getOrDefault(id, 0L) > 0, line);
    }
    assertFalse(branches.get(Identifier.of("nightborn", "core")).line());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "trapper, ''",
    "faithful, hunter/faith",
    "slayer, hunter/bite_ward",
    "seeker, hunter/iron_will"
  })
  @DisplayName("the hunter tree ships four paths, and each skill it had moves into one")
  void huntingPathsShip(final String path, final String moved) throws IOException {
    final Identifier id = Identifier.of("nightborn", path);
    final SkillBranch branch = shippedBranches().get(id);
    assertEquals(SkillTree.HUNTER, branch.tree());
    assertTrue(branch.line(), path);
    if (!moved.isEmpty()) {
      assertEquals(id, shippedSkills().get(Identifier.of("nightborn", moved)).branch());
    }
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"hunter/bite_ward, 0.8", "hunter/iron_will, 0.8", "hunter/faith, 3.0"})
  @DisplayName("a hunter's resistances reach 0.8 at the last rank, and faith one point a rank")
  void hunterRanksAddUp(final String skill, final double total) throws IOException {
    final Skill shipped = shippedSkills().get(Identifier.of("nightborn", skill));
    assertEquals(3, shipped.maxRank());
    assertEquals(
        total,
        shipped.ranks().stream()
            .flatMap(rank -> rank.attributes().stream())
            .mapToDouble(SkillAttribute::amount)
            .sum(),
        1.0E-9);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"branch\":\"nightborn:core\",\"ranks\":[{\"cost\":1}],\"icon\":\"nightborn:x\",\"column\":3}",
        "{\"branch\":\"nightborn:core\",\"ranks\":[{\"cost\":1}],\"icon\":\"nightborn:x\",\"row\":7}",
        "{\"branch\":\"nightborn:core\",\"ranks\":[{\"cost\":-1}],\"icon\":\"nightborn:x\"}",
        "{\"ranks\":[{\"cost\":1}],\"icon\":\"nightborn:x\"}",
        "{\"branch\":\"nightborn:core\",\"ranks\":[],\"icon\":\"nightborn:x\"}",
        "{\"branch\":\"nightborn:core\",\"icon\":\"nightborn:x\"}",
        "{\"branch\":\"nightborn:core\",\"ranks\":[{\"cost\":1},{\"cost\":1},{\"cost\":1},{\"cost\":1}],\"icon\":\"nightborn:x\"}",
        "{\"branch\":\"nightborn:core\",\"ranks\":[{\"cost\":1},{\"cost\":1}],\"icon\":\"nightborn:x\",\"power\":{\"blood\":1,\"cooldown_ticks\":20}}"
      })
  @DisplayName(
      "a skill off the screen grid, with no branch, a bad cost, no ranks, more than three ranks,"
          + " or a power with more than one rank is rejected")
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

  @ParameterizedTest(name = "{0}: costs {1}, needs {2}, gate {3}")
  @CsvSource(
      delimiter = '|',
      value = {
        "thick_blood | 1,2,3 | | ",
        "grave_strength | 2,2,3 | thick_blood | ",
        "vanish | 2 | thick_blood | ",
        "blood_rush | 2 | thick_blood | ",
        "night_eyes | 1,1,2 | thick_blood | ",
        "blood_healing | 2 | thick_blood | ",
        "iron_grip | 1,2,2 | grave_strength | ",
        "deep_veins | 2,3,3 | blood_healing | adult",
        "unbroken | 3 | iron_grip | elder",
        "sanguine_mastery | 3 | deep_veins | elder"
      })
  @DisplayName("the core holds the ten locked skills with their rank costs, needs and stage gates")
  void coreSkills(final String path, final String costs, final String needs, final String gate)
      throws IOException {
    final Skill skill = shippedSkills().get(Identifier.of("nightborn", "core/" + path));
    assertEquals(
        costs,
        skill.ranks().stream()
            .map(rank -> Integer.toString(rank.cost()))
            .collect(Collectors.joining(",")));
    assertEquals(
        needs == null ? List.of() : List.of(Identifier.of("nightborn", "core/" + needs)),
        skill.requires());
    assertEquals(
        Optional.ofNullable(gate).map(stage -> Identifier.of("nightborn", stage)), skill.stage());
  }

  @ParameterizedTest(name = "{0}: costs {1}, needs {2}, gate {3}, power {4}")
  @CsvSource(
      delimiter = '|',
      value = {
        "field_craft | 1,1,2 | hunter/tracking | | false",
        "snare | 2 | trapper/field_craft | | true",
        "bait | 2 | trapper/field_craft | | true",
        "net_shot | 2 | trapper/snare | | true",
        "quick_hands | 1,2,2 | trapper/snare | | false",
        "trap_sense | 2 | trapper/snare | | false",
        "silver_edge | 2,2,3 | trapper/net_shot | hunter | false",
        "flash_trap | 3 | trapper/snare | hunter | true",
        "killing_ground | 4 | trapper/flash_trap,trapper/net_shot | master | true",
        "master_trapper | 3 | trapper/killing_ground | master | false"
      })
  @DisplayName("the Trapper path holds its ten locked skills with their costs, needs and ranks")
  void trapperSkills(
      final String path,
      final String costs,
      final String needs,
      final String gate,
      final boolean power)
      throws IOException {
    final Skill skill = shippedSkills().get(Identifier.of("nightborn", "trapper/" + path));
    assertEquals(Identifier.of("nightborn", "trapper"), skill.branch());
    assertEquals(
        costs,
        skill.ranks().stream()
            .map(rank -> Integer.toString(rank.cost()))
            .collect(Collectors.joining(",")));
    assertEquals(
        List.of(needs.split(",")).stream().map(need -> Identifier.of("nightborn", need)).toList(),
        skill.requires());
    assertEquals(
        Optional.ofNullable(gate).map(rank -> Identifier.of("nightborn", rank)), skill.rank());
    assertEquals(power, skill.active());
    assertEquals(0, skill.power().map(SkillPower::blood).orElse(0));
  }

  @ParameterizedTest(name = "{0}: cooldown {1} seconds")
  @CsvSource({"snare, 20", "bait, 60", "net_shot, 25", "flash_trap, 60", "killing_ground, 300"})
  @DisplayName("each Trapper power cools down for the time the table gives")
  void trapperCooldowns(final String path, final int seconds) throws IOException {
    assertEquals(
        seconds * 20,
        shippedSkills()
            .get(Identifier.of("nightborn", "trapper/" + path))
            .power()
            .orElseThrow()
            .cooldownTicks());
  }

  @Test
  @DisplayName("the core holds exactly ten skills")
  void coreSize() throws IOException {
    assertEquals(
        10L,
        shippedSkills().values().stream()
            .filter(skill -> skill.branch().equals(Identifier.of("nightborn", "core")))
            .count());
  }

  @ParameterizedTest(name = "{0} ranks owned: next cost {1}, {2} held")
  @CsvSource({"0, 1, 0", "1, 2, 1", "2, 3, 2", "3, 3, 3", "9, 3, 3", "-1, 1, 0"})
  @DisplayName("each rank is bought in turn at its own cost")
  void ranksInTurn(final int owned, final int cost, final int held) throws IOException {
    final Skill skill = shippedSkills().get(Identifier.of("nightborn", "core/thick_blood"));
    assertEquals(cost, skill.nextCost(owned));
    assertEquals(held, skill.held(owned).size());
    assertEquals(3, skill.maxRank());
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
          case "bare hands" -> skill.ranks().stream().anyMatch(rank -> rank.bareHandDamage() > 0.0);
          case "speed" ->
              skill.ranks().stream()
                  .flatMap(rank -> rank.attributes().stream())
                  .anyMatch(a -> a.attribute().equals(EntityAttributes.GENERIC_MOVEMENT_SPEED));
          case "leap" -> skill.power().flatMap(SkillPower::leap).isPresent();
          default -> false;
        };
    assertTrue(gives, gift);
  }

  @ParameterizedTest(name = "[{0}]")
  @CsvSource({"'', ''", "nightborn:veteran, nightborn:veteran", "apack:warden, apack:warden"})
  @DisplayName("a skill may name the guild rank it needs, and needs none when it names none")
  void readsItsGuildRank(final String rank, final String expected) {
    final String field = rank.isEmpty() ? "" : ",\"rank\":\"" + rank + "\"";
    final Skill skill =
        Skill.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"branch\":\"nightborn:slayer\",\"icon\":\"nightborn:icon\","
                        + "\"ranks\":[{\"cost\":2}]"
                        + field
                        + "}"))
            .getOrThrow();
    assertEquals(
        expected.isEmpty() ? Optional.empty() : Optional.of(Identifier.of(expected)), skill.rank());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "thick_blood",
    "grave_strength",
    "vanish",
    "blood_rush",
    "night_eyes",
    "blood_healing",
    "iron_grip",
    "deep_veins",
    "unbroken",
    "sanguine_mastery",
    "beast_blood",
    "beast_claws",
    "swift_prowl",
    "long_leap",
    "bat_form",
    "hear_heartbeats",
    "pounce",
    "blood_frenzy",
    "night_terror",
    "apex_predator",
    "noble_blood",
    "mesmerise",
    "silver_tongue",
    "blood_bond",
    "command",
    "dark_court",
    "dread_presence",
    "blood_tithe",
    "glamour",
    "sovereigns_gaze",
    "born_to_rule",
    "occult_blood",
    "grave_fog",
    "summon_familiar",
    "shadow_step",
    "spirit_pact",
    "hex",
    "shadow_veil",
    "blood_ward",
    "mist_form",
    "raise_the_dead",
    "black_mass",
    "beyond_the_veil",
    "tracking",
    "field_craft",
    "snare",
    "bait",
    "net_shot",
    "quick_hands",
    "trap_sense",
    "silver_edge",
    "flash_trap",
    "killing_ground",
    "master_trapper",
    "faith",
    "mend",
    "rebuke",
    "blessing",
    "grace",
    "blood_fire",
    "sanctuary",
    "consecrate",
    "judgement",
    "unshaken_faith",
    "bite_ward",
    "sidestep",
    "stake_throw",
    "vampire_slayer",
    "riposte",
    "quick_shot",
    "tough_as_oak",
    "heartstrike",
    "last_stand",
    "onslaught",
    "slayers_calm",
    "keen_tracking",
    "iron_will",
    "sixth_sense",
    "soft_step",
    "read_the_scene",
    "mark_quarry",
    "grave_listener",
    "true_sight",
    "revelation",
    "hunters_patience"
  })
  @DisplayName("every skill in the skill lists has a 16 by 16 icon of its own")
  void iconShips(final String name) throws IOException {
    final BufferedImage image =
        ImageIO.read(SPRITES.resolve("skills/icon/" + name + ".png").toFile());
    assertNotNull(image, name);
    assertEquals(16, image.getWidth());
    assertEquals(16, image.getHeight());
  }
}
