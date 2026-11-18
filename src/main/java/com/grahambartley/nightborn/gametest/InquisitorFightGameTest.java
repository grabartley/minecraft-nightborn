package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.boss.BossAttacks;
import com.grahambartley.nightborn.boss.BossFightPlan;
import com.grahambartley.nightborn.boss.BossFights;
import com.grahambartley.nightborn.boss.BossWard;
import com.grahambartley.nightborn.boss.ZoneAttack;
import com.grahambartley.nightborn.boss.ZoneShape;
import com.grahambartley.nightborn.boss.ZoneStrikes;
import com.grahambartley.nightborn.config.InquisitorSettings;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.hunt.HuntAttachment;
import com.grahambartley.nightborn.hunt.HuntRecord;
import com.grahambartley.nightborn.inquisitor.GrandInquisitorEntity;
import com.grahambartley.nightborn.inquisitor.InquisitorEnding;
import com.grahambartley.nightborn.inquisitor.InquisitorMobs;
import com.grahambartley.nightborn.inquisitor.InquisitorOutcomes;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sun.DirectSun;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class InquisitorFightGameTest implements FabricGameTest {

  static final String ARENA = EldestFightGameTest.ARENA;
  static final BlockPos CENTRE = new BlockPos(4, 2, 4);
  static final float FIRST_FLOOR = 0.70F;
  static final float HUGE = 100_000.0F;
  static final double NOTORIOUS = 40.0;

  private static GrandInquisitorEntity inquisitor(final TestContext context) {
    return inquisitor(context, CENTRE);
  }

  private static GrandInquisitorEntity inquisitor(final TestContext context, final BlockPos at) {
    final GrandInquisitorEntity man = context.spawnEntity(InquisitorMobs.GRAND_INQUISITOR, at);
    man.setAiDisabled(true);
    man.initialize(
        context.getWorld(),
        context.getWorld().getLocalDifficulty(man.getBlockPos()),
        SpawnReason.EVENT,
        null);
    return man;
  }

  private static GrandInquisitorEntity inArena(final TestContext context) {
    final GrandInquisitorEntity man = inquisitor(context);
    man.bossFight().claim(new Arena(context.getAbsolutePos(CENTRE), Arena.SMALLEST));
    return man;
  }

  private static void atPhase(final GrandInquisitorEntity man, final int phase) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putInt("BossPhase", phase);
    man.bossFight().read(nbt);
  }

  private static void hunted(final ServerPlayerEntity vampire, final GrandInquisitorEntity man) {
    vampire.setAttached(
        HuntAttachment.RECORD,
        HuntRecord.EMPTY.withNotoriety(NOTORIOUS).visitedBy(man.getUuid()).named("Ezra"));
  }

  private static void downed(final TestContext context, final GrandInquisitorEntity man) {
    atPhase(man, 2);
    man.setHealth(5.0F);
    man.damage(context.getWorld().getDamageSources().generic(), HUGE);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theShippedFightHasThreePhasesWhoseAttacksAllLoad(final TestContext context) {
    final var registries = context.getWorld().getRegistryManager();
    final BossFightPlan plan =
        BossFights.fight(registries, GrandInquisitorEntity.FIGHT).orElse(null);
    context.assertTrue(plan != null, "the Grand Inquisitor's fight loaded");
    context.assertTrue(
        plan.phases().stream()
            .map(phase -> phase.name())
            .toList()
            .equals(List.of("hunt", "faith", "pyre")),
        "the phases are the Hunt, the Faith and the Pyre");
    plan.phases()
        .forEach(
            phase ->
                phase
                    .attacks()
                    .forEach(
                        attack ->
                            context.assertTrue(
                                BossFights.attack(registries, attack).isPresent(),
                                phase.name() + " names a missing attack " + attack)));
    context.assertTrue(plan.wall().isPresent(), "his wall has its own look");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void theArenaSealsWhereHeCatchesHisQuarry(final TestContext context) {
    final GrandInquisitorEntity man = inquisitor(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(6, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          man.hunt(vampire, false, List.of(vampire));
          hunted(vampire, man);
          man.tickFight();
          final Arena arena = man.bossFight().arena().orElse(null);
          context.assertTrue(arena != null, "the circle seals once he is close");
          context.assertTrue(
              arena.centre().equals(vampire.getBlockPos()), "it seals where the quarry stands");
          context.assertTrue(
              arena.radius() == ServerConfigService.current().inquisitor().arenaRadius(),
              "its size is the setting");
          context.assertTrue(man.attacksOnSight(vampire), "he attacks the vampire on sight");
          man.discard();
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aBlowCannotCarryPastAPhaseAndTheChangeShrugsOffTheNext(final TestContext context) {
    final GrandInquisitorEntity man = inArena(context);
    final float floor = man.getMaxHealth() * FIRST_FLOOR;
    man.setHealth(floor + 5.0F);
    context.assertTrue(
        man.damage(context.getWorld().getDamageSources().generic(), HUGE), "the blow lands");
    context.assertTrue(
        Math.abs(man.getHealth() - floor) <= 0.51F,
        "he stops at the Hunt's floor of " + floor + ", not " + man.getHealth());
    context.assertTrue(man.bossFight().phase() == 1, "he is in the Faith");
    man.timeUntilRegen = 0;
    context.assertTrue(
        !man.damage(context.getWorld().getDamageSources().generic(), 5.0F),
        "he cannot be hurt while he changes");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aPrayerHealsHimUntilEnoughBlowsBreakIt(final TestContext context) {
    final GrandInquisitorEntity man = inquisitor(context, new BlockPos(1, 2, 1));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(7, 2, 7));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final BossFightPlan plan =
              BossFights.fight(context.getWorld().getRegistryManager(), GrandInquisitorEntity.FIGHT)
                  .orElseThrow();
          final BossAttacks attacks = new BossAttacks();
          final long now = context.getWorld().getTime();
          man.setTarget(vampire);
          man.setHealth(100.0F);
          attacks.counter(
              man, List.of(Identifier.of("nightborn", "inquisitor_prayer")), List.of(vampire), now);
          for (long tick = now; tick <= now + 30; tick++) {
            attacks.tick(man, plan.clips(), plan.phase(1), List.of(vampire), 300.0F, tick);
          }
          context.assertTrue(man.getHealth() > 100.0F, "he heals as he prays");
          context.assertTrue(
              attacks.channelBar(now + 30).isPresent(), "the bar shows him at prayer");
          attacks.hurt(30.0F);
          attacks.tick(man, plan.clips(), plan.phase(1), List.of(vampire), 300.0F, now + 31);
          attacks.tick(man, plan.clips(), plan.phase(1), List.of(vampire), 300.0F, now + 32);
          context.assertTrue(!attacks.busy(), "enough blows break the prayer");
          man.discard();
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void theFalseDawnIsSunOnlyToAVampireStandingInIt(final TestContext context) {
    final GrandInquisitorEntity man = inArena(context);
    final ServerPlayerEntity inside = TestPlayers.vampire(context, new BlockPos(2, 2, 2));
    final ServerPlayerEntity outside = TestPlayers.vampire(context, new BlockPos(6, 2, 6));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final ZoneAttack dawn =
              BossFights.attack(
                      context.getWorld().getRegistryManager(),
                      Identifier.of("nightborn", "inquisitor_false_dawn"))
                  .flatMap(attack -> attack.zone())
                  .orElseThrow();
          ZoneStrikes.harm(
              man,
              context.getWorld(),
              ZoneShape.circle(inside.getX(), inside.getZ(), dawn.radius()),
              inside.getY(),
              dawn.damage(),
              0.0F,
              dawn.hit());
          context.assertTrue(DirectSun.on(inside), "the vampire in the shaft stands in sun");
          context.assertTrue(!DirectSun.on(outside), "the one outside it stays in the dark");
          man.discard();
          TestPlayers.finish(context, inside, outside);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void drainedOnHisKneesHeEndsAndClearsTheVampiresName(final TestContext context) {
    final GrandInquisitorEntity man = inArena(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 3));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          man.hunt(vampire, false, List.of());
          hunted(vampire, man);
          man.damage(vampire.getDamageSources().playerAttack(vampire), 1.0F);
          man.timeUntilRegen = 0;
          downed(context, man);
          final InquisitorSettings settings = ServerConfigService.current().inquisitor();
          context.assertTrue(man.isAlive() && man.bitable(), "the last phase leaves him downed");
          context.assertTrue(
              man.getHealth() == settings.drainSeconds(),
              "he holds a sip of life for each second of the drain");
          man.timeUntilRegen = 0;
          context.assertTrue(
              !man.damage(vampire.getDamageSources().playerAttack(vampire), HUGE),
              "a vampire's blade does not end him on his knees");
          for (int sip = 0; sip < settings.drainSeconds() && man.isAlive(); sip++) {
            man.timeUntilRegen = 0;
            man.damage(NightbornDamage.bite(context.getWorld(), vampire), 2.0F);
          }
          context.assertTrue(!man.isAlive(), "a held bite drains him");
          final HuntRecord record = HuntAttachment.get(vampire);
          context.assertTrue(record.notoriety() == 0.0, "the vampire's notoriety is gone");
          context.assertTrue(record.phase() == HuntRecord.Phase.NONE, "the hunt is over");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void theHumanWhoRangEndsHimWithOneBlowAndChangesNoName(final TestContext context) {
    final GrandInquisitorEntity man = inArena(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 3));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          man.hunt(human, true, List.of(human));
          context.assertTrue(man.attacksOnSight(human), "he turns on the human who rang");
          vampire.setAttached(HuntAttachment.RECORD, HuntRecord.EMPTY.withNotoriety(NOTORIOUS));
          man.damage(vampire.getDamageSources().playerAttack(vampire), 1.0F);
          man.timeUntilRegen = 0;
          downed(context, man);
          man.timeUntilRegen = 0;
          man.damage(human.getDamageSources().playerAttack(human), 1.0F);
          context.assertTrue(!man.isAlive(), "any blow from that human ends him");
          context.assertTrue(
              HuntAttachment.get(vampire).notoriety() == NOTORIOUS, "nobody's notoriety changes");
          TestPlayers.finish(context, human, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void whenEveryFoeHasFallenHeLeavesAndTheHuntIsOver(final TestContext context) {
    final GrandInquisitorEntity man = inArena(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 3));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          man.hunt(vampire, false, List.of());
          hunted(vampire, man);
          man.tickFight();
          man.fallen(vampire);
          vampire.changeGameMode(GameMode.SPECTATOR);
          man.tickFight();
          context.assertTrue(man.isRemoved(), "he leaves and the wall falls");
          final HuntRecord record = HuntAttachment.get(vampire);
          context.assertTrue(record.phase() == HuntRecord.Phase.NONE, "the hunt is over");
          context.assertTrue(record.notoriety() == NOTORIOUS, "his leaving adds nothing");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void leavingAnEmptyArenaOnAHuntBringsTheSameManNextDusk(final TestContext context) {
    final GrandInquisitorEntity man = inArena(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 3));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          man.hunt(vampire, false, List.of());
          hunted(vampire, man);
          InquisitorOutcomes.leave(man, context.getWorld(), InquisitorEnding.EMPTY);
          context.assertTrue(man.isRemoved(), "he leaves");
          final HuntRecord record = HuntAttachment.get(vampire);
          context.assertTrue(
              record.phase() == HuntRecord.Phase.WARNING && record.warningsLeft() == 0,
              "he is due again at the next dusk");
          context.assertTrue(
              record.man().filter("Ezra"::equals).isPresent(), "the same man will come");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void theHuntersHeCallsFightTheHumanHeTurnedOnAndLeaveNothing(final TestContext context) {
    final GrandInquisitorEntity man = inArena(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 1));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          man.hunt(human, true, List.of());
          final BossWard ward = new BossWard();
          ward.raise(
              man,
              context.getWorld(),
              BossFights.attack(
                      context.getWorld().getRegistryManager(),
                      Identifier.of("nightborn", "inquisitor_call"))
                  .flatMap(attack -> attack.ward())
                  .orElseThrow(),
              context.getWorld().getTime());
          final List<HunterEntity> called =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      HunterEntity.class, context.getTestBox().expand(4.0), BossWard::keeper);
          called.forEach(hunter -> hunter.setAiDisabled(true));
          context.assertTrue(called.size() == 2, "he calls two hunters, not " + called.size());
          called.forEach(
              hunter -> {
                context.assertTrue(hunter.canTarget(human), "a called hunter fights the human");
                context.assertTrue(BossWard.keeper(hunter), "a called hunter goes with the fight");
              });
          ward.drop();
          context.assertTrue(
              called.stream().allMatch(HunterEntity::isRemoved), "they go when the fight ends");
          man.discard();
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void calledHuntersFightOnThroughAChangeOfPhase(final TestContext context) {
    final GrandInquisitorEntity man = inquisitor(context, new BlockPos(1, 2, 1));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(7, 2, 7));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final BossFightPlan plan =
              BossFights.fight(context.getWorld().getRegistryManager(), GrandInquisitorEntity.FIGHT)
                  .orElseThrow();
          final BossAttacks attacks = new BossAttacks();
          final long now = context.getWorld().getTime();
          man.setTarget(vampire);
          attacks.counter(
              man, List.of(Identifier.of("nightborn", "inquisitor_call")), List.of(vampire), now);
          for (long tick = now; tick <= now + 30; tick++) {
            attacks.tick(man, plan.clips(), plan.phase(0), List.of(vampire), 300.0F, tick);
          }
          final List<HunterEntity> called =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      HunterEntity.class, context.getTestBox().expand(4.0), BossWard::keeper);
          called.forEach(hunter -> hunter.setAiDisabled(true));
          context.assertTrue(called.size() == 2, "he calls two hunters, not " + called.size());
          attacks.interrupt();
          context.assertTrue(
              called.stream().noneMatch(HunterEntity::isRemoved),
              "they fight on when his phase changes");
          attacks.cancel();
          context.assertTrue(
              called.stream().allMatch(HunterEntity::isRemoved), "they go when the fight ends");
          man.discard();
          TestPlayers.finish(context, vampire);
        });
  }
}
