package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.boss.BossFightPlan;
import com.grahambartley.nightborn.boss.BossFights;
import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.boss.ZoneShape;
import com.grahambartley.nightborn.boss.ZoneStrikes;
import com.grahambartley.nightborn.config.RitualSettings;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EldestFightGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final BlockPos CENTRE = EldestGameTest.CENTRE;
  static final float FIRST_FLOOR = 0.70F;
  static final float LAST_SHARE = 0.35F;
  static final float HUGE = 100_000.0F;

  private static EldestEntity inArena(final TestContext context) {
    final EldestEntity eldest = EldestGameTest.eldest(context);
    eldest.claimArena(new Arena(context.getAbsolutePos(CENTRE), Arena.SMALLEST));
    return eldest;
  }

  private static void atPhase(final EldestEntity eldest, final int phase) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putInt("BossPhase", phase);
    eldest.fight().read(nbt);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theShippedFightHasThreePhasesWhoseAttacksAllLoad(final TestContext context) {
    final var registries = context.getWorld().getRegistryManager();
    final BossFightPlan plan = BossFights.fight(registries, EldestEntity.FIGHT).orElse(null);
    context.assertTrue(plan != null, "the Eldest's fight loaded");
    context.assertTrue(plan.phases().size() == 3, "it has " + plan.phases().size() + " phases");
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
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void theBossBarShowsOnlyToPlayersInsideTheArena(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final ServerPlayerEntity player = TestPlayers.human(context, new BlockPos(4, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          eldest.tickFight();
          context.assertTrue(
              eldest.presence().bar().getPlayers().contains(player),
              "a player inside the arena sees the bar");
          final Vec3d above = context.getAbsolute(new Vec3d(4.5, 2.0 + Arena.ABOVE + 4.0, 2.5));
          player.refreshPositionAndAngles(above.x, above.y, above.z, 0.0F, 0.0F);
          eldest.tickFight();
          context.assertTrue(
              !eldest.presence().bar().getPlayers().contains(player),
              "a player who leaves the arena loses the bar");
          eldest.discard();
          TestPlayers.finish(context, player);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aBlowCannotCarryPastAPhaseAndTheChangeShrugsOffTheNext(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final float floor = eldest.getMaxHealth() * FIRST_FLOOR;
    final boolean hurt = eldest.damage(context.getWorld().getDamageSources().generic(), HUGE);
    context.assertTrue(hurt, "the blow lands");
    context.assertTrue(
        Math.abs(eldest.getHealth() - floor) <= 0.51F,
        "it stops at the first phase's floor of " + floor + ", not " + eldest.getHealth());
    context.assertTrue(eldest.fight().phase() == 1, "it is in phase " + eldest.fight().phase());
    eldest.timeUntilRegen = 0;
    context.assertTrue(
        !eldest.damage(context.getWorld().getDamageSources().generic(), 5.0F),
        "it cannot be hurt while it changes phase");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theLastPhaseEndsDownedAndItRisesInsteadOfEscaping(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final RitualSettings settings = ServerConfigService.current().ritual();
    atPhase(eldest, 2);
    eldest.setHealth(eldest.getMaxHealth() * 0.2F);
    eldest.damage(context.getWorld().getDamageSources().generic(), HUGE);
    final DownedState downed = Downed.state(eldest);
    context.assertTrue(downed != null && downed.isDowned(), "the last phase ends with it downed");
    context.assertTrue(eldest.isAlive(), "only a stake or the sun ends it");
    eldest.tickFight();
    context.assertTrue(
        Downed.state(eldest).ticksLeft() == settings.stakeWindowSeconds() * 20,
        "hunters get the stake window, not " + Downed.state(eldest).ticksLeft() + " ticks");
    eldest.setAttached(Downed.TYPE, Downed.state(eldest).next(DownedState.Phase.ESCAPING, 100));
    eldest.tickFight();
    final float risen = eldest.getMaxHealth() * LAST_SHARE * settings.riseHealth() / 100.0F;
    context.assertTrue(Downed.state(eldest) == null, "it does not escape from its arena");
    context.assertTrue(
        Math.abs(eldest.getHealth() - risen) < 0.01F,
        "it rises with " + risen + ", not " + eldest.getHealth());
    context.assertTrue(eldest.fight().phase() == 2, "it rises into its last phase");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 300)
  public void anArenaLeftEmptyResetsTheFight(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final int emptyTicks = ServerConfigService.current().ritual().arenaEmptyTicks();
    eldest.damage(context.getWorld().getDamageSources().generic(), HUGE);
    eldest.tickFight();
    context.assertTrue(eldest.fight().phase() == 1, "the fight is under way");
    context.runAtTick(
        emptyTicks / 2,
        () -> {
          eldest.tickFight();
          context.assertTrue(eldest.fight().phase() == 1, "it holds its phase until time is up");
        });
    context.runAtTick(
        emptyTicks + 5L,
        () -> {
          eldest.tickFight();
          context.assertTrue(eldest.fight().phase() == 0, "it starts again from its first phase");
          context.assertTrue(eldest.getHealth() == eldest.getMaxHealth(), "at full health");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aFightLeftForLongerThanTheEmptyTimeResetsOnLoad(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    eldest.damage(context.getWorld().getDamageSources().generic(), HUGE);
    eldest.tickFight();
    final NbtCompound saved = new NbtCompound();
    eldest.fight().write(saved);
    saved.putLong("BossLastOccupied", context.getWorld().getTime() - 100_000L);
    eldest.fight().read(saved);
    context.assertTrue(eldest.fight().phase() == 1, "the saved fight is half done");
    eldest.tickFight();
    context.assertTrue(eldest.fight().phase() == 0, "coming back long after finds it reset");
    context.assertTrue(eldest.getHealth() == eldest.getMaxHealth(), "at full health");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theStakeWindowIsNotGivenAgainAfterALoad(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    atPhase(eldest, 2);
    eldest.setHealth(eldest.getMaxHealth() * 0.2F);
    eldest.damage(context.getWorld().getDamageSources().generic(), HUGE);
    eldest.tickFight();
    eldest.setAttached(Downed.TYPE, Downed.state(eldest).next(DownedState.Phase.DOWNED, 40));
    final NbtCompound saved = new NbtCompound();
    eldest.fight().write(saved);
    eldest.fight().read(saved);
    eldest.tickFight();
    context.assertTrue(
        Downed.state(eldest).ticksLeft() == 40,
        "the window keeps running down, it shows " + Downed.state(eldest).ticksLeft());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void aStakeCannotSkipAPhaseOfAStandingEldest(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(4, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          hunter.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.WOODEN_STAKE));
          final float floor = eldest.getMaxHealth() * FIRST_FLOOR;
          eldest.damage(context.getWorld().getDamageSources().playerAttack(hunter), HUGE);
          context.assertTrue(eldest.isAlive(), "a stake does not end a standing Eldest");
          context.assertTrue(Downed.state(eldest) == null, "nor down it");
          context.assertTrue(
              Math.abs(eldest.getHealth() - floor) <= 0.51F,
              "the blow stops at the first phase's floor of " + floor + ": " + eldest.getHealth());
          eldest.timeUntilRegen = 0;
          context.assertTrue(
              !eldest.damage(context.getWorld().getDamageSources().playerAttack(hunter), 5.0F),
              "and a stake cannot hurt it while it changes phase");
          eldest.discard();
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void aBlowFromOutsideTheArenaDoesNothing(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final ServerPlayerEntity player = TestPlayers.human(context, new BlockPos(4, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final Vec3d inside = player.getPos();
          final Vec3d above = context.getAbsolute(new Vec3d(4.5, 2.0 + Arena.ABOVE + 4.0, 2.5));
          player.refreshPositionAndAngles(above.x, above.y, above.z, 0.0F, 0.0F);
          context.assertTrue(
              !eldest.damage(context.getWorld().getDamageSources().playerAttack(player), 4.0F),
              "a blow from outside the arena is shrugged off");
          context.assertTrue(eldest.fighters().isEmpty(), "and does not join the fight");
          player.refreshPositionAndAngles(inside.x, inside.y, inside.z, 0.0F, 0.0F);
          context.assertTrue(
              eldest.damage(context.getWorld().getDamageSources().playerAttack(player), 4.0F),
              "the same blow from inside lands");
          eldest.discard();
          TestPlayers.finish(context, player);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aMarkedZoneHurtsOnlyThoseStandingInIt(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final PigEntity marked = context.spawnMob(EntityType.PIG, new BlockPos(2, 2, 2));
    final PigEntity clear = context.spawnMob(EntityType.PIG, new BlockPos(6, 2, 6));
    marked.setAiDisabled(true);
    clear.setAiDisabled(true);
    ZoneStrikes.harm(
        eldest,
        context.getWorld(),
        ZoneShape.circle(marked.getX(), marked.getZ(), 1.5),
        marked.getY(),
        4.0F,
        0.0F);
    context.assertTrue(marked.getHealth() < marked.getMaxHealth(), "the one in the mark is hurt");
    context.assertTrue(clear.getHealth() == clear.getMaxHealth(), "the one outside is not");
    context.assertTrue(
        eldest.getHealth() == eldest.getMaxHealth(), "the Eldest does not hurt itself");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void aMarkCoversItsShapeAndGoesWhenItsTimeIsUp(final TestContext context) {
    final Vec3d at = context.getAbsolute(new Vec3d(4.5, 2.0, 4.5));
    final ZoneMarkEntity mark =
        ZoneMarkEntity.place(context.getWorld(), ZoneShape.circle(at.x, at.z, 2.5), at.y);
    context.assertTrue(mark.radius() == 2.5F && !mark.line(), "it is a circle of 2.5 blocks");
    context.assertTrue(!mark.lingering(), "it starts as a warning");
    mark.linger(10);
    context.assertTrue(mark.lingering(), "then lingers");
    context.runAtTick(5, () -> context.assertTrue(mark.isAlive(), "it lasts its time"));
    context.runAtTick(
        20,
        () -> {
          context.assertTrue(mark.isRemoved(), "and is gone when its time is up");
          context.complete();
        });
  }
}
