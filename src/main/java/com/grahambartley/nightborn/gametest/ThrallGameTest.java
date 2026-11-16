package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.FeedingSettings;
import com.grahambartley.nightborn.config.MindSettings;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.mind.Gazes;
import com.grahambartley.nightborn.mind.Mesmerise;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.thrall.BindRules;
import com.grahambartley.nightborn.thrall.ThrallEntity;
import com.grahambartley.nightborn.thrall.ThrallMode;
import com.grahambartley.nightborn.thrall.ThrallRegistry;
import com.grahambartley.nightborn.thrall.Thralls;
import com.grahambartley.nightborn.thrall.goal.ThrallTargetGoal;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillagerProfession;

public class ThrallGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "thralls";
  static final int FIRST_DRINK = TestPlayers.READY + 25;

  @BeforeBatch(batchId = BATCH)
  public void certainBonds(final ServerWorld world) {
    FeedingSessions.clearAll();
    Gazes.clearAll();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(MindSettings.THRALL_CHANCE, 100)
            .with(FeedingSettings.RESIST_CHANCE, 0));
  }

  @AfterBatch(batchId = BATCH)
  public void restoreBonds(final ServerWorld world) {
    FeedingSessions.clearAll();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(MindSettings.THRALL_CHANCE, MindSettings.THRALL_CHANCE.defaultValue())
            .with(FeedingSettings.RESIST_CHANCE, FeedingSettings.RESIST_CHANCE.defaultValue()));
  }

  private static VillagerEntity villager(final TestContext context, final BlockPos at) {
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, at);
    villager.setAiDisabled(true);
    villager.setVillagerData(villager.getVillagerData().withProfession(VillagerProfession.FARMER));
    return villager;
  }

  private static ThrallEntity bound(
      final TestContext context, final ServerPlayerEntity owner, final BlockPos at) {
    final ThrallEntity thrall = context.spawnEntity(Thralls.THRALL, at);
    thrall.bind(owner.getUuid(), thrall.getVillagerData());
    ThrallRegistry.of(context.getWorld().getServer()).bind(owner.getUuid(), thrall.getUuid());
    return thrall;
  }

  private static List<ThrallEntity> thrallsNear(final TestContext context, final BlockPos at) {
    final Vec3d centre = context.getAbsolute(Vec3d.ofBottomCenter(at));
    return context
        .getWorld()
        .getEntitiesByClass(ThrallEntity.class, new Box(centre, centre).expand(2.0), t -> true);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = FIRST_DRINK + 10)
  public void sippingAVillagerYouMesmerisedBindsItAsYourThrall(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final BlockPos at = new BlockPos(4, 2, 4);
    final VillagerEntity villager = villager(context, at);
    NobleSkills.learn(vampire, NobleSkills.MESMERISE, NobleSkills.BLOOD_BOND);
    context.runAtTick(
        TestPlayers.READY, () -> Mesmerise.land(vampire, villager, NobleSkills.power(context)));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= TestPlayers.READY && context.getTick() < FIRST_DRINK) {
            vampire.setSneaking(true);
            UseEntityCallback.EVENT
                .invoker()
                .interact(vampire, context.getWorld(), Hand.MAIN_HAND, villager, null);
          }
        });
    context.runAtTick(
        FIRST_DRINK,
        () -> {
          context.assertTrue(villager.getHealth() < villager.getMaxHealth(), "the vampire sipped");
          FeedingSessions.end(vampire);
          final List<ThrallEntity> thralls = thrallsNear(context, at);
          context.assertTrue(villager.isRemoved(), "the villager became something else");
          context.assertEquals(thralls.size(), 1, "one thrall stands where the villager was");
          context.assertTrue(thralls.get(0).isOwnedBy(vampire), "the thrall serves the vampire");
          context.assertEquals(
              thralls.get(0).getVillagerData().getProfession(),
              VillagerProfession.FARMER,
              "the thrall keeps the villager's look");
          context.assertEquals(
              ThrallRegistry.of(context.getWorld().getServer()).count(vampire.getUuid()),
              1,
              "the vampire keeps one thrall");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 5)
  public void aVampireAtTheirThrallLimitBindsNoMore(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final VillagerEntity villager = villager(context, new BlockPos(4, 2, 4));
    NobleSkills.learn(vampire, NobleSkills.MESMERISE, NobleSkills.BLOOD_BOND);
    bound(context, vampire, new BlockPos(1, 2, 1));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Mesmerise.land(vampire, villager, NobleSkills.power(context));
          context.assertEquals(
              Thralls.mayBind(vampire, villager, 1.0F),
              BindRules.Outcome.AT_LIMIT,
              "a second thrall is past the limit");
          context.assertFalse(villager.isRemoved(), "the villager stays a villager");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 5)
  public void anUnmesmerisedVillagerIsNeverBound(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final VillagerEntity villager = villager(context, new BlockPos(4, 2, 4));
    NobleSkills.learn(vampire, NobleSkills.MESMERISE, NobleSkills.BLOOD_BOND);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertEquals(
              Thralls.mayBind(vampire, villager, 1.0F),
              BindRules.Outcome.NO_BOND,
              "only a mesmerised villager can be bound");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 5)
  public void onlyItsOwnerOrdersAThrallToFollowStayOrGuard(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity stranger = TestPlayers.vampire(context, new BlockPos(5, 2, 4));
    final ThrallEntity thrall = bound(context, owner, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          stranger.interact(thrall, Hand.MAIN_HAND);
          context.assertEquals(thrall.mode(), ThrallMode.FOLLOW, "a stranger cannot order it");
          owner.interact(thrall, Hand.MAIN_HAND);
          context.assertEquals(thrall.mode(), ThrallMode.STAY, "the owner told it to stay");
          owner.interact(thrall, Hand.MAIN_HAND);
          context.assertEquals(thrall.mode(), ThrallMode.GUARD, "the owner told it to guard");
          TestPlayers.finish(context, owner, stranger);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 60)
  public void aThrallWaitsWhereItIsWhileItsOwnerIsAway(final TestContext context) {
    final UUID away = UUID.randomUUID();
    final ThrallEntity thrall = context.spawnEntity(Thralls.THRALL, new BlockPos(4, 2, 4));
    thrall.bind(away, thrall.getVillagerData());
    ThrallRegistry.of(context.getWorld().getServer()).bind(away, thrall.getUuid());
    final Vec3d start = context.getAbsolute(Vec3d.ofBottomCenter(new BlockPos(4, 2, 4)));
    context.runAtTick(
        50,
        () -> {
          context.assertTrue(thrall.isAlive() && !thrall.isRemoved(), "it is still a thrall");
          context.assertTrue(
              thrall.getPos().subtract(start).horizontalLength() < 0.5, "it waited where it was");
          ThrallRegistry.of(context.getWorld().getServer()).freeOwner(away);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 5)
  public void aThrallJoinsItsOwnersFight(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ThrallEntity thrall = bound(context, owner, new BlockPos(4, 2, 4));
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 2, 6));
    zombie.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          owner.attack(zombie);
          context.assertTrue(owner.getAttacking() == zombie, "the owner struck the zombie");
          final ThrallTargetGoal goal = new ThrallTargetGoal(thrall);
          context.assertTrue(goal.canStart(), "the thrall found someone to fight");
          goal.start();
          context.assertTrue(thrall.getTarget() == zombie, "it went for the zombie");
          TestPlayers.finish(context, owner);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 5)
  public void aReleasedThrallIsAVillagerAgain(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final BlockPos at = new BlockPos(4, 2, 4);
    final ThrallEntity thrall = bound(context, owner, at);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(Thralls.free(thrall), "an operator released it");
          context.assertTrue(thrall.isRemoved(), "the thrall is gone");
          context.expectEntityAround(EntityType.VILLAGER, at, 1.5);
          context.assertEquals(
              ThrallRegistry.of(context.getWorld().getServer()).count(owner.getUuid()),
              0,
              "the owner keeps no thralls");
          TestPlayers.finish(context, owner);
        });
  }
}
