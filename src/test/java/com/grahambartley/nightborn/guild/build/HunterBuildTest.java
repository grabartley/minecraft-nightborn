package com.grahambartley.nightborn.guild.build;

import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.CROSSBOW;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.STAKE;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.rank.RankLadder;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
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
    assertTrue(SHIPPED.thinkTicks(olderId) < SHIPPED.thinkTicks(youngerId));
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
  @ValueSource(ints = {300})
  @DisplayName("staking a vampire pays more than a trickle")
  void stakingPaysMore(final int stake) {
    assertEquals(stake, SHIPPED.stakeExperience());
    assertTrue(SHIPPED.stakeExperience() > SHIPPED.trickleExperience());
    assertEquals(CROSSBOW, id("crossbow"));
    assertEquals(STAKE, id("stake"));
  }
}
