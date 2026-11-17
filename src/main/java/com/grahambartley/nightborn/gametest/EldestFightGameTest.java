package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.boss.BossFightPlan;
import com.grahambartley.nightborn.boss.BossFights;
import com.grahambartley.nightborn.boss.BossWard;
import com.grahambartley.nightborn.boss.WardAttack;
import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.boss.ZoneShape;
import com.grahambartley.nightborn.boss.ZoneStrikes;
import com.grahambartley.nightborn.config.RitualSettings;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireBatEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public class EldestFightGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final BlockPos CENTRE = EldestGameTest.CENTRE;
  static final float FIRST_FLOOR = 0.70F;
  static final float LAST_SHARE = 0.35F;
  static final float HUGE = 100_000.0F;
  static final float FIRST_MAX_HIT = 14.0F;
  static final int FIRST_GUARD_HITS = 4;
  static final int CHANGE_TICKS = 80;

  private static EldestEntity inArena(final TestContext context) {
    final EldestEntity eldest = EldestGameTest.eldest(context);
    eldest.claimArena(new Arena(context.getAbsolutePos(CENTRE), Arena.SMALLEST));
    return eldest;
  }

  private static void endFirstPhase(final TestContext context, final EldestEntity eldest) {
    eldest.setHealth(eldest.getMaxHealth() * FIRST_FLOOR + 5.0F);
    eldest.damage(context.getWorld().getDamageSources().generic(), HUGE);
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
          final Vec3d inside = player.getPos();
          final Vec3d above = context.getAbsolute(new Vec3d(4.5, 2.0 + Arena.ABOVE + 4.0, 2.5));
          player.refreshPositionAndAngles(above.x, above.y, above.z, 0.0F, 0.0F);
          eldest.tickFight();
          context.assertTrue(
              !eldest.presence().bar().getPlayers().contains(player),
              "a player outside the arena sees no bar");
          player.refreshPositionAndAngles(inside.x, inside.y, inside.z, 0.0F, 0.0F);
          eldest.tickFight();
          context.assertTrue(
              eldest.presence().bar().getPlayers().contains(player),
              "a player inside the arena sees the bar");
          player.changeGameMode(GameMode.CREATIVE);
          player.refreshPositionAndAngles(above.x, above.y, above.z, 0.0F, 0.0F);
          eldest.tickFight();
          context.assertTrue(
              !eldest.presence().bar().getPlayers().contains(player),
              "a player the wall does not hold loses the bar when they leave");
          eldest.discard();
          TestPlayers.finish(context, player);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aBlowCannotCarryPastAPhaseAndTheChangeShrugsOffTheNext(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final float floor = eldest.getMaxHealth() * FIRST_FLOOR;
    eldest.setHealth(floor + 5.0F);
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
    eldest.setHealth(5.0F);
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
    endFirstPhase(context, eldest);
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
    endFirstPhase(context, eldest);
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
    eldest.setHealth(5.0F);
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
          eldest.setHealth(floor + 5.0F);
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
  public void noSingleBlowLandsHarderThanThePhaseAllows(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final float before = eldest.getHealth();
    eldest.damage(context.getWorld().getDamageSources().generic(), HUGE);
    final float lost = before - eldest.getHealth();
    context.assertTrue(lost > 0.0F && lost <= FIRST_MAX_HIT, "a huge blow takes only " + lost);
    context.assertTrue(eldest.fight().phase() == 0, "and the phase goes on");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void aRunOfBlowsIsGuardedAndThrowsTheHunterBack(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(4, 2, 3));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final var blow = context.getWorld().getDamageSources().playerAttack(hunter);
          for (int hit = 0; hit < FIRST_GUARD_HITS; hit++) {
            eldest.timeUntilRegen = 0;
            context.assertTrue(eldest.damage(blow, 2.0F), "blow " + (hit + 1) + " lands");
          }
          final float before = eldest.getHealth();
          eldest.timeUntilRegen = 0;
          context.assertTrue(!eldest.damage(blow, 2.0F), "the next blow is guarded");
          context.assertTrue(eldest.getHealth() == before, "and takes nothing");
          context.assertTrue(
              hunter.getVelocity().horizontalLengthSquared() > 0.0, "the hunter is thrown back");
          eldest.timeUntilRegen = 0;
          context.assertTrue(eldest.damage(blow, 2.0F), "then blows land again");
          eldest.discard();
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void theEldestGrowsWithEachPhaseAndItsEyesBlazeInTheLast(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    eldest.tickFight();
    final double armor = eldest.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
    final float scale = eldest.getScale();
    context.assertTrue(!eldest.eyesBlaze(), "its eyes are dark in the first phase");
    endFirstPhase(context, eldest);
    eldest.tickFight();
    context.assertTrue(eldest.getScale() == scale, "it starts the change at its old size");
    context.runAtTick(
        CHANGE_TICKS + 2L,
        () -> {
          eldest.tickFight();
          context.assertTrue(
              eldest.getAttributeValue(EntityAttributes.GENERIC_ARMOR) > armor,
              "the second phase is better armoured");
          context.assertTrue(
              eldest.getScale() > scale, "and bigger: " + scale + " to " + eldest.getScale());
          final float second = eldest.getScale();
          atPhase(eldest, 2);
          eldest.tickFight();
          context.assertTrue(eldest.getScale() > second, "the last phase is bigger again");
          context.assertTrue(eldest.eyesBlaze(), "and its eyes blaze");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void itReturnsToWhereItBeganWhenAPhaseEnds(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    eldest.tickFight();
    final Vec3d began = eldest.getPos();
    final Vec3d corner = context.getAbsolute(new Vec3d(6.5, 2.0, 6.5));
    eldest.refreshPositionAndAngles(corner.x, corner.y, corner.z, 0.0F, 0.0F);
    endFirstPhase(context, eldest);
    context.assertTrue(
        eldest.getPos().squaredDistanceTo(began) < 1.0,
        "it is back at " + began + ", not " + eldest.getPos());
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aWardHoldsUntilItsKeepersAreSlain(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final BossWard ward = new BossWard();
    final long now = context.getWorld().getTime();
    ward.raise(
        eldest,
        context.getWorld(),
        new WardAttack(
            Registries.ENTITY_TYPE.getEntry(NightbornMobs.VAMPIRE_BAT), 3, 2.0F, 500, true),
        now);
    final List<VampireBatEntity> keepers =
        context
            .getWorld()
            .getEntitiesByClass(
                VampireBatEntity.class, context.getTestBox().expand(2.0), bat -> true);
    keepers.forEach(keeper -> keeper.setAiDisabled(true));
    context.assertTrue(keepers.size() == 3, "three keepers appear, not " + keepers.size());
    context.assertTrue(keepers.stream().allMatch(VampireBatEntity::isGlowing), "marked out");
    context.assertTrue(ward.holds(now), "the ward holds while they live");
    keepers.get(0).kill();
    keepers.get(1).kill();
    context.assertTrue(ward.holds(now), "and while one of them lives");
    keepers.get(2).kill();
    context.assertTrue(!ward.holds(now), "and falls with the last of them");
    context.complete();
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

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void anEldestLoadedWeightlessOutsideItsFlyingPhaseComesBackToTheGround(
      final TestContext context) {
    final EldestEntity eldest = inArena(context);
    eldest.setNoGravity(true);
    eldest.tickFight();
    context.assertFalse(
        eldest.hasNoGravity(), "an Eldest that is not flying has its weight back after a load");
    context.assertFalse(eldest.fight().flying(), "and it does not count as flying");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void theWallPutsBackAPlayerWhoLeavesTheArena(final TestContext context) {
    final EldestEntity eldest = inArena(context);
    final ServerPlayerEntity player = TestPlayers.human(context, new BlockPos(4, 2, 3));
    final Arena arena = eldest.fight().arena().orElseThrow();
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          eldest.tickFight();
          final Vec3d outside = context.getAbsolute(new Vec3d(4.5, 2.0, 4.5)).add(9.0, 0.0, 0.0);
          player.setPosition(outside);
          context.assertFalse(arena.holds(outside.x, outside.y, outside.z), "the spot is outside");
          eldest.tickFight();
          context.assertTrue(
              arena.holds(player.getX(), player.getY(), player.getZ()),
              "the wall put the player back inside, not at " + player.getPos());
          context.complete();
        });
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
