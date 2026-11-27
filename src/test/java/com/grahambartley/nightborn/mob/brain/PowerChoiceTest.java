package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillPower;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PowerChoiceTest {

  private static final Identifier RUSH = Identifier.of("nightborn", "core/blood_rush");
  private static final Identifier STARE = Identifier.of("nightborn", "noble/mesmerise");
  private static final Identifier SOUND = Identifier.of("minecraft", "x");
  private static final PowerUse.Verdict[] VERDICTS = {
    PowerUse.Verdict.READY,
    PowerUse.Verdict.READY,
    PowerUse.Verdict.COOLING,
    PowerUse.Verdict.WEAKENED,
    PowerUse.Verdict.THIRSTY
  };

  private static PowerChoice.Option option(
      final Identifier id,
      final PowerUse.Verdict verdict,
      final int cost,
      final float min,
      final float max) {
    return new PowerChoice.Option(
        id, verdict, cost, new MobPowerUse(min, max, 10, SOUND, "tell", false));
  }

  private static PowerChoice.Option option(final Identifier id, final MobUse... uses) {
    return new PowerChoice.Option(
        id,
        PowerUse.Verdict.READY,
        0,
        new MobPowerUse(0.0F, 0.0F, 10, SOUND, "tell", false, List.of(uses)));
  }

  private static Situation fight(final double distance, final int blood) {
    return new Situation(
        Intent.FIGHT, Situation.Self.of(1.0F, blood), new Situation.Target(distance, true, 1.0F));
  }

  private static Optional<Identifier> choose(
      final List<PowerChoice.Option> options,
      final Situation situation,
      final IntentBudget budget,
      final int floor) {
    return PowerChoice.choose(PowerChoice.rank(options, situation, budget, floor), budget)
        .map(scored -> scored.option().power());
  }

  private static Optional<Identifier> firstWorthCasting(
      final List<PowerChoice.Option> options,
      final double distance,
      final int blood,
      final int floor) {
    return options.stream()
        .filter(
            option ->
                option.verdict() == PowerUse.Verdict.READY
                    && distance >= option.use().minRange()
                    && distance <= option.use().maxRange()
                    && blood - option.cost() >= floor)
        .findFirst()
        .map(PowerChoice.Option::power);
  }

  static Stream<Integer> seeds() {
    return IntStream.range(0, 200).boxed();
  }

  @ParameterizedTest(name = "seed {0}")
  @MethodSource("seeds")
  @DisplayName(
      "a lone mob picks the power the old rule picked: the first ready one in range that leaves"
          + " its blood floor")
  void samePicksAsTheFirstReadyRule(final int seed) {
    final Random random = new Random(seed);
    final List<PowerChoice.Option> options = new ArrayList<>();
    for (int i = 0; i < 1 + random.nextInt(6); i++) {
      final float min = random.nextInt(8);
      options.add(
          option(
              Identifier.of("nightborn", "test/power_" + i),
              VERDICTS[random.nextInt(VERDICTS.length)],
              random.nextInt(8),
              min,
              min + random.nextInt(24)));
    }
    final double distance = random.nextInt(32) + random.nextDouble();
    final int blood = random.nextInt(21);
    final int floor = random.nextBoolean() ? 0 : random.nextInt(12);
    assertEquals(
        firstWorthCasting(options, distance, blood, floor),
        choose(options, fight(distance, blood), IntentBudget.FREE, floor));
  }

  @ParameterizedTest(name = "{0}, {1} blocks, blood {2}, floor {3}: {4}")
  @CsvSource({
    "READY, 10, 13, 10, true",
    "READY, 10, 12, 10, false",
    "READY, 3, 20, 0, false",
    "READY, 21, 20, 0, false",
    "COOLING, 10, 20, 0, false",
    "HELPLESS, 10, 20, 0, false",
    "NO_POWER, 10, 20, 0, false"
  })
  @DisplayName("only a ready power in range that leaves the blood floor is cast")
  void castsOnlyAReadyAffordablePowerInRange(
      final PowerUse.Verdict verdict,
      final double distance,
      final int blood,
      final int floor,
      final boolean casts) {
    assertEquals(
        casts ? Optional.of(RUSH) : Optional.empty(),
        choose(
            List.of(option(RUSH, verdict, 3, 4.0F, 20.0F)),
            fight(distance, blood),
            IntentBudget.FREE,
            floor));
  }

  @ParameterizedTest(name = "reserve {0}, blood {1}: casts {2}")
  @CsvSource({"0, 4, true", "5, 8, true", "5, 7, false", "12, 14, false"})
  @DisplayName("an intent's blood reserve raises the floor a cast must leave")
  void intentReserveRaisesTheFloor(final int reserve, final int blood, final boolean casts) {
    assertEquals(
        casts ? Optional.of(RUSH) : Optional.empty(),
        choose(
            List.of(option(RUSH, PowerUse.Verdict.READY, 3, 0.0F, 20.0F)),
            fight(5, blood),
            new IntentBudget(reserve, 0, 0.0F),
            0));
  }

  @ParameterizedTest(name = "threshold {0}: casts {1}")
  @CsvSource({"0.0, true", "0.49, true", "0.5, false", "2.0, false"})
  @DisplayName("the best score must pass the intent's threshold")
  void thresholdHoldsBackWeakUses(final float threshold, final boolean casts) {
    final MobUse half =
        new MobUse(
            "fight",
            "target",
            List.of(new Consideration("target_distance", Curve.FALLING, 0.0F, 10.0F)),
            1.0F);
    assertEquals(
        casts ? Optional.of(RUSH) : Optional.empty(),
        choose(List.of(option(RUSH, half)), fight(5, 20), new IntentBudget(0, 0, threshold), 0));
  }

  @Test
  @DisplayName("the highest scoring use wins, whatever order the powers were learned in")
  void bestScoreWins() {
    final MobUse weak = MobUse.inRange(Intent.FIGHT, 0.0F, 20.0F);
    final MobUse strong =
        new MobUse("fight", "target", MobUse.inRange(Intent.FIGHT, 0, 20).considerations(), 3.0F);
    final List<PowerChoice.Scored> ranked =
        PowerChoice.rank(
            List.of(option(STARE, weak), option(RUSH, weak, strong)),
            fight(5, 20),
            IntentBudget.FREE,
            0);
    assertEquals(List.of(RUSH, STARE), ranked.stream().map(s -> s.option().power()).toList());
    assertEquals(List.of(3.0, 1.0), ranked.stream().map(PowerChoice.Scored::score).toList());
  }

  @Test
  @DisplayName("a power added through data alone is chosen by a mob that owns it")
  void madeUpSkillIsUsedFromDataAlone() {
    final Skill skill =
        Skill.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"branch\":\"nightborn:noble\",\"icon\":\"nightborn:skills/icon/x\","
                        + "\"ranks\":[{\"cost\":1}],\"power\":{\"blood\":2,"
                        + "\"cooldown_ticks\":100,\"mob\":{\"max_range\":6,\"tell_ticks\":4,"
                        + "\"tell_sound\":\"minecraft:block.bell.use\",\"tell_clip\":\"stare\","
                        + "\"uses\":[{\"intent\":\"fight\",\"aim\":\"target\",\"weight\":2,"
                        + "\"considerations\":[{\"fact\":\"own_health\",\"curve\":\"falling\","
                        + "\"low\":0.2,\"high\":0.8}]}]}}}"))
            .getOrThrow();
    final Identifier ember = Identifier.of("nightborn", "test/ember_ward");
    final SkillPower power = skill.power().orElseThrow();
    final List<PowerChoice.Option> options =
        List.of(
            option(RUSH, PowerUse.Verdict.READY, 0, 0.0F, 20.0F),
            new PowerChoice.Option(
                ember, PowerUse.Verdict.READY, power.blood(), power.mob().orElseThrow()));
    final Situation hurt =
        new Situation(
            Intent.FIGHT, Situation.Self.of(0.3F, 20), new Situation.Target(5, true, 1.0F));
    assertEquals(Optional.of(ember), choose(options, hurt, IntentBudget.FREE, 0));
    assertEquals(Optional.of(RUSH), choose(options, fight(5, 20), IntentBudget.FREE, 0));
  }
}
