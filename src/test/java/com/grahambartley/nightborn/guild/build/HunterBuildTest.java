package com.grahambartley.nightborn.guild.build;

import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.CROSSBOW;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.STAKE;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.brain.Aim;
import com.grahambartley.nightborn.mob.brain.ChoiceFixtures;
import com.grahambartley.nightborn.mob.brain.Disposition;
import com.grahambartley.nightborn.mob.brain.IntentBudget;
import com.grahambartley.nightborn.mob.brain.Mind;
import com.grahambartley.nightborn.mob.brain.Sharpness;
import com.grahambartley.nightborn.rank.RankLadder;
import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterBuildTest {

  private static final HunterBuild SHIPPED = HunterBuildFixtures.build();

  @ParameterizedTest(name = "rank {0} thinks no slower than rank {1}")
  @CsvSource({"1, 0", "2, 1", "3, 2"})
  @DisplayName("an older rank thinks about its powers more often")
  void olderRanksUsePowersMoreOften(final int older, final int younger) {
    final RankLadder ladder = HunterBuildFixtures.ladder();
    final Optional<Identifier> olderId = ladder.rung(older).map(RankLadder.Rung::id);
    final Optional<Identifier> youngerId = ladder.rung(younger).map(RankLadder.Rung::id);
    assertTrue(
        SHIPPED.mind(olderId, Optional.empty()).thinkTicks()
            < SHIPPED.mind(youngerId, Optional.empty()).thinkTicks());
  }

  @ParameterizedTest(name = "rank {0} is sharper than rank {1}")
  @CsvSource({"1, 0", "2, 1", "3, 2"})
  @DisplayName("an older rank blurs its choice less, sees more and plans further ahead")
  void olderRanksAreSharper(final int older, final int younger) {
    final RankLadder ladder = HunterBuildFixtures.ladder();
    final Sharpness olderSharpness =
        SHIPPED.mind(ladder.rung(older).map(RankLadder.Rung::id), Optional.empty()).sharpness();
    final Sharpness youngerSharpness =
        SHIPPED.mind(ladder.rung(younger).map(RankLadder.Rung::id), Optional.empty()).sharpness();
    assertTrue(olderSharpness.blur() < youngerSharpness.blur());
    assertTrue(olderSharpness.foresight() > youngerSharpness.foresight());
    assertTrue(olderSharpness.reads().containsAll(youngerSharpness.reads()));
  }

  @ParameterizedTest(name = "{0} reads group {1} and place {2}")
  @CsvSource({"initiate, false, false", "master, true, true"})
  @DisplayName("an Initiate reads only itself and its foe, and a Master reads the whole fight")
  void readsByRank(final String rank, final boolean group, final boolean place) {
    final Sharpness sharpness = SHIPPED.mind(Optional.of(id(rank)), Optional.empty()).sharpness();
    assertEquals(group, sharpness.reads(Sharpness.Reads.GROUP));
    assertEquals(place, sharpness.reads(Sharpness.Reads.PLACE));
  }

  @ParameterizedTest(name = "rank {0}, kit {1}")
  @CsvSource({
    "none, none, true, true",
    "baron, crossbow, true, false",
    "master, jester, false, true"
  })
  @DisplayName("a missing rank thinks keenly and a missing kit leans no way")
  void mindFallsBack(final String rank, final String kit, final boolean keen, final boolean even) {
    final Mind mind =
        SHIPPED.mind(
            rank.equals("none") ? Optional.empty() : Optional.of(id(rank)),
            kit.equals("none") ? Optional.empty() : Optional.of(id(kit)));
    assertEquals(keen, mind.sharpness().equals(Sharpness.KEEN));
    assertEquals(even, mind.disposition().equals(Disposition.EVEN));
  }

  @ParameterizedTest(name = "rank {0} weighs less than rank {1}")
  @CsvSource({"hunter, initiate", "veteran, hunter", "master, veteran"})
  @DisplayName("most hunters are Initiates and Hunters, and few are Veterans or Masters")
  void olderRanksAreRarer(final String older, final String younger) {
    assertTrue(SHIPPED.rankWeights().get(id(older)) < SHIPPED.rankWeights().get(id(younger)));
  }

  @ParameterizedTest(name = "kit {0}, skill {1}, path {2} -> {3}")
  @CsvSource({
    "crossbow, slayer/quick_shot, true, 11",
    "crossbow, slayer/heartstrike, true, 3",
    "stake, slayer/heartstrike, true, 11",
    "stake, hunter/faith, false, 6",
    "none, slayer/heartstrike, true, 3",
    "none, hunter/tracking, false, 2"
  })
  @DisplayName("what a hunter buys leans toward its kit, and its paths over its core")
  void weighsByKitAndPath(
      final String kit, final String skill, final boolean path, final int weight) {
    final Optional<Identifier> held = kit.equals("none") ? Optional.empty() : Optional.of(id(kit));
    assertEquals(weight, SHIPPED.weight(held, id(skill), path));
  }

  @ParameterizedTest(name = "carried {0}, roll {1} -> {2}")
  @CsvSource({
    "plain;silver, 0.0, plain",
    "plain;silver, 0.24, plain",
    "plain;silver, 0.25, silver",
    "plain;silver, 0.99, silver",
    "silver, 0.0, silver",
    "plain, 0.99, plain",
    "crossbow, 0.5, none"
  })
  @DisplayName("a Guild Master's kit is rolled by weight from the master kits that exist")
  void rollsAMasterKitThatExists(final String carried, final double roll, final String kit) {
    final HunterBuild build =
        new HunterBuild(
            Map.of(),
            Map.of(),
            Map.of(),
            0,
            1,
            1,
            Map.of(),
            Map.of(),
            Map.of(),
            0,
            1200,
            0,
            Map.of(id("plain"), 1, id("silver"), 3));
    final Set<Identifier> known =
        Arrays.stream(carried.split(";")).map(HunterBuildFixtures::id).collect(Collectors.toSet());
    assertEquals(
        kit.equals("none") ? Optional.empty() : Optional.of(id(kit)), build.masterKit(known, roll));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"nightborn:master_silver"})
  @DisplayName("the shipped build arms every Guild Master from a master kit")
  void shippedArmsGuildMasters(final String kit) {
    assertEquals(
        Optional.of(Identifier.of(kit)), SHIPPED.masterKit(Set.of(Identifier.of(kit)), 0.5));
  }

  @ParameterizedTest(name = "age {0} trickles: {1}")
  @CsvSource({"0, false", "1, false", "1199, false", "1200, true", "2400, true", "2401, false"})
  @DisplayName("a living hunter earns a trickle of experience at a steady interval")
  void tricklesAtItsInterval(final long age, final boolean trickles) {
    assertEquals(trickles, SHIPPED.trickles(age));
  }

  @ParameterizedTest(name = "age {0}")
  @ValueSource(longs = {1200L, 2400L})
  @DisplayName("a build with no trickle never trickles")
  void noTrickle(final long age) {
    assertFalse(HunterBuild.NONE.trickles(age));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"rank_weights\":{}} | true",
        "{\"rank_weights\":{},\"think_ticks\":{\"nightborn:master\":0}} | false",
        "{\"rank_weights\":{},\"trickle_ticks\":0} | false",
        "{\"rank_weights\":{\"nightborn:master\":1001}} | false",
        "{} | false"
      })
  @DisplayName("the build reads only inside its bounds")
  void readsInsideItsBounds(final String json, final boolean reads) {
    assertEquals(
        reads, HunterBuild.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0} points of stake experience")
  @ValueSource(ints = {250})
  @DisplayName("staking a vampire pays more than a trickle")
  void stakingPaysMore(final int stake) {
    assertEquals(stake, SHIPPED.stakeExperience());
    assertTrue(SHIPPED.stakeExperience() > SHIPPED.trickleExperience());
    assertEquals(CROSSBOW, id("crossbow"));
    assertEquals(STAKE, id("stake"));
  }

  @ParameterizedTest(name = "{0}, rolls {1} and {2}: {3}")
  @CsvSource({
    "initiate, 0.25, 0.75, guard",
    "hunter, 0.25, 0.75, guard",
    "veteran, 0.25, 0.75, strike",
    "master, 0, 1, strike"
  })
  @DisplayName(
      "with the same powers in the same fight, an Initiate can be fooled by chance and a Master"
          + " cannot")
  void shippedRanksChooseByRank(
      final String rank, final double first, final double second, final String picked) {
    assertEquals(
        ChoiceFixtures.id(picked),
        ChoiceFixtures.pick(
            ChoiceFixtures.strikeOrGuard(0),
            SHIPPED.mind(Optional.of(id(rank)), Optional.empty()),
            IntentBudget.FREE,
            ChoiceFixtures.rolls(first, second)));
  }

  @ParameterizedTest(name = "{0} leans {2} toward {1}")
  @CsvSource({
    "crossbow, TARGET, 1.25",
    "stake, TARGET, 1.2",
    "silver_stake, ALLY, 1.25",
    "raid_silver, ALLY, 1.15",
    "master_silver, TARGET, 1"
  })
  @DisplayName("a hunter's kit colours what it reaches for: its quarry, or its fellows")
  void kitsLean(final String kit, final Aim aim, final double lean) {
    assertEquals(
        lean, SHIPPED.mind(Optional.empty(), Optional.of(id(kit))).disposition().lean(aim), 1.0E-6);
  }

  @ParameterizedTest(name = "kit {0} -> {1} slots")
  @CsvSource({"crossbow, 2", "raid_wooden, 1", "jester, 0", "none, 0"})
  @DisplayName("a kit gives the path slots it names, and a missing kit gives none")
  void pathsByKit(final String kit, final int slots) {
    final Optional<Identifier> held = kit.equals("none") ? Optional.empty() : Optional.of(id(kit));
    assertEquals(slots, SHIPPED.paths(held).size());
  }

  @ParameterizedTest(name = "slot {0} -> {1}")
  @CsvSource({"0, faithful", "1, slayer"})
  @DisplayName("the Guild Master's kit makes Faithful and Slayer certain")
  void masterKitPathsAreCertain(final int slot, final String path) {
    assertEquals(Map.of(id(path), 1), SHIPPED.paths(Optional.of(id("master_silver"))).get(slot));
  }
}
