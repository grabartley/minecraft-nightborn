package com.grahambartley.nightborn.mob.brain;

import com.google.gson.JsonParser;
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
import java.util.List;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import net.minecraft.util.Identifier;

public final class ChoiceFixtures {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn/nightborn");

  private ChoiceFixtures() {}

  public static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  public static List<PowerChoice.Option> strikeOrGuard(final int strikeCost) {
    return List.of(
        option(
            "strike",
            strikeCost,
            new MobUse(
                Intent.FIGHT.key(),
                Aim.TARGET.key(),
                List.of(new Consideration(Fact.TARGET_DISTANCE.key(), Curve.BETWEEN, 0, 8)),
                1.0F)),
        option(
            "guard",
            0,
            new MobUse(
                Intent.FIGHT.key(),
                Aim.SELF.key(),
                List.of(new Consideration(Fact.OWN_HEALTH.key(), Curve.BETWEEN, 0, 1)),
                0.8F)));
  }

  public static DoubleSupplier rolls(final double... rolls) {
    final int[] next = {0};
    return () -> rolls[next[0]++];
  }

  public static Identifier pick(
      final List<PowerChoice.Option> options,
      final Mind mind,
      final IntentBudget budget,
      final DoubleSupplier chance) {
    final Situation fight =
        new Situation(
            Intent.FIGHT, Situation.Self.of(1.0F, 20), new Situation.Target(5, true, 1.0F));
    final IntentBudget minded = mind.budget(budget);
    return PowerChoice.choose(
            PowerChoice.rank(options, fight, minded, 0, mind, chance), IntentBudget.FREE, 0.0)
        .orElseThrow()
        .option()
        .id();
  }

  private static PowerChoice.Option option(final String name, final int cost, final MobUse use) {
    return new PowerChoice.Option(
        id(name),
        PowerUse.Verdict.READY,
        cost,
        new MobPowerUse(
            0.0F, 0.0F, 10, Identifier.of("minecraft", "x"), "tell", false, List.of(use)));
  }

  public static PowerChoice.Option shipped(final String skill, final boolean running) {
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

  public static IntentBudget shippedBudget(final Intent intent) {
    return parse(IntentBudget.CODEC, DATA.resolve("mob_intent/" + intent.key() + ".json"));
  }

  public static <T> T parse(final Codec<T> codec, final Path file) {
    try {
      return codec
          .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
          .getOrThrow();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static Situation.Target target(
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

  public static Situation.Member ally(
      final int id, final double distance, final float health, final boolean underAttack) {
    return new Situation.Member(new UUID(0, id), distance, health, underAttack, false, true, false);
  }

  public static Situation group(
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

  public static PowerChoice.Option shippedAction(
      final String action, final PowerUse.Verdict verdict, final boolean running) {
    return PowerChoice.Option.action(
        new ActionReadiness(Identifier.of("nightborn", action), verdict, 60, running),
        parse(MobAction.CODEC, DATA.resolve("mob_action/" + action + ".json")));
  }

  public static Situation facing(
      final double distance, final boolean inSight, final boolean threatening) {
    return new Situation(
        Intent.FIGHT,
        Situation.Self.of(1.0F, 20),
        new Situation.Target(
            distance, inSight, 1.0F, false, false, false, false, false, false, true, threatening));
  }
}
