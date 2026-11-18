package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.WingedForm;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.hold.HoldKind;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.mind.Commands;
import com.grahambartley.nightborn.mind.Glamour;
import com.grahambartley.nightborn.mind.MindEffect;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.occult.Circles;
import com.grahambartley.nightborn.occult.MistForm;
import com.grahambartley.nightborn.occult.MistState;
import com.grahambartley.nightborn.occult.RestlessDead;
import com.grahambartley.nightborn.occult.RestlessDeadEntity;
import com.grahambartley.nightborn.servant.Servants;
import com.grahambartley.nightborn.skill.Boosts;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillAttributes;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.List;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class BloodlineGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "bloodlines";
  static final int LIMIT = TestPlayers.READY + 40;

  static final Identifier BEAST = id("beast");
  static final Identifier NOBLE = id("noble");
  static final Identifier OCCULT = id("occult");

  @BeforeBatch(batchId = BATCH)
  public void clearCircles(final ServerWorld world) {
    Circles.clearAll();
  }

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  private static void learn(
      final LivingEntity vampire, final Identifier branch, final String... skills) {
    TreeProgress progress =
        TreeProgress.EMPTY.withEarned(60).buy(id(branch.getPath() + "/" + skills[0]), 1, branch);
    for (int i = 1; i < skills.length; i++) {
      progress = progress.buy(id(branch.getPath() + "/" + skills[i]), 1, null);
    }
    SkillAttachment.set(vampire, SkillTree.VAMPIRE, progress);
    SkillAttributes.apply(vampire);
    BloodAttachment.set(vampire, 20);
    PowerCooldownAttachment.set(vampire, PowerCooldowns.NONE);
  }

  private static <T extends LivingEntity> T still(
      final TestContext context, final EntityType<T> type, final BlockPos at) {
    final T entity = context.spawnEntity(type, at);
    if (entity instanceof MobEntity mob) {
      mob.setAiDisabled(true);
    }
    return entity;
  }

  private static void face(final ServerPlayerEntity player, final Entity target) {
    player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getEyePos());
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void pouncePinsTheCreatureItLandsOn(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 2));
    final PigEntity pig = still(context, EntityType.PIG, new BlockPos(4, 2, 3));
    learn(vampire, BEAST, "beast_blood", "swift_prowl", "long_leap", "pounce");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          face(vampire, pig);
          context.assertEquals(
              Powers.cast(vampire, id("beast/pounce")), PowerUse.Verdict.READY, "pounce cast");
        });
    context.runAtTick(
        TestPlayers.READY + 5,
        () -> {
          context.assertTrue(Holds.pinned(pig), "the pig is pinned");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void bloodFrenzyBoostsTheVampireForAWhile(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    learn(vampire, BEAST, "beast_blood", "beast_claws", "blood_frenzy");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final double damage = vampire.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
          context.assertEquals(
              Powers.cast(vampire, id("beast/blood_frenzy")),
              PowerUse.Verdict.READY,
              "frenzy cast");
          context.assertTrue(Boosts.active(vampire, id("beast/blood_frenzy")), "frenzy is on");
          context.assertTrue(
              vampire.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) >= damage + 3.0,
              "the frenzy adds three damage");
          context.assertEquals(BloodAttachment.get(vampire), 17, "the frenzy cost three blood");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void nightTerrorTakesWingAndStaggersAVillager(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final VillagerEntity villager = still(context, EntityType.VILLAGER, new BlockPos(6, 2, 4));
    learn(
        vampire,
        BEAST,
        "beast_blood",
        "beast_claws",
        "swift_prowl",
        "long_leap",
        "pounce",
        "blood_frenzy",
        "night_terror");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertEquals(
              Powers.cast(vampire, id("beast/night_terror")),
              PowerUse.Verdict.READY,
              "night terror cast");
          context.assertTrue(BatForm.isBat(vampire) && WingedForm.on(vampire), "it takes wing");
          context.assertEquals(
              Holds.held(villager).orElse(null), HoldKind.STAGGER, "the villager staggers");
          context.assertEquals(
              Powers.cast(vampire, id("beast/blood_frenzy")),
              PowerUse.Verdict.IN_WINGED_FORM,
              "no other power in that shape");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void apexPredatorCutsBeastBlood(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    learn(
        vampire,
        BEAST,
        "beast_blood",
        "beast_claws",
        "swift_prowl",
        "long_leap",
        "pounce",
        "blood_frenzy",
        "night_terror",
        "apex_predator");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Powers.cast(vampire, id("beast/blood_frenzy"));
          context.assertEquals(BloodAttachment.get(vampire), 18, "the frenzy cost one less");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void commandSendsAMesmerisedVillagerAfterTheVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 2));
    final VillagerEntity villager = still(context, EntityType.VILLAGER, new BlockPos(4, 2, 6));
    learn(vampire, NOBLE, "noble_blood", "mesmerise", "command");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          villager.setAttached(MindTies.MESMERISED_BY, vampire.getUuid());
          villager.addStatusEffect(new StatusEffectInstance(MindEffect.MESMERISED, 200), vampire);
          vampire.setPitch(-80.0F);
          context.assertEquals(
              Powers.cast(vampire, id("noble/command")), PowerUse.Verdict.READY, "command cast");
          context.assertTrue(Commands.commanded(villager), "the villager is under orders");
          context.assertFalse(MindTies.mesmerised(villager), "the stare gives way to the order");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void glamourHidesTheVampireUntilStruck(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    learn(vampire, NOBLE, "noble_blood", "mesmerise", "silver_tongue", "glamour");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertEquals(
              Powers.cast(vampire, id("noble/glamour")), PowerUse.Verdict.READY, "glamour cast");
          context.assertTrue(Glamour.on(vampire), "the vampire passes as human");
          vampire.timeUntilRegen = 0;
          vampire.damage(context.getWorld().getDamageSources().generic(), 1.0F);
          context.assertFalse(Glamour.on(vampire), "a blow breaks the glamour");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void hexHalvesHealing(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 2));
    final PigEntity pig = still(context, EntityType.PIG, new BlockPos(4, 2, 6));
    learn(vampire, OCCULT, "occult_blood", "hex");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          face(vampire, pig);
          context.assertEquals(
              Powers.cast(vampire, id("occult/hex")), PowerUse.Verdict.READY, "hex cast");
          pig.setHealth(4.0F);
          pig.heal(4.0F);
          context.assertEquals(pig.getHealth(), 6.0F, "the pig heals half as well");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void mistDriftsThroughADoor(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final BlockPos door = new BlockPos(2, 2, 2);
    context.setBlockState(door, Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.OPEN, false));
    learn(vampire, OCCULT, "occult_blood", "grave_fog", "shadow_step", "shadow_veil", "mist_form");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final BlockPos at = context.getAbsolutePos(door);
          context.assertFalse(
              context
                  .getWorld()
                  .getBlockState(at)
                  .getCollisionShape(context.getWorld(), at, ShapeContext.of(vampire))
                  .isEmpty(),
              "a closed door stops a vampire");
          context.assertEquals(
              Powers.cast(vampire, id("occult/mist_form")), PowerUse.Verdict.READY, "mist cast");
          context.assertTrue(
              context
                  .getWorld()
                  .getBlockState(at)
                  .getCollisionShape(context.getWorld(), at, ShapeContext.of(vampire))
                  .isEmpty(),
              "mist drifts through it");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void bloodWardBleedsAVillagerInside(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final VillagerEntity villager = still(context, EntityType.VILLAGER, new BlockPos(5, 2, 4));
    learn(vampire, OCCULT, "occult_blood", "grave_fog", "blood_ward");
    context.runAtTick(
        TestPlayers.READY,
        () ->
            context.assertEquals(
                Powers.cast(vampire, id("occult/blood_ward")),
                PowerUse.Verdict.READY,
                "ward cast"));
    context.runAtTick(
        TestPlayers.READY + 25,
        () -> {
          context.assertTrue(
              villager.getHealth() < villager.getMaxHealth(), "the villager bleeds in the circle");
          context.assertTrue(
              Circles.stopsPrayers(context.getWorld(), villager.getPos()),
              "no prayer heals inside the circle");
          Circles.clearAll();
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void raiseTheDeadCallsThreeThatServeTheCaster(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    learn(vampire, OCCULT, "occult_blood", "summon_familiar", "raise_the_dead");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertEquals(
              Powers.cast(vampire, id("occult/raise_the_dead")),
              PowerUse.Verdict.READY,
              "raise cast");
          final List<RestlessDeadEntity> dead =
              context
                  .getWorld()
                  .getEntitiesByType(
                      RestlessDead.TYPE,
                      context.getTestBox().expand(4.0),
                      entity -> Servants.serves(entity, vampire));
          dead.forEach(entity -> entity.setAiDisabled(true));
          context.assertEquals(dead.size(), 3, "three rise");
          context.assertEquals(dead.get(0).getMaxHealth(), 20.0F, "each has twenty health");
          dead.forEach(Entity::discard);
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void mistCannotBite(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final PigEntity pig = still(context, EntityType.PIG, new BlockPos(4, 2, 5));
    learn(vampire, OCCULT, "occult_blood", "grave_fog", "shadow_step", "shadow_veil", "mist_form");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Powers.cast(vampire, id("occult/mist_form"));
          vampire.setSneaking(true);
          context.assertEquals(
              UseEntityCallback.EVENT
                  .invoker()
                  .interact(vampire, context.getWorld(), Hand.MAIN_HAND, pig, null),
              ActionResult.FAIL,
              "mist cannot bite");
          context.assertFalse(FeedingSessions.isFeeding(vampire), "no feeding starts");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void mistHoldsWhileInsideAFence(final TestContext context) {
    final BlockPos fence = new BlockPos(4, 2, 4);
    context.setBlockState(fence, Blocks.OAK_FENCE);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, fence);
    learn(vampire, OCCULT, "occult_blood", "grave_fog", "shadow_step", "shadow_veil", "mist_form");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Powers.cast(vampire, id("occult/mist_form"));
          vampire.setAttached(MistForm.TYPE, new MistState(context.getWorld().getTime(), 2.0F));
          MistForm.tick(vampire);
          context.assertTrue(MistForm.on(vampire), "mist holds inside the fence");
          context.setBlockState(fence, Blocks.AIR);
          vampire.setAttached(MistForm.TYPE, new MistState(context.getWorld().getTime(), 2.0F));
          MistForm.tick(vampire);
          context.assertFalse(MistForm.on(vampire), "mist ends once there is room");
          TestPlayers.finish(context, vampire);
        });
  }
}
