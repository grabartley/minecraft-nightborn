package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillPower;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
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
    return PowerChoice.choose(PowerChoice.rank(options, situation, budget, floor), budget, 0.0)
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

  private static final Path DATA = Path.of("src/main/resources/data/nightborn/nightborn");

  private static PowerChoice.Option shipped(final String skill, final boolean running) {
    final SkillPower power =
        parse(Skill.CODEC, DATA.resolve("skill/" + skill + ".json")).power().orElseThrow();
    return new PowerChoice.Option(
        Identifier.of("nightborn", skill),
        PowerUse.Verdict.READY,
        power.blood(),
        power.cooldownTicks(),
        running,
        power.mob().orElseThrow());
  }

  private static IntentBudget shippedBudget(final Intent intent) {
    return parse(IntentBudget.CODEC, DATA.resolve("mob_intent/" + intent.key() + ".json"));
  }

  private static <T> T parse(final Codec<T> codec, final Path file) {
    try {
      return codec
          .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
          .getOrThrow();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Situation.Target target(
      final double distance, final float health, final String signs) {
    return new Situation.Target(
        distance,
        true,
        health,
        signs.contains("winding"),
        signs.contains("running"),
        signs.contains("slowed"),
        signs.contains("airborne"),
        signs.contains("vampire"),
        signs.contains("hunter"));
  }

  @ParameterizedTest(name = "[{0}] at {1} blocks, health {2}, signs [{3}]: {4}")
  @CsvSource(
      delimiter = '|',
      value = {
        "occult/shadow_step beast/pounce | 14 | 1 | '' | occult/shadow_step",
        "occult/shadow_step beast/pounce | 8 | 1 | running | beast/pounce",
        "trapper/net_shot slayer/quick_shot | 10 | 1 | running vampire | trapper/net_shot",
        "trapper/net_shot | 10 | 1 | running slowed vampire | ''",
        "trapper/snare | 8 | 1 | slowed vampire | ''",
        "trapper/snare | 8 | 1 | vampire | trapper/snare",
        "trapper/snare | 3 | 1 | vampire | ''",
        "occult/shadow_step beast/pounce | 8 | 1 | '' | occult/shadow_step",
        "occult/shadow_step beast/pounce | 8 | 0.25 | '' | beast/pounce",
        "noble/mesmerise | 6 | 1 | slowed | ''",
        "noble/dread_presence | 4 | 1 | slowed | ''",
        "beast/long_leap | 2 | 1 | '' | ''",
        "beast/long_leap | 8 | 1 | '' | beast/long_leap",
        "slayer/quick_shot slayer/stake_throw | 21 | 1 | vampire | slayer/quick_shot",
        "slayer/quick_shot slayer/stake_throw | 10 | 1 | winding vampire | slayer/stake_throw",
        "faithful/rebuke slayer/heartstrike | 5 | 1 | winding vampire | faithful/rebuke",
        "slayer/heartstrike slayer/stake_throw | 4 | 0.25 | vampire | slayer/heartstrike",
        "slayer/quick_shot trapper/net_shot | 12 | 1 | airborne vampire | trapper/net_shot",
        "beast/night_terror | 6 | 0.15 | '' | ''",
        "beast/night_terror | 6 | 1 | '' | beast/night_terror",
        "occult/blood_ward | 3 | 1 | hunter | occult/blood_ward",
        "occult/blood_ward | 3 | 1 | '' | ''",
        "faithful/blessing | 3 | 1 | '' | ''",
        "faithful/blessing | 3 | 1 | vampire | faithful/blessing"
      })
  @DisplayName(
      "in a fight a mob closes on a far foe, snares a runner, stuns one winding up, finishes one"
          + " nearly down and wards against the kind of foe in front of it")
  void picksTheRightPowerForTheMoment(
      final String owned,
      final double distance,
      final float health,
      final String signs,
      final String chosen) {
    final List<PowerChoice.Option> options =
        Arrays.stream(owned.split(" ")).map(skill -> shipped(skill, false)).toList();
    assertEquals(
        chosen.isEmpty() ? Optional.empty() : Optional.of(Identifier.of("nightborn", chosen)),
        choose(
            options,
            new Situation(
                Intent.FIGHT, Situation.Self.of(1.0F, 20), target(distance, health, signs)),
            shippedBudget(Intent.FIGHT),
            0));
  }

  @ParameterizedTest(name = "{0}, sun {1}, route {2} blocks: {3}")
  @CsvSource({
    "core/vanish, false, 10, core/vanish",
    "core/vanish, false, NaN, ''",
    "occult/shadow_step, true, 6, occult/shadow_step",
    "occult/shadow_step, false, 6, occult/shadow_step",
    "beast/long_leap, true, 5, beast/long_leap",
    "beast/pounce, true, 5, ''"
  })
  @DisplayName(
      "a mob falling back, or a vampire caught in the sun, uses an escape power toward where it is"
          + " headed")
  void escapesTowardItsRoute(
      final String owned, final boolean sunlit, final double route, final String chosen) {
    final Situation escaping =
        new Situation(
            Intent.RETREAT,
            new Situation.Self(0.3F, 20, false, sunlit, false, false),
            null,
            new Situation.Place(route));
    final Optional<PowerChoice.Scored> scored =
        PowerChoice.choose(
            PowerChoice.rank(
                List.of(shipped(owned, false)), escaping, shippedBudget(Intent.RETREAT), 0),
            shippedBudget(Intent.RETREAT),
            0.0);
    assertEquals(
        chosen.isEmpty() ? Optional.empty() : Optional.of(Identifier.of("nightborn", chosen)),
        scored.map(s -> s.option().power()));
    scored.ifPresent(s -> assertEquals(Aim.SPOT, s.use().aimed()));
  }

  @ParameterizedTest(name = "running {0}: casts {1}")
  @CsvSource({"false, true", "true, false"})
  @DisplayName("a mob never casts a buff it already has running")
  void neverRecastsARunningBuff(final boolean running, final boolean casts) {
    assertEquals(
        casts ? Optional.of(Identifier.of("nightborn", "core/blood_rush")) : Optional.empty(),
        choose(
            List.of(shipped("core/blood_rush", running)),
            fight(8, 20),
            shippedBudget(Intent.FIGHT),
            0));
  }

  @ParameterizedTest(name = "own health {0}: {1}")
  @CsvSource({"1.0, test/cheap", "0.1, test/costly"})
  @DisplayName("a healthy mob saves a costly power, and a losing one spends it")
  void thrift(final float health, final String chosen) {
    final IntentBudget thrifty = new IntentBudget(0, 0, 0.0F, 0.5F, 0.0F);
    final MobUse use = MobUse.inRange(Intent.FIGHT, 0.0F, 20.0F);
    final MobUse keener = new MobUse("fight", "target", use.considerations(), 1.3F);
    final List<PowerChoice.Option> options =
        List.of(
            new PowerChoice.Option(
                Identifier.of("nightborn", "test/costly"),
                PowerUse.Verdict.READY,
                6,
                6000,
                false,
                new MobPowerUse(0.0F, 20.0F, 10, SOUND, "tell", false, List.of(keener))),
            new PowerChoice.Option(
                Identifier.of("nightborn", "test/cheap"),
                PowerUse.Verdict.READY,
                0,
                0,
                false,
                new MobPowerUse(0.0F, 20.0F, 10, SOUND, "tell", false, List.of(use))));
    assertEquals(
        Optional.of(Identifier.of("nightborn", chosen)),
        choose(
            options,
            new Situation(
                Intent.FIGHT, Situation.Self.of(health, 20), new Situation.Target(5, true, 1.0F)),
            thrifty,
            0));
  }

  @ParameterizedTest(name = "spread {0}, roll {1}: {2}")
  @CsvSource({
    "0, 0.99, first",
    "0.1, 0, first",
    "0.1, 0.6, second",
    "0.1, 0.99, second",
    "0.01, 0.99, first"
  })
  @DisplayName("a close score is settled by chance, and a clear winner is not")
  void settlesCloseScoresByChance(final float spread, final double roll, final String chosen) {
    final MobUse first = new MobUse("fight", "target", List.of(), 2.0F);
    final MobUse second = new MobUse("fight", "target", List.of(), 1.9F);
    final MobUse third = new MobUse("fight", "target", List.of(), 1.0F);
    final IntentBudget budget = new IntentBudget(0, 0, 0.0F, 0.0F, spread);
    assertEquals(
        Optional.of(Identifier.of("nightborn", chosen)),
        PowerChoice.choose(
                PowerChoice.rank(
                    List.of(
                        option(Identifier.of("nightborn", "first"), first),
                        option(Identifier.of("nightborn", "second"), second),
                        option(Identifier.of("nightborn", "third"), third)),
                    fight(5, 20),
                    budget,
                    0),
                budget,
                roll)
            .map(scored -> scored.option().power()));
  }

  @ParameterizedTest(name = "ready aims [{0}]: {1}")
  @CsvSource({"TARGET SPOT, spot", "TARGET, target", "'', ''"})
  @DisplayName("a use is weighed only when its power is ready for that use's aim")
  void weighsOnlyUsesWhoseAimIsReady(final String ready, final String chosen) {
    final Set<Aim> aims = EnumSet.noneOf(Aim.class);
    if (!ready.isEmpty()) {
      Arrays.stream(ready.split(" ")).map(Aim::valueOf).forEach(aims::add);
    }
    final MobUse atSpot = new MobUse("fight", "spot", List.of(), 2.0F);
    final MobUse atTarget = new MobUse("fight", "target", List.of(), 1.0F);
    final PowerChoice.Option option =
        new PowerChoice.Option(
            RUSH,
            aims.isEmpty() ? PowerUse.Verdict.NOWHERE : PowerUse.Verdict.READY,
            0,
            0,
            false,
            new MobPowerUse(0.0F, 8.0F, 1, SOUND, "tell", false, List.of(atSpot, atTarget)),
            aims);
    assertEquals(
        chosen.isEmpty() ? Optional.empty() : Optional.of(chosen),
        PowerChoice.choose(
                PowerChoice.rank(List.of(option), fight(5, 20), IntentBudget.FREE, 0),
                IntentBudget.FREE,
                0.0)
            .map(scored -> scored.use().aim()));
  }
}
