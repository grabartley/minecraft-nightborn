package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.coffin.CoffinOwnersState;
import com.grahambartley.nightborn.coffin.CoffinOwnership;
import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.server.ConfigEditPermission;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class CoffinOwnershipGameTest implements FabricGameTest {

  static final String BATCH = "coffin-ownership";
  static final String NIGHT = "coffin-ownership-night";
  static final int WAKE_WINDOW = 60;
  static final float BLAST_POWER = 3.0F;

  @BeforeBatch(batchId = BATCH)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = BATCH)
  public void releaseNoon(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void restingClaimsAFreeCoffinAndFreesTheOneBefore(final TestContext context) {
    final BlockPos first = CoffinStage.coffin(context, new BlockPos(1, 2, 6), Direction.EAST);
    final BlockPos second = CoffinStage.coffin(context, new BlockPos(5, 2, 6), Direction.EAST);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(first, vampire);
          context.assertEquals(
              owner(context, first), Optional.of(vampire.getUuid()), "the first rest claims it");
          CoffinRise.wakeAtOnce(vampire);
          context.useBlock(second, vampire);
          context.assertEquals(
              owner(context, second), Optional.of(vampire.getUuid()), "the second rest claims it");
          context.assertEquals(owner(context, first), Optional.empty(), "the first is freed");
          context.assertEquals(
              vampire.getSpawnPointPosition(),
              context.getAbsolutePos(second),
              "the respawn point follows the claimed coffin");
          TestPlayers.finish(context, vampire);
        });
  }

  @BeforeBatch(batchId = NIGHT)
  public void pinMidnight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = NIGHT)
  public void releaseMidnight(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = NIGHT, tickLimit = TestPlayers.READY + 10)
  public void aRefusedRestClaimsNothingAndKeepsTheCoffinHeldBefore(final TestContext context) {
    final BlockPos held = CoffinStage.coffin(context, new BlockPos(1, 2, 6), Direction.EAST);
    final BlockPos tried = CoffinStage.coffin(context, new BlockPos(5, 2, 6), Direction.EAST);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    claim(context, held, vampire.getUuid());
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(tried, vampire);
          context.assertFalse(vampire.isSleeping(), "a vampire cannot rest at night");
          context.assertEquals(owner(context, tried), Optional.empty(), "the coffin stays free");
          context.assertEquals(
              owner(context, held), Optional.of(vampire.getUuid()), "the old coffin is kept");
          context.assertTrue(
              vampire.getSpawnPointPosition() == null, "the refused coffin sets no respawn");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void restingInAnothersCoffinClaimsNothing(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.EAST);
    final UUID other = UUID.randomUUID();
    CoffinOwnership.claim(context.getWorld(), context.getAbsolutePos(head), other);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, vampire);
          context.assertTrue(vampire.isSleeping(), "a vampire may rest in another's coffin");
          context.assertEquals(owner(context, head), Optional.of(other), "the owner keeps it");
          context.assertTrue(
              vampire.getSpawnPointPosition() == null, "the borrower's respawn point is unchanged");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void onlyTheOwnerACreativePlayerOrAnOperatorBreaksAnAssignedCoffin(
      final TestContext context) {
    final BlockPos strangers = CoffinStage.coffin(context, new BlockPos(1, 2, 2), Direction.EAST);
    final BlockPos creatives = CoffinStage.coffin(context, new BlockPos(1, 2, 6), Direction.EAST);
    final BlockPos operators = CoffinStage.coffin(context, new BlockPos(5, 2, 2), Direction.EAST);
    final ServerPlayerEntity stranger = TestPlayers.human(context, new BlockPos(4, 2, 4));
    final ServerPlayerEntity builder = TestPlayers.human(context, new BlockPos(4, 2, 5));
    final ServerPlayerEntity operator = TestPlayers.human(context, new BlockPos(5, 2, 4));
    final UUID[] owners = {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          claim(context, strangers, owners[0]);
          claim(context, creatives, owners[1]);
          claim(context, operators, owners[2]);
          context.assertFalse(
              stranger.interactionManager.tryBreakBlock(context.getAbsolutePos(strangers)),
              "a survival stranger cannot break it");
          context.expectBlock(CoffinStage.darkOak(), strangers);
          context.assertEquals(
              owner(context, strangers), Optional.of(owners[0]), "the coffin stays assigned");
          builder.changeGameMode(GameMode.CREATIVE);
          context.assertTrue(
              builder.interactionManager.tryBreakBlock(context.getAbsolutePos(creatives)),
              "a creative player can");
          context.assertEquals(owner(context, creatives), Optional.empty(), "and it is freed");
          final var players = context.getWorld().getServer().getPlayerManager();
          players
              .getOpList()
              .add(
                  new OperatorEntry(
                      operator.getGameProfile(), ConfigEditPermission.OPERATOR_LEVEL, false));
          try {
            context.assertTrue(
                operator.interactionManager.tryBreakBlock(context.getAbsolutePos(operators)),
                "an operator can");
          } finally {
            players.getOpList().remove(operator.getGameProfile());
          }
          context.assertEquals(owner(context, operators), Optional.empty(), "and it is freed");
          TestPlayers.finish(context, stranger, builder, operator);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void theOwnerBreakingTheirCoffinFreesItAndTheirRespawn(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.EAST);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, vampire);
          CoffinRise.wakeAtOnce(vampire);
          context.assertTrue(
              vampire.interactionManager.tryBreakBlock(context.getAbsolutePos(head)),
              "the owner breaks their own coffin");
          context.assertEquals(owner(context, head), Optional.empty(), "the coffin is freed");
          context.assertEquals(
              CoffinOwnersState.get(context.getWorld().getServer()).coffinOf(vampire.getUuid()),
              Optional.empty(),
              "the vampire holds no coffin");
          context.assertTrue(vampire.getSpawnPointPosition() == null, "the respawn is cleared");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = CoffinStage.YARD,
      batchId = BATCH,
      tickLimit = TestPlayers.READY + WAKE_WINDOW + 10)
  public void theOwnerBreakingTheirCoffinWakesItsSleeperAndFreesItOnce(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.EAST);
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity sleeper = TestPlayers.vampire(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, owner);
          CoffinRise.wakeAtOnce(owner);
          context.useBlock(head, sleeper);
          context.assertTrue(sleeper.isSleeping(), "the other vampire lies in the coffin");
          context.assertTrue(
              owner.interactionManager.tryBreakBlock(context.getAbsolutePos(head)),
              "the owner breaks it while it is occupied");
          context.assertEquals(owner(context, head), Optional.empty(), "the coffin is freed");
          context.assertEquals(
              CoffinOwnersState.get(context.getWorld().getServer()).coffinOf(sleeper.getUuid()),
              Optional.empty(),
              "the sleeper never gains it");
        });
    context.runAtTick(
        TestPlayers.READY + WAKE_WINDOW,
        () -> {
          context.assertFalse(
              sleeper.isSleeping() || CoffinSleepers.inCoffin(sleeper),
              "the sleeper is woken: " + Diagnostics.of(sleeper));
          TestPlayers.finish(context, owner, sleeper);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void explosionsLeaveAnAssignedCoffinOutOfTheBlastAndAWitherCannotBreakIt(
      final TestContext context) {
    final BlockPos assigned = CoffinStage.coffin(context, new BlockPos(1, 2, 4), Direction.EAST);
    final BlockPos free = CoffinStage.coffin(context, new BlockPos(5, 2, 4), Direction.WEST);
    final BlockPos struck = CoffinStage.coffin(context, new BlockPos(3, 2, 7), Direction.EAST);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          claim(context, assigned, UUID.randomUUID());
          claim(context, struck, UUID.randomUUID());
          final ServerWorld world = context.getWorld();
          final Vec3d center = context.getAbsolute(new Vec3d(3.5, 2.5, 4.5));
          final Explosion blast =
              world.createExplosion(
                  null, center.x, center.y, center.z, BLAST_POWER, World.ExplosionSourceType.TNT);
          context.assertTrue(
              blast.getAffectedBlocks().contains(context.getAbsolutePos(free)),
              "the blast reaches the free coffin");
          context.assertFalse(
              blast.getAffectedBlocks().contains(context.getAbsolutePos(assigned))
                  || blast.getAffectedBlocks().contains(context.getAbsolutePos(assigned.west())),
              "the assigned coffin is left out of the blast every client replays");
          final CreeperEntity creeper =
              context.spawnEntity(EntityType.CREEPER, new BlockPos(3, 2, 2));
          world.createExplosion(
              creeper, center.x, center.y, center.z, BLAST_POWER, World.ExplosionSourceType.MOB);
          creeper.discard();
          context.expectBlock(CoffinStage.darkOak(), assigned);
          context.expectBlock(CoffinStage.darkOak(), assigned.west());
          context.expectBlock(Blocks.AIR, free);
          final WitherEntity wither = EntityType.WITHER.create(world);
          context.assertFalse(
              world.breakBlock(context.getAbsolutePos(struck), true, wither),
              "a wither cannot break an assigned coffin");
          context.expectBlock(CoffinStage.darkOak(), struck);
          context.complete();
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = BATCH, tickLimit = TestPlayers.READY + 20)
  public void aPistonCannotMoveOrDestroyAnAssignedCoffin(final TestContext context) {
    final BlockPos assigned = CoffinStage.coffin(context, new BlockPos(2, 2, 2), Direction.EAST);
    final BlockPos free = CoffinStage.coffin(context, new BlockPos(2, 2, 6), Direction.EAST);
    for (final BlockPos head : new BlockPos[] {assigned, free}) {
      context.setBlockState(
          head.east(), Blocks.PISTON.getDefaultState().with(PistonBlock.FACING, Direction.WEST));
    }
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          claim(context, assigned, UUID.randomUUID());
          context.setBlockState(assigned.east(2), Blocks.REDSTONE_BLOCK);
          context.setBlockState(free.east(2), Blocks.REDSTONE_BLOCK);
        });
    context.runAtTick(
        TestPlayers.READY + 10,
        () -> {
          context.expectBlock(CoffinStage.darkOak(), assigned);
          context.expectBlock(CoffinStage.darkOak(), assigned.west());
          context.expectBlock(Blocks.PISTON_HEAD, free);
          context.expectBlock(Blocks.AIR, free.west());
          context.checkBlockState(
              assigned.east(),
              state -> !state.get(PistonBlock.EXTENDED),
              () -> "the piston stays retracted");
          context.complete();
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void theSunAStakeAndACureFreeACoffinAndAnyOtherDeathKeepsIt(final TestContext context) {
    final BlockPos[] heads = {
      CoffinStage.coffin(context, new BlockPos(1, 2, 1), Direction.SOUTH),
      CoffinStage.coffin(context, new BlockPos(4, 2, 1), Direction.SOUTH),
      CoffinStage.coffin(context, new BlockPos(7, 2, 1), Direction.SOUTH),
      CoffinStage.coffin(context, new BlockPos(1, 2, 5), Direction.SOUTH)
    };
    final ServerPlayerEntity burned = TestPlayers.vampire(context, new BlockPos(1, 2, 3));
    final ServerPlayerEntity staked = TestPlayers.vampire(context, new BlockPos(4, 2, 3));
    final ServerPlayerEntity cured = TestPlayers.vampire(context, new BlockPos(7, 2, 3));
    final ServerPlayerEntity killed = TestPlayers.vampire(context, new BlockPos(1, 2, 7));
    final ServerPlayerEntity[] vampires = {burned, staked, cured, killed};
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          for (int i = 0; i < vampires.length; i++) {
            context.useBlock(heads[i], vampires[i]);
            CoffinRise.wakeAtOnce(vampires[i]);
            context.assertEquals(
                owner(context, heads[i]),
                Optional.of(vampires[i].getUuid()),
                "resting claims the coffin");
          }
          burned.damage(NightbornDamage.sun(context.getWorld()), Float.MAX_VALUE);
          staked.damage(NightbornDamage.stake(context.getWorld(), null), Float.MAX_VALUE);
          VampireTransitions.makeHuman(cured);
          killed.kill();
          for (int i = 0; i < 3; i++) {
            context.assertEquals(
                owner(context, heads[i]),
                Optional.empty(),
                "the coffin is freed: " + Diagnostics.of(vampires[i]));
            context.assertTrue(
                vampires[i].getSpawnPointPosition() == null, "no coffin respawn is kept");
          }
          context.assertTrue(killed.isDead(), "the last vampire died");
          context.assertEquals(
              owner(context, heads[3]), Optional.of(killed.getUuid()), "any other death keeps it");
          final ServerPlayerEntity respawned =
              context
                  .getWorld()
                  .getServer()
                  .getPlayerManager()
                  .respawnPlayer(killed, false, Entity.RemovalReason.KILLED);
          context.assertTrue(
              respawned.getPos().isInRange(Vec3d.ofCenter(context.getAbsolutePos(heads[3])), 3.0),
              "the vampire respawns at its coffin: " + respawned.getPos());
          TestPlayers.finish(context, burned, staked, cured, respawned);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aRespawnPointInACoffinNoLongerYoursFallsBackToWorldSpawn(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.EAST);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, vampire);
          CoffinRise.wakeAtOnce(vampire);
          CoffinOwnersState.get(context.getWorld().getServer()).release(vampire.getUuid());
          claim(context, head, UUID.randomUUID());
          context.assertEquals(
              vampire.getSpawnPointPosition(),
              context.getAbsolutePos(head),
              "the respawn point still names the coffin, as for an owner who was offline");
          vampire.getRespawnTarget(true, TeleportTarget.NO_OP);
          context.assertTrue(
              vampire.getSpawnPointPosition() == null,
              "a coffin owned by another is no respawn point");
          TestPlayers.finish(context, vampire);
        });
  }

  private static void claim(final TestContext context, final BlockPos head, final UUID owner) {
    CoffinOwnership.claim(context.getWorld(), context.getAbsolutePos(head), owner);
  }

  private static Optional<UUID> owner(final TestContext context, final BlockPos head) {
    return CoffinOwnership.ownerAt(context.getWorld(), context.getAbsolutePos(head));
  }
}
