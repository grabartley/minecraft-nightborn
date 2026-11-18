package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.FeedingSettings;
import com.grahambartley.nightborn.config.MindSettings;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.mind.Gazes;
import com.grahambartley.nightborn.mind.Mesmerise;
import com.grahambartley.nightborn.servant.Stance;
import com.grahambartley.nightborn.servant.goal.StanceTargetGoal;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.thrall.BindRules;
import com.grahambartley.nightborn.thrall.ThrallEntity;
import com.grahambartley.nightborn.thrall.ThrallOrders;
import com.grahambartley.nightborn.thrall.ThrallRegistry;
import com.grahambartley.nightborn.thrall.Thralls;
import com.grahambartley.nightborn.thrall.VillagerMemory;
import com.grahambartley.nightborn.wheel.CommandWheel;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.village.VillageGossipType;
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
    thrall.bind(owner.getUuid(), thrall.getVillagerData(), VillagerMemory.NONE);
    ThrallRegistry.of(context.getWorld().getServer()).bind(owner.getUuid(), thrall.getUuid());
    return thrall;
  }

  private static List<ThrallEntity> thrallsNear(final TestContext context, final BlockPos at) {
    final Vec3d centre = context.getAbsolute(Vec3d.ofBottomCenter(at));
    return context
        .getWorld()
        .getEntitiesByClass(ThrallEntity.class, new Box(centre, centre).expand(2.0), t -> true);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 40)
  public void aHouseThrallStaysBoundToItsHouseAndRemembersIt(final TestContext context) {
    final ThrallEntity thrall = context.spawnEntity(Thralls.THRALL, new BlockPos(4, 2, 4));
    thrall.setAiDisabled(true);
    final BlockPos home = context.getAbsolutePos(new BlockPos(2, 2, 2));
    thrall.keepHouse(home);
    context.runAtTick(
        30,
        () -> {
          context.assertTrue(
              thrall.isAlive() && !thrall.isRemoved(),
              "a house thrall with no player master is not set free");
          final ThrallEntity reloaded = Thralls.THRALL.create(context.getWorld());
          reloaded.readNbt(thrall.writeNbt(new NbtCompound()));
          context.assertEquals(
              reloaded.house().orElse(null), home, "the thrall remembers its house after a reload");
          context.assertEquals(reloaded.mode(), Stance.STAY, "it keeps to its post");
          reloaded.discard();
          context.complete();
        });
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
          context.assertTrue(
              NameGameTest.inFamiliarPools(context, thralls.get(0).getName().getString()),
              "the thrall holds a name from the familiar pools from the moment it is bound");
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
  public void onlyItsOwnerOrdersAThrallToFollowStayOrGuardFromTheWheel(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity stranger = TestPlayers.vampire(context, new BlockPos(5, 2, 4));
    final ThrallEntity thrall = bound(context, owner, new BlockPos(4, 2, 4));
    thrall.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertFalse(
              CommandWheel.open(stranger, thrall), "a stranger gets no wheel for it");
          context.assertFalse(
              WheelUse.forge(stranger, thrall, ThrallOrders.ID, Stance.STAY.ordinal()),
              "a forged choice for someone else's thrall is refused");
          context.assertEquals(thrall.mode(), Stance.FOLLOW, "a stranger cannot order it");
          context.assertTrue(
              WheelUse.pick(owner, thrall, ThrallOrders.ID, Stance.STAY.ordinal()),
              "the owner picks stay on the wheel");
          context.assertEquals(thrall.mode(), Stance.STAY, "the owner told it to stay");
          context.assertTrue(
              WheelUse.pick(owner, thrall, ThrallOrders.ID, Stance.GUARD.ordinal()),
              "the owner picks guard on the wheel");
          context.assertEquals(thrall.mode(), Stance.GUARD, "the owner told it to guard");
          context.assertTrue(
              WheelUse.pick(owner, thrall, ThrallOrders.ID, Stance.FOLLOW.ordinal()),
              "the owner picks follow on the wheel");
          context.assertEquals(thrall.mode(), Stance.FOLLOW, "the owner told it to follow");
          context.assertFalse(
              WheelUse.forge(owner, thrall, ThrallOrders.ID, Stance.STAY.ordinal()),
              "each wheel takes one choice, so an old one cannot be replayed");
          TestPlayers.finish(context, owner, stranger);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 60)
  public void aThrallWaitsWhereItIsWhileItsOwnerIsAway(final TestContext context) {
    final UUID away = UUID.randomUUID();
    final ThrallEntity thrall = context.spawnEntity(Thralls.THRALL, new BlockPos(4, 2, 4));
    thrall.bind(away, thrall.getVillagerData(), VillagerMemory.NONE);
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
          final StanceTargetGoal goal = new StanceTargetGoal(thrall, thrall);
          context.assertTrue(goal.canStart(), "the thrall found someone to fight");
          goal.start();
          context.assertTrue(thrall.getTarget() == zombie, "it went for the zombie");
          thrall.setTarget(null);
          owner.age += 200;
          context.assertFalse(
              new StanceTargetGoal(thrall, thrall).canStart(),
              "long after the owner's blow the thrall leaves the zombie be");
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
          run(context, "nightborn thrall release " + thrall.getUuidAsString());
          context.assertTrue(thrall.isRemoved(), "the thrall is gone");
          context.expectEntityAround(EntityType.VILLAGER, at, 1.5);
          context.assertEquals(
              ThrallRegistry.of(context.getWorld().getServer()).count(owner.getUuid()),
              0,
              "the owner keeps no thralls");
          TestPlayers.finish(context, owner);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 5)
  public void aThrallSetFreeKeepsItsTradesAndExperience(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final BlockPos at = new BlockPos(4, 2, 4);
    final VillagerEntity villager = villager(context, at);
    NobleSkills.learn(vampire, NobleSkills.MESMERISE, NobleSkills.BLOOD_BOND);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          villager.setExperience(30);
          villager.getGossip().startGossip(vampire.getUuid(), VillageGossipType.MINOR_POSITIVE, 20);
          final int reputation =
              villager.getGossip().getReputationFor(vampire.getUuid(), type -> true);
          final int offers = villager.getOffers().size();
          final String firstSold = villager.getOffers().get(0).getSellItem().toString();
          Mesmerise.land(vampire, villager, NobleSkills.power(context));
          context.assertEquals(
              Thralls.mayBind(vampire, villager, 1.0F), BindRules.Outcome.BOUND, "bound");
          final ThrallEntity thrall = thrallsNear(context, at).get(0);
          context.assertTrue(Thralls.free(thrall), "released");
          final Vec3d centre = context.getAbsolute(Vec3d.ofBottomCenter(at));
          final VillagerEntity back =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      VillagerEntity.class, new Box(centre, centre).expand(2.0), v -> v.isAlive())
                  .get(0);
          context.assertEquals(back.getExperience(), 30, "the villager keeps its experience");
          context.assertEquals(
              back.getGossip().getReputationFor(vampire.getUuid(), type -> true),
              reputation,
              "the villager keeps its gossip");
          context.assertEquals(back.getOffers().size(), offers, "the villager keeps its trades");
          context.assertEquals(
              back.getOffers().get(0).getSellItem().toString(), firstSold, "the same first trade");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 30)
  public void freeingAnOwnerLetsTheirThrallsGo(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final BlockPos at = new BlockPos(5, 2, 5);
    final ThrallEntity thrall = bound(context, owner, at);
    thrall.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          run(context, "nightborn thrall free @a[name=" + owner.getName().getString() + "]");
          context.assertEquals(
              ThrallRegistry.of(context.getWorld().getServer()).count(owner.getUuid()),
              0,
              "the command freed the owner's thrall");
        });
    context.runAtTick(
        TestPlayers.READY + 25,
        () -> {
          context.assertTrue(thrall.isRemoved(), "the thrall let go of its bond");
          context.expectEntityAround(EntityType.VILLAGER, at, 1.5);
          TestPlayers.finish(context, owner);
        });
  }

  private static void run(final TestContext context, final String command) {
    final MinecraftServer server = context.getWorld().getServer();
    server
        .getCommandManager()
        .executeWithPrefix(server.getCommandSource().withLevel(4).withSilent(), command);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 5)
  public void aThrallFightsBackButNeverAtItsOwnersSide(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(1, 2, 1));
    final ThrallEntity thrall = bound(context, owner, new BlockPos(4, 2, 4));
    final ThrallEntity fellow = bound(context, owner, new BlockPos(5, 2, 4));
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 2, 6));
    zombie.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          thrall.setAttacker(owner);
          context.assertFalse(
              new StanceTargetGoal(thrall, thrall).canStart(), "a blow from its owner is forgiven");
          thrall.setAttacker(fellow);
          context.assertFalse(
              new StanceTargetGoal(thrall, thrall).canStart(),
              "a blow from a fellow thrall is forgiven");
          thrall.setAttacker(zombie);
          final StanceTargetGoal goal = new StanceTargetGoal(thrall, thrall);
          context.assertTrue(goal.canStart(), "a zombie's blow is answered");
          goal.start();
          context.assertTrue(thrall.getTarget() == zombie, "it fights back");
          TestPlayers.finish(context, owner);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 5)
  public void aGuardGoesAfterMonstersNearItsPostAndAFollowerDoesNot(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(1, 2, 1));
    final ThrallEntity guard = bound(context, owner, new BlockPos(3, 2, 4));
    final ThrallEntity follower = bound(context, owner, new BlockPos(4, 2, 4));
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 2, 6));
    zombie.setAiDisabled(true);
    guard.order(Stance.GUARD);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertFalse(
              new StanceTargetGoal(follower, follower).canStart(),
              "a follower leaves the zombie be");
          final StanceTargetGoal goal = new StanceTargetGoal(guard, guard);
          context.assertTrue(goal.canStart(), "the guard sees the zombie by its post");
          goal.start();
          context.assertTrue(guard.getTarget() == zombie, "the guard goes for it");
          TestPlayers.finish(context, owner);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = FIRST_DRINK + 5)
  public void aThrallNeverJoinsItsOwnersFeed(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final VillagerEntity villager = villager(context, new BlockPos(4, 2, 4));
    final ThrallEntity thrall = bound(context, owner, new BlockPos(1, 2, 1));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= TestPlayers.READY && context.getTick() < FIRST_DRINK) {
            owner.setSneaking(true);
            UseEntityCallback.EVENT
                .invoker()
                .interact(owner, context.getWorld(), Hand.MAIN_HAND, villager, null);
          }
        });
    context.runAtTick(
        FIRST_DRINK,
        () -> {
          context.assertTrue(villager.getHealth() < villager.getMaxHealth(), "the owner fed");
          context.assertFalse(
              new StanceTargetGoal(thrall, thrall).canStart(), "the thrall leaves the meal alone");
          FeedingSessions.end(owner);
          TestPlayers.finish(context, owner);
        });
  }
}
