package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mind.Charm;
import com.grahambartley.nightborn.mind.CharmDiscount;
import com.grahambartley.nightborn.mind.Gazes;
import com.grahambartley.nightborn.mind.Mesmerise;
import com.grahambartley.nightborn.mind.MindEffect;
import com.grahambartley.nightborn.mind.MindFeatures;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;

public class MindGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "minds";
  static final int LANDED = TestPlayers.READY + NobleSkills.STARE_TICKS + 3;

  @BeforeBatch(batchId = BATCH)
  public void clearGazes(final ServerWorld world) {
    Gazes.clearAll();
  }

  private static VillagerEntity stillVillager(final TestContext context, final BlockPos at) {
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, at);
    villager.setAiDisabled(true);
    return villager;
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LANDED + 5)
  public void aPlayerStaringAtAVillagerMesmerisesIt(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 4));
    final VillagerEntity villager = stillVillager(context, new BlockPos(6, 2, 4));
    NobleSkills.learn(vampire, NobleSkills.MESMERISE);
    context.runAtEveryTick(
        () -> vampire.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, villager.getEyePos()));
    context.runAtTick(
        TestPlayers.READY,
        () ->
            context.assertEquals(
                Powers.cast(vampire, NobleSkills.MESMERISE),
                PowerUse.Verdict.READY,
                "the power was cast at the villager"));
    context.runAtTick(
        TestPlayers.READY + NobleSkills.STARE_TICKS - 5,
        () -> context.assertFalse(MindTies.mesmerised(villager), "a glance is not a stare"));
    context.runAtTick(
        LANDED,
        () -> {
          context.assertTrue(
              MindTies.mesmerisedBy(villager, vampire), "the stare mesmerised the villager");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LANDED + 5)
  public void aWallBlocksTheGaze(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 4));
    final VillagerEntity villager = stillVillager(context, new BlockPos(6, 2, 4));
    NobleSkills.learn(vampire, NobleSkills.MESMERISE);
    context.setBlockState(new BlockPos(3, 2, 4), Blocks.STONE);
    context.setBlockState(new BlockPos(3, 3, 4), Blocks.STONE);
    context.runAtEveryTick(
        () -> vampire.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, villager.getEyePos()));
    context.runAtTick(
        TestPlayers.READY,
        () ->
            context.assertEquals(
                Powers.cast(vampire, NobleSkills.MESMERISE),
                PowerUse.Verdict.UNSEEN,
                "nothing can be mesmerised through a wall"));
    context.runAtTick(
        LANDED,
        () -> {
          context.assertFalse(
              MindTies.mesmerised(villager), "the villager behind the wall is free");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 50)
  public void aMesmerisedHorseStandsStillWhileAnotherWalksOn(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 7));
    final HorseEntity held = context.spawnEntity(EntityType.HORSE, new BlockPos(1, 2, 1));
    final HorseEntity free = context.spawnEntity(EntityType.HORSE, new BlockPos(1, 2, 4));
    final Vec3d[] start = new Vec3d[2];
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Mesmerise.land(vampire, held, NobleSkills.power(context));
          start[0] = held.getPos();
          start[1] = free.getPos();
        });
    context.runAtEveryTick(
        () -> {
          if (context.getTick() > TestPlayers.READY) {
            final Vec3d goal = context.getAbsolute(new Vec3d(7.5, 2.0, 2.5));
            held.getNavigation().startMovingTo(goal.x, goal.y, goal.z, 1.2);
            final Vec3d other = context.getAbsolute(new Vec3d(7.5, 2.0, 5.5));
            free.getNavigation().startMovingTo(other.x, other.y, other.z, 1.2);
          }
        });
    context.runAtTick(
        TestPlayers.READY + 45,
        () -> {
          context.assertTrue(
              held.getPos().subtract(start[0]).horizontalLength() < 0.1,
              "the mesmerised horse moved " + held.getPos().subtract(start[0]).horizontalLength());
          context.assertTrue(
              free.getPos().subtract(start[1]).horizontalLength() > 1.0,
              "the free horse should walk but moved "
                  + free.getPos().subtract(start[1]).horizontalLength());
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 60)
  public void aMesmerisedMobStandsStillAndCannotHurtAnyone(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 1));
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 2, 4));
    final VillagerEntity villager = stillVillager(context, new BlockPos(7, 2, 7));
    final Vec3d[] start = new Vec3d[1];
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          zombie.setTarget(villager);
          Mesmerise.land(vampire, zombie, NobleSkills.power(context));
          start[0] = zombie.getPos();
          context.assertTrue(zombie.getTarget() == null, "the mob let go of its target");
        });
    context.runAtTick(
        TestPlayers.READY + 50,
        () -> {
          final float before = villager.getHealth();
          zombie.tryAttack(villager);
          context.assertEquals(
              villager.getHealth(), before, "a mesmerised mob's blow does nothing");
          context.assertTrue(
              zombie.getPos().subtract(start[0]).horizontalLength() < 0.1,
              "the mob stood still, it moved "
                  + zombie.getPos().subtract(start[0]).horizontalLength());
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LANDED + 5)
  public void aVampireMobMesmerisesAPlayerWhoIsSlowedAndCannotStrikeIt(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(1, 2, 4));
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(6, 2, 4));
    final ZombieEntity bystander = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
    bystander.setAiDisabled(true);
    vampire.setAiDisabled(true);
    NobleSkills.learn(vampire, NobleSkills.MESMERISE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setTarget(human);
          context.assertEquals(
              Powers.cast(vampire, NobleSkills.MESMERISE),
              PowerUse.Verdict.READY,
              "the vampire mob cast the same power a player does");
        });
    context.runAtTick(
        LANDED,
        () -> {
          context.assertTrue(MindTies.mesmerisedBy(human, vampire), "the player is mesmerised");
          context.assertTrue(
              human.hasStatusEffect(StatusEffects.SLOWNESS), "a mesmerised player is slowed");
          context.assertFalse(MindFeatures.stilled(human), "a mesmerised player is never frozen");
          context.assertFalse(
              vampire.damage(human.getDamageSources().playerAttack(human), 4.0F),
              "the player cannot strike the vampire that mesmerised them");
          context.assertTrue(
              bystander.damage(human.getDamageSources().playerAttack(human), 1.0F),
              "the player can still strike anything else");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = EMPTY_STRUCTURE, batchId = BATCH)
  public void bossesCannotBeMesmerised(final TestContext context) {
    for (final EntityType<?> boss :
        new EntityType<?>[] {EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WARDEN}) {
      context.assertTrue(boss.isIn(Mesmerise.UNMESMERISABLE), boss.getUntranslatedName());
    }
    context.assertFalse(
        EntityType.VILLAGER.isIn(Mesmerise.UNMESMERISABLE), "a villager can be mesmerised");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 5)
  public void aCharmedVillagerTradesAtAHerosPrice(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 4));
    final VillagerEntity villager = stillVillager(context, new BlockPos(3, 2, 4));
    villager.setVillagerData(villager.getVillagerData().withProfession(VillagerProfession.FARMER));
    NobleSkills.learn(vampire, NobleSkills.MESMERISE, NobleSkills.SILVER_TONGUE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Mesmerise.land(vampire, villager, NobleSkills.power(context));
          UseEntityCallback.EVENT
              .invoker()
              .interact(vampire, context.getWorld(), Hand.MAIN_HAND, villager, null);
          context.assertTrue(
              villager.hasStatusEffect(MindEffect.CHARMED), "talking to it charmed the villager");
          final TradeOffer offer = villager.getOffers().get(0);
          offer.clearSpecialPrice();
          Charm.discount(villager, vampire);
          context.assertEquals(
              offer.getSpecialPrice(),
              -CharmDiscount.cut(offer.getOriginalFirstBuyItem().getCount(), 1),
              "the charmed price matches a hero's");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 50)
  public void aMesmerisedCreeperLetsItsFuseGoOut(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 1));
    final CreeperEntity creeper = context.spawnEntity(EntityType.CREEPER, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          creeper.setFuseSpeed(1);
          Mesmerise.land(vampire, creeper, NobleSkills.power(context));
        });
    context.runAtTick(
        TestPlayers.READY + 45,
        () -> {
          context.assertTrue(creeper.isAlive(), "the creeper never went off");
          context.assertEquals(creeper.getFuseSpeed(), -1, "its fuse went out");
          TestPlayers.finish(context, vampire);
        });
  }
}
