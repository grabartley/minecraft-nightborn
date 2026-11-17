package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.downed.DownedState.Phase;
import com.grahambartley.nightborn.downed.DownedTargeting;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class DownedTargetingGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final int START = 2;
  static final List<EntityType<? extends MobEntity>> ATTACKERS =
      List.of(
          EntityType.IRON_GOLEM,
          EntityType.ZOMBIE,
          EntityType.SKELETON,
          EntityType.WOLF,
          EntityType.WITHER,
          EntityType.ELDER_GUARDIAN,
          EntityType.ENDER_DRAGON,
          NightbornMobs.VAMPIRE);
  static final List<EntityType<? extends MobEntity>> HUNTERS =
      List.of(GuildMobs.HUNTER, GuildMobs.GUILD_MASTER);

  private static VampireEntity vampire(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    vampire.setAiDisabled(true);
    return vampire;
  }

  private static MobEntity beside(
      final TestContext context,
      final EntityType<? extends MobEntity> type,
      final LivingEntity target) {
    final MobEntity mob = type.create(context.getWorld());
    mob.setAiDisabled(true);
    mob.setPosition(target.getPos());
    return mob;
  }

  private static void put(final LivingEntity vampire, final Phase phase) {
    vampire.setAttached(Downed.TYPE, new DownedState(phase, 200));
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void noMobOrBossTargetsADownedVampireMob(final TestContext context) {
    final VampireEntity vampire = vampire(context);
    context.runAtTick(
        START,
        () -> {
          for (final EntityType<? extends MobEntity> type : ATTACKERS) {
            final MobEntity mob = beside(context, type, vampire);
            final String name = EntityType.getId(type).getPath();
            context.assertTrue(mob.canTarget(vampire), name + " may target a standing vampire");
            put(vampire, Phase.DOWNED);
            context.assertFalse(mob.canTarget(vampire), name + " may not target a downed one");
            mob.setTarget(vampire);
            context.assertTrue(mob.getTarget() == null, name + " never takes it as a target");
            vampire.removeAttached(Downed.TYPE);
          }
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theWardenDoesNotCountADownedVampireAsATarget(final TestContext context) {
    final VampireEntity vampire = vampire(context);
    context.runAtTick(
        START,
        () -> {
          final WardenEntity warden = (WardenEntity) beside(context, EntityType.WARDEN, vampire);
          context.assertTrue(warden.isValidTarget(vampire), "a standing vampire angers it");
          put(vampire, Phase.DOWNED);
          context.assertFalse(warden.isValidTarget(vampire), "a downed one does not");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void noMobTakesADownedVampirePlayerAsItsTarget(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final MobEntity zombie = beside(context, EntityType.ZOMBIE, vampire);
          zombie.setTarget(vampire);
          context.assertTrue(zombie.getTarget() == vampire, "a standing vampire player is taken");
          zombie.setTarget(null);
          put(vampire, Phase.DOWNED);
          zombie.setTarget(vampire);
          context.assertTrue(zombie.getTarget() == null, "a downed vampire player is not");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aMobDropsItsTargetTheMomentItGoesDownAndTakesItAgainOnceItEscapes(
      final TestContext context) {
    final VampireEntity vampire = vampire(context);
    final MobEntity golem = context.spawnEntity(EntityType.IRON_GOLEM, new BlockPos(3, 2, 4));
    golem.setAiDisabled(true);
    final MobEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 2, 4));
    zombie.setAiDisabled(true);
    context.runAtTick(
        START,
        () -> {
          for (final MobEntity mob : List.of(golem, zombie)) {
            final String name = EntityType.getId(mob.getType()).getPath();
            mob.setTarget(vampire);
            DownedTargeting.forget(mob);
            context.assertTrue(mob.getTarget() == vampire, name + " keeps a standing target");
            put(vampire, Phase.DOWNED);
            DownedTargeting.forget(mob);
            context.assertTrue(mob.getTarget() == null, name + " drops a downed target");
            put(vampire, Phase.ESCAPING);
            context.assertTrue(mob.canTarget(vampire), name + " may target it once it escapes");
            mob.setTarget(vampire);
            context.assertTrue(mob.getTarget() == vampire, name + " takes it again");
            mob.setTarget(null);
            vampire.removeAttached(Downed.TYPE);
          }
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void huntersAndGuildMastersStillTargetADownedVampire(final TestContext context) {
    final VampireEntity vampire = vampire(context);
    context.runAtTick(
        START,
        () -> {
          put(vampire, Phase.DOWNED);
          for (final EntityType<? extends MobEntity> type : HUNTERS) {
            final MobEntity mob = beside(context, type, vampire);
            final String name = EntityType.getId(type).getPath();
            mob.setTarget(vampire);
            context.assertTrue(mob.getTarget() == vampire, name + " takes a downed target");
            DownedTargeting.forget(mob);
            context.assertTrue(mob.getTarget() == vampire, name + " keeps it");
          }
          context.assertTrue(
              beside(context, GuildMobs.HUNTER, vampire).canTarget(vampire),
              "a hunter may target a downed vampire");
          context.complete();
        });
  }
}
