package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.UUID;
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
        .map(scored -> scored.option().id());
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
                    && distance >= range(option).low()
                    && distance <= range(option).high()
                    && blood - option.cost() >= floor)
        .findFirst()
        .map(PowerChoice.Option::id);
  }

  private static Consideration range(final PowerChoice.Option option) {
    return option.uses().getFirst().considerations().getFirst();
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
    assertEquals(List.of(RUSH, STARE), ranked.stream().map(s -> s.option().id()).toList());
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
  private static final Situation.Group ALONE = Situation.Group.of(List.of(), List.of());
  private static final Situation.Place DARK =
      new Situation.Place(Double.NaN, Route.UNKNOWN, Double.NaN, Double.NaN, 2.0, Double.NaN);

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
        signs.contains("hunter"),
        !signs.contains("unaware"));
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
        "faithful/blessing | 3 | 1 | vampire | faithful/blessing",
        "occult/shadow_veil | 7 | 1 | unaware | occult/shadow_veil",
        "occult/shadow_veil | 7 | 1 | '' | ''",
        "occult/summon_familiar | 20 | 1 | '' | occult/summon_familiar",
        "occult/summon_familiar | 26 | 1 | '' | ''"
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
                Intent.FIGHT,
                Situation.Self.of(1.0F, 20),
                target(distance, health, signs),
                DARK,
                ALONE,
                null),
            shippedBudget(Intent.FIGHT),
            0));
  }

  @ParameterizedTest(name = "[{0}] {1}, health {2}, gap {3}, detour {4}, home {5}, dawn {6}: {7}")
  @CsvSource(
      delimiter = '|',
      value = {
        "beast/bat_form | TRAVEL | 1 | 1 | NaN | NaN | NaN | beast/bat_form",
        "beast/bat_form | TRAVEL | 1 | 0 | 1.1 | NaN | NaN | ''",
        "beast/bat_form | TRAVEL | 1 | 0 | 2.5 | NaN | NaN | beast/bat_form",
        "beast/bat_form | TRAVEL | 1 | 0 | 1 | 60 | 500 | beast/bat_form",
        "beast/bat_form | TRAVEL | 1 | 0 | 1 | 60 | 6000 | ''",
        "occult/shadow_step | TRAVEL | 1 | 0 | 1 | 60 | 500 | occult/shadow_step",
        "core/blood_healing | REST | 0.4 | NaN | NaN | NaN | NaN | core/blood_healing",
        "core/blood_healing beast/bat_form occult/shadow_step core/blood_rush occult/summon_familiar"
            + " | REST | 1 | NaN | NaN | NaN | NaN | ''"
      })
  @DisplayName(
      "outside a fight a vampire flies a gap, a long way round or home before dawn, heals once it"
          + " is hurt, and spends nothing while it rests unhurt")
  void usesPowersOutsideAFight(
      final String owned,
      final Intent intent,
      final float health,
      final double gap,
      final double detour,
      final double home,
      final double untilDawn,
      final String chosen) {
    final Situation situation =
        new Situation(
            intent,
            Situation.Self.of(health, 20),
            null,
            new Situation.Place(8.0, new Route(gap, detour, 30.0), home, untilDawn, 2.0, 4.0));
    assertEquals(
        chosen.isEmpty() ? Optional.empty() : Optional.of(Identifier.of("nightborn", chosen)),
        choose(
            Arrays.stream(owned.split(" ")).map(skill -> shipped(skill, false)).toList(),
            situation,
            shippedBudget(intent),
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
        scored.map(s -> s.option().id()));
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
            .map(scored -> scored.option().id()));
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

  private static Situation.Member ally(
      final int id, final double distance, final float health, final boolean underAttack) {
    return new Situation.Member(new UUID(0, id), distance, health, underAttack, false, true, false);
  }

  private static Situation group(
      final float ownHealth,
      final Situation.Target target,
      final List<Situation.Member> allies,
      final List<Double> foes) {
    return new Situation(
        Intent.FIGHT,
        Situation.Self.of(ownHealth, 20),
        target,
        Situation.Place.UNKNOWN,
        Situation.Group.of(allies, foes),
        null);
  }

  @ParameterizedTest(name = "own health {0}, ally health {1} at {2} blocks: {3} for {4}")
  @CsvSource({
    "1, 0.25, 3, ally, 1",
    "1, 0.9, 3, '', 0",
    "1, 0.25, 10, '', 0",
    "0.25, 0.25, 3, self, 0",
    "0.6, 0.25, 3, ally, 1"
  })
  @DisplayName(
      "a healthy hunter binds the wounds of a badly hurt ally beside it, and one close to going"
          + " down saves itself first")
  void healsAHurtAllyWhileHealthy(
      final float own,
      final float allyHealth,
      final double distance,
      final String aimed,
      final int helped) {
    final Optional<PowerChoice.Scored> chosen =
        PowerChoice.choose(
            PowerChoice.rank(
                List.of(shipped("hunter/bind_wounds", false)),
                group(own, null, List.of(ally(1, distance, allyHealth, false)), List.of()),
                shippedBudget(Intent.FIGHT),
                0),
            shippedBudget(Intent.FIGHT),
            0.0);
    assertEquals(
        aimed.isEmpty() ? Optional.empty() : Optional.of(aimed),
        chosen.map(scored -> scored.use().aim()));
    assertEquals(
        helped == 0 ? Optional.empty() : Optional.of(new UUID(0, helped)),
        chosen.map(PowerChoice.Scored::ally).map(Situation.Member::id));
  }

  @Test
  @DisplayName(
      "a healer picks the ally who needs it most, and skips one another mob has claimed or one a"
          + " heal is already working on")
  void healsTheNeediestUnclaimedAlly() {
    final List<PowerChoice.Option> options = List.of(shipped("hunter/bind_wounds", false));
    final Situation.Member grazed = ally(1, 3, 0.6F, false);
    final Situation.Member dying = ally(2, 3, 0.2F, false);
    final Situation.Member claimed =
        new Situation.Member(new UUID(0, 3), 2, 0.1F, false, false, true, true);
    final Situation.Member mending =
        new Situation.Member(new UUID(0, 4), 2, 0.05F, false, true, true, false);
    final PowerChoice.Scored best =
        PowerChoice.rank(
                options,
                group(1.0F, null, List.of(grazed, dying, claimed, mending), List.of()),
                shippedBudget(Intent.FIGHT),
                0)
            .getFirst();
    assertEquals(dying, best.ally());
  }

  @ParameterizedTest(name = "ally under attack {0} at {1} blocks: {2}")
  @CsvSource({"true, 5, ally", "false, 5, ''", "true, 12, ''"})
  @DisplayName("a vampire wards an ally who is under attack, and not one who is safe or far off")
  void wardsTheAllyUnderAttack(final boolean underAttack, final double distance, final String aim) {
    final Optional<PowerChoice.Scored> chosen =
        PowerChoice.choose(
            PowerChoice.rank(
                List.of(shipped("occult/blood_ward", false)),
                group(1.0F, null, List.of(ally(4, distance, 0.8F, underAttack)), List.of()),
                shippedBudget(Intent.FIGHT),
                0),
            shippedBudget(Intent.FIGHT),
            0.0);
    assertEquals(
        aim.isEmpty() ? Optional.empty() : Optional.of(aim),
        chosen.map(scored -> scored.use().aim()));
  }

  @ParameterizedTest(name = "{0} with an ally beside it, foes at [{1}]: casts {2}")
  @CsvSource(
      delimiter = '|',
      value = {
        "occult/grave_fog | 2 3 3.5 | true",
        "occult/grave_fog | 2 | false",
        "occult/grave_fog | 2 9 12 | false",
        "noble/dread_presence | 3 5 7 | true",
        "noble/dread_presence | 3 | false",
        "faithful/consecrate | 1 2 4 | true",
        "faithful/consecrate | 2 | false",
        "noble/sovereigns_gaze | 4 8 11 | true",
        "occult/black_mass | 4 6 10 | true",
        "trapper/killing_ground | 3 6 7 | true"
      })
  @DisplayName(
      "in a group fight an area power is used when it would catch three foes and held against one")
  void areaPowerWaitsForAClusterInAGroupFight(
      final String skill, final String foes, final boolean casts) {
    final List<Double> distances = Arrays.stream(foes.split(" ")).map(Double::valueOf).toList();
    final double nearest = distances.getFirst();
    final Optional<PowerChoice.Scored> chosen =
        PowerChoice.choose(
            PowerChoice.rank(
                List.of(shipped(skill, false)),
                group(
                    0.4F,
                    target(
                        nearest,
                        0.4F,
                        skill.startsWith("faithful") || skill.startsWith("trapper")
                            ? "vampire"
                            : "hunter"),
                    List.of(ally(5, 2, 1.0F, false)),
                    distances),
                shippedBudget(Intent.FIGHT),
                0),
            shippedBudget(Intent.FIGHT),
            0.0);
    assertEquals(casts, chosen.isPresent());
    chosen.ifPresent(scored -> assertEquals("foes", scored.use().aim()));
  }

  @ParameterizedTest(name = "{0} alone against one foe at {1} blocks: casts {2}")
  @CsvSource({
    "occult/grave_fog, 3, true",
    "noble/dread_presence, 5, true",
    "faithful/consecrate, 3, true"
  })
  @DisplayName("a mob alone still uses an area power on its one foe, as it did before")
  void aMobAloneFightsAsBefore(final String skill, final double distance, final boolean casts) {
    final Optional<PowerChoice.Scored> chosen =
        PowerChoice.choose(
            PowerChoice.rank(
                List.of(shipped(skill, false)),
                group(
                    0.4F,
                    target(distance, 0.4F, skill.startsWith("faithful") ? "vampire" : "hunter"),
                    List.of(),
                    List.of(distance)),
                shippedBudget(Intent.FIGHT),
                0),
            shippedBudget(Intent.FIGHT),
            0.0);
    assertEquals(casts, chosen.isPresent());
  }

  @ParameterizedTest(name = "{0} beside an idle ally: casts {1}")
  @CsvSource({"occult/grave_fog, true", "faithful/consecrate, true"})
  @DisplayName(
      "an ally standing idle nearby, such as a housemate asleep in its coffin, does not stop a"
          + " mob using an area power on its one foe")
  void anIdleAllyLeavesTheMobAlone(final String skill, final boolean casts) {
    final Situation.Member idle =
        new Situation.Member(new UUID(0, 9), 3, 1.0F, false, false, false, false);
    final Optional<PowerChoice.Scored> chosen =
        PowerChoice.choose(
            PowerChoice.rank(
                List.of(shipped(skill, false)),
                group(
                    0.4F,
                    target(3, 0.4F, skill.startsWith("faithful") ? "vampire" : "hunter"),
                    List.of(idle),
                    List.of(3.0)),
                shippedBudget(Intent.FIGHT),
                0),
            shippedBudget(Intent.FIGHT),
            0.0);
    assertEquals(casts, chosen.isPresent());
  }

  private static PowerChoice.Option shippedAction(
      final String action, final PowerUse.Verdict verdict, final boolean running) {
    return PowerChoice.Option.action(
        new ActionReadiness(Identifier.of("nightborn", action), verdict, 60, running),
        parse(MobAction.CODEC, DATA.resolve("mob_action/" + action + ".json")));
  }

  private static Situation facing(
      final double distance, final boolean inSight, final boolean threatening) {
    return new Situation(
        Intent.FIGHT,
        Situation.Self.of(1.0F, 20),
        new Situation.Target(
            distance, inSight, 1.0F, false, false, false, false, false, false, true, threatening));
  }

  private static Optional<Identifier> chooseShipped(
      final List<PowerChoice.Option> options, final Situation situation, final int floor) {
    return choose(options, situation, shippedBudget(Intent.FIGHT), floor);
  }

  @ParameterizedTest(name = "{0} at {1} blocks, seen {2}, threatened {3}: chosen {4}")
  @CsvSource({
    "leap, 3, true, false, true",
    "leap, 5, true, false, true",
    "leap, 7, true, false, true",
    "leap, 2.5, true, false, false",
    "leap, 8, true, false, false",
    "dash, 7, true, false, true",
    "dash, 12, true, false, true",
    "dash, 16, true, false, true",
    "dash, 5, true, false, false",
    "dash, 17, true, false, false",
    "dodge, 2, true, true, true",
    "dodge, 12, false, true, true",
    "dodge, 2, true, false, false",
    "holy_water, 4, true, false, true",
    "holy_water, 10, true, false, true",
    "holy_water, 6, false, false, false",
    "holy_water, 3.9, true, false, false",
    "holy_water, 10.5, true, false, false",
    "crucifix, 3, true, false, true",
    "crucifix, 3, false, false, false"
  })
  @DisplayName(
      "the shipped built-in actions keep their old moments: leap in close, dash from afar, dodge a"
          + " threat, throw holy water at a foe in sight four to ten blocks off, raise the"
          + " crucifix at one in sight")
  void shippedActionsKeepTheirMoments(
      final String action,
      final double distance,
      final boolean inSight,
      final boolean threatening,
      final boolean chosen) {
    assertEquals(
        chosen ? Optional.of(Identifier.of("nightborn", action)) : Optional.empty(),
        chooseShipped(
            List.of(shippedAction(action, PowerUse.Verdict.READY, false)),
            facing(distance, inSight, threatening),
            0));
  }

  @ParameterizedTest(name = "[{0}] at {1} blocks, threatened {2}: {3}")
  @CsvSource({
    "holy_water crucifix, 5, false, holy_water",
    "crucifix holy_water, 5, false, holy_water",
    "crucifix holy_water, 3, false, crucifix",
    "leap dodge, 3.5, true, dodge",
    "dash dodge, 9, true, dodge",
    "leap dash, 7, false, leap",
    "dodge leap, 5, false, leap",
    "dash leap, 7, false, leap"
  })
  @DisplayName(
      "between built-in actions the old order holds: holy water before the crucifix, a dodge"
          + " before a leap or a dash, and a leap before a dash where their ranges meet")
  void shippedActionsKeepTheirOrder(
      final String actions, final double distance, final boolean threatening, final String chosen) {
    assertEquals(
        Optional.of(Identifier.of("nightborn", chosen)),
        chooseShipped(
            Arrays.stream(actions.split(" "))
                .map(action -> shippedAction(action, PowerUse.Verdict.READY, false))
                .toList(),
            facing(distance, true, threatening),
            0));
  }

  @ParameterizedTest(name = "{0} beside blood rush at {1} blocks: {2}")
  @CsvSource({
    "leap, 5, core/blood_rush",
    "dash, 10, core/blood_rush",
    "crucifix, 5, crucifix",
    "leap, 3.5, leap",
    "holy_water, 5, holy_water"
  })
  @DisplayName(
      "a ready power in range comes before a leap or a dash, which fill the moments a mob has no"
          + " power for, while holy water and the crucifix go up first when they can")
  void powersBeforeFillerActions(final String action, final double distance, final String chosen) {
    assertEquals(
        Optional.of(Identifier.of("nightborn", chosen)),
        chooseShipped(
            List.of(
                shipped("core/blood_rush", false),
                shippedAction(action, PowerUse.Verdict.READY, false)),
            facing(distance, true, false),
            0));
  }

  @ParameterizedTest(name = "{0}, threatened {1}")
  @CsvSource({"dodge, true", "dodge, false", "leap, false", "dash, false", "holy_water, false"})
  @DisplayName("no built-in action is chosen for a mob that is falling back")
  void noActionWhileFallingBack(final String action, final boolean threatening) {
    final Situation retreating =
        new Situation(
            Intent.RETREAT,
            Situation.Self.of(0.2F, 20),
            new Situation.Target(
                5.0, true, 1.0F, false, false, false, false, false, false, true, threatening));
    assertEquals(
        Optional.empty(),
        choose(
            List.of(shippedAction(action, PowerUse.Verdict.READY, false)),
            retreating,
            shippedBudget(Intent.RETREAT),
            0));
  }

  @ParameterizedTest(name = "blood {0}, floor {1}")
  @CsvSource({"0, 10", "5, 20", "20, 0"})
  @DisplayName("a built-in action spends no blood, so the blood floor never holds it back")
  void actionsIgnoreTheBloodFloor(final int blood, final int floor) {
    final Situation situation =
        new Situation(
            Intent.FIGHT, Situation.Self.of(1.0F, blood), new Situation.Target(5.0, true, 1.0F));
    final List<PowerChoice.Scored> ranked =
        PowerChoice.rank(
            List.of(shippedAction("leap", PowerUse.Verdict.READY, false)),
            situation,
            shippedBudget(Intent.FIGHT),
            floor);
    assertTrue(ranked.getFirst().affordable());
    assertTrue(ranked.getFirst().option().action());
  }

  @ParameterizedTest(name = "{0}, running {1}")
  @CsvSource({"COOLING, false", "READY, true", "COOLING, true"})
  @DisplayName("a built-in action that is not ready, or a crucifix already raised, is not weighed")
  void unreadyOrRunningActionsAreNotWeighed(final PowerUse.Verdict verdict, final boolean running) {
    assertEquals(
        List.of(),
        PowerChoice.rank(
            List.of(shippedAction("crucifix", verdict, running)),
            facing(3.0, true, false),
            shippedBudget(Intent.FIGHT),
            0));
  }
}
