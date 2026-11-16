package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.stage.VampireStaging;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillPower;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.ward.Wards;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class BatFormGameTest implements FabricGameTest {

  static final String NIGHT = "bat-form";
  static final String DAY = "bat-form-sun";
  static final String ARENA = "nightborn:vampire_arena";
  static final String YARD = "nightborn:vampire_yard";
  static final Identifier BEAST = Identifier.of("nightborn", "beast/beast_blood");
  static final Identifier BAT_FORM = Identifier.of("nightborn", "beast/bat_form");
  static final Identifier LONG_LEAP = Identifier.of("nightborn", "beast/long_leap");
  static final Identifier SWIFT_PROWL = Identifier.of("nightborn", "beast/swift_prowl");
  static final BlockPos STAND = new BlockPos(4, 2, 4);

  @BeforeBatch(batchId = NIGHT)
  public void pinMidnight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = NIGHT)
  public void releaseMidnight(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = DAY)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = DAY)
  public void releaseNoon(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = 20)
  public void aBeastVampireTurnsIntoABatAndBack(final TestContext context) {
    final ServerPlayerEntity vampire = beast(context, BAT_FORM);
    context.assertEquals(Powers.cast(vampire, BAT_FORM), PowerUse.Verdict.READY, "turns");
    context.assertTrue(BatForm.isBat(vampire), "the vampire is a bat");
    context.assertTrue(vampire.getAbilities().flying, "the bat flies");
    context.assertTrue(
        TestAdvancements.done(context, vampire, "fly_by_night"),
        "taking bat form earns Fly By Night");
    context.assertTrue(
        vampire.getHeight() < 1.0F,
        "the bat is bat sized: " + vampire.getDimensions(vampire.getPose()));
    context.assertTrue(
        BloodAttachment.get(vampire) < BloodAttachment.FULL, "turning into a bat costs blood");
    context.assertEquals(Powers.cast(vampire, BAT_FORM), PowerUse.Verdict.READY, "turns back");
    context.assertFalse(BatForm.isBat(vampire), "the vampire stands again");
    context.assertFalse(vampire.getAbilities().allowFlying, "a survival vampire cannot fly");
    context.assertTrue(vampire.getHeight() > 1.5F, "the vampire is full height again");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = 20)
  public void aBatCannotUseItemsOrFeed(final TestContext context) {
    final ServerPlayerEntity vampire = beast(context, BAT_FORM);
    final CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(5, 2, 4));
    cow.setAiDisabled(true);
    Powers.cast(vampire, BAT_FORM);
    context.assertEquals(
        UseEntityCallback.EVENT
            .invoker()
            .interact(vampire, context.getWorld(), Hand.MAIN_HAND, cow, null),
        ActionResult.FAIL,
        "a bat cannot bite");
    context.assertEquals(
        UseItemCallback.EVENT
            .invoker()
            .interact(vampire, context.getWorld(), Hand.MAIN_HAND)
            .getResult(),
        ActionResult.FAIL,
        "a bat cannot use an item");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = 20)
  public void aBatStaysABatWithoutRoomToStand(final TestContext context) {
    final ServerPlayerEntity vampire = beast(context, BAT_FORM);
    Powers.cast(vampire, BAT_FORM);
    context.setBlockState(STAND.up(), Blocks.STONE);
    context.assertEquals(
        Powers.cast(vampire, BAT_FORM), PowerUse.Verdict.CRAMPED, "no room to turn back");
    context.assertTrue(BatForm.isBat(vampire), "the vampire is still a bat");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = TestPlayers.READY + 10)
  public void holyWaterForcesABatBack(final TestContext context) {
    final ServerPlayerEntity vampire = beast(context, BAT_FORM);
    Powers.cast(vampire, BAT_FORM);
    Weakened.add(vampire, HolyWater.WEAKENED_SOURCE, 1, 100);
    context.runAtTick(
        5,
        () -> {
          context.assertFalse(BatForm.isBat(vampire), "holy water forces the bat back");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = 20)
  public void garlicForcesABatBack(final TestContext context) {
    final ServerPlayerEntity vampire = beast(context, BAT_FORM);
    Powers.cast(vampire, BAT_FORM);
    Weakened.add(vampire, Wards.STRINGS, 1, 100);
    context.runAtTick(
        5,
        () -> {
          context.assertFalse(BatForm.isBat(vampire), "garlic forces the bat back");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = 20)
  public void anEmptyBloodBarForcesABatBack(final TestContext context) {
    final ServerPlayerEntity vampire = beast(context, BAT_FORM);
    Powers.cast(vampire, BAT_FORM);
    BloodAttachment.set(vampire, 0);
    context.runAtTick(
        5,
        () -> {
          context.assertFalse(BatForm.isBat(vampire), "an empty blood bar forces the bat back");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = 20)
  public void beingDownedForcesABatBack(final TestContext context) {
    final ServerPlayerEntity vampire = beast(context, BAT_FORM);
    Powers.cast(vampire, BAT_FORM);
    Downed.down(vampire, ServerConfigService.current().downed(), Optional.empty());
    context.runAtTick(
        5,
        () -> {
          context.assertFalse(BatForm.isBat(vampire), "being downed forces the bat back");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = 20)
  public void aBatCannotUseItsOtherPowers(final TestContext context) {
    final ServerPlayerEntity vampire = beast(context, BAT_FORM, SWIFT_PROWL, LONG_LEAP);
    Powers.cast(vampire, BAT_FORM);
    context.assertEquals(
        Powers.cast(vampire, LONG_LEAP), PowerUse.Verdict.AS_A_BAT, "a bat cannot leap");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = TestPlayers.READY + 10)
  public void aHeavyBlowForcesABatBack(final TestContext context) {
    final ServerPlayerEntity vampire = beast(context, BAT_FORM);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Powers.cast(vampire, BAT_FORM);
          vampire.damage(vampire.getDamageSources().generic(), 2.0F);
          context.assertTrue(BatForm.isBat(vampire), "a light blow leaves it a bat");
          vampire.timeUntilRegen = 0;
          vampire.damage(vampire.getDamageSources().generic(), 8.0F);
          context.assertFalse(BatForm.isBat(vampire), "a heavy blow forces it back");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = 20)
  public void aLongLeapThrowsTheVampireForward(final TestContext context) {
    final ServerPlayerEntity vampire = beast(context, SWIFT_PROWL, LONG_LEAP);
    context.assertEquals(Powers.cast(vampire, LONG_LEAP), PowerUse.Verdict.READY, "leaps");
    context.assertTrue(
        vampire.getVelocity().horizontalLength() > 1.0 && vampire.getVelocity().y > 0.0,
        "the leap throws it forward and up: " + vampire.getVelocity());
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = 40)
  public void aVampireMobWithNothingToChaseLandsFromBatForm(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, STAND);
    learn(context, vampire, BAT_FORM);
    context.assertEquals(Powers.cast(vampire, BAT_FORM), PowerUse.Verdict.READY, "turns");
    context.assertTrue(BatForm.isBat(vampire) && vampire.hasNoGravity(), "the mob flies");
    context.assertTrue(vampire.getHeight() < 1.0F, "the mob is bat sized");
    context.runAtTick(
        30,
        () -> {
          context.assertFalse(BatForm.isBat(vampire), "with no target it turns back");
          context.assertFalse(vampire.hasNoGravity(), "and falls under gravity again");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = 80)
  public void aVampireMobFliesToItsTargetAndLandsBesideIt(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(1, 2, 1));
    final CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(7, 2, 7));
    cow.setAiDisabled(true);
    learn(context, vampire, BAT_FORM);
    final float landRange =
        Skills.skill(context.getWorld().getRegistryManager(), BAT_FORM)
            .power()
            .flatMap(SkillPower::batForm)
            .orElseThrow()
            .landRange();
    context.assertTrue(vampire.distanceTo(cow) > landRange, "the target starts out of reach");
    vampire.setAttacker(cow);
    vampire.setTarget(cow);
    context.assertEquals(Powers.cast(vampire, BAT_FORM), PowerUse.Verdict.READY, "turns");
    final boolean[] flew = {false};
    context.runAtEveryTick(
        () -> {
          if (BatForm.isBat(vampire)) {
            flew[0] = true;
          } else if (flew[0]) {
            context.assertTrue(
                vampire.distanceTo(cow) <= landRange + 1.0F,
                "it lands beside its target, " + vampire.distanceTo(cow) + " away");
            context.complete();
          }
        });
  }

  @GameTest(
      templateName = YARD,
      batchId = DAY,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 10)
  public void directSunForcesABatBack(final TestContext context) {
    final ServerPlayerEntity vampire = beast(context, BAT_FORM);
    BatForm.enter(vampire);
    context.runAtTick(
        5,
        () -> {
          context.assertFalse(BatForm.isBat(vampire), "the sun forces the bat back");
          TestPlayers.finish(context, vampire);
        });
  }

  private static ServerPlayerEntity beast(final TestContext context, final Identifier... skills) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    learn(context, vampire, skills);
    return vampire;
  }

  private static void learn(
      final TestContext context, final LivingEntity vampire, final Identifier... skills) {
    final DynamicRegistryManager registries = context.getWorld().getRegistryManager();
    final List<Identifier> picks = new ArrayList<>(List.of(BEAST));
    picks.addAll(List.of(skills));
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        VampireStaging.learned(
            picks, Skills.allSkills(registries), Skills.allBranches(registries)));
    BloodAttachment.set(vampire, BloodAttachment.FULL);
  }
}
