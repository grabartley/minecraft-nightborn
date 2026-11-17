package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodCapacity;
import com.grahambartley.nightborn.skill.PowerBar;
import com.grahambartley.nightborn.skill.PowerBarEdits;
import com.grahambartley.nightborn.skill.PowerLayout;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillAttributes;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillShop;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SkillFoundationGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String NIGHT_BATCH = "skillFoundationNight";
  static final BlockPos STAND = new BlockPos(3, 2, 4);
  static final Identifier THICK_BLOOD = Identifier.of("nightborn", "core/thick_blood");
  static final Identifier BLOOD_RUSH = Identifier.of("nightborn", "core/blood_rush");
  static final Identifier VANISH = Identifier.of("nightborn", "core/vanish");
  static final Identifier BLOOD_HEALING = Identifier.of("nightborn", "core/blood_healing");
  static final Identifier DEEP_VEINS = Identifier.of("nightborn", "core/deep_veins");

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void eachRankIsBoughtInTurnAndAddsHealth(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    SkillAttachment.set(vampire, SkillTree.VAMPIRE, TreeProgress.EMPTY.withEarned(6));
    float health = vampire.getMaxHealth();
    for (int rank = 1; rank <= 3; rank++) {
      context.assertEquals(
          SkillShop.buy(vampire, THICK_BLOOD), SkillPurchase.Verdict.ALLOWED, "rank " + rank);
      context.assertTrue(vampire.getMaxHealth() > health, "rank " + rank + " adds health");
      health = vampire.getMaxHealth();
    }
    context.assertEquals(
        SkillShop.buy(vampire, THICK_BLOOD), SkillPurchase.Verdict.OWNED, "no fourth rank");
    final TreeProgress after = SkillAttachment.get(vampire, SkillTree.VAMPIRE);
    context.assertEquals(after.rank(THICK_BLOOD), 3, "three ranks held");
    context.assertEquals(after.free(), 0, "the ranks cost one, two and three points");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aFledglingCannotBuyASkillThatNeedsAnOlderStage(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    SkillAttachment.set(vampire, SkillTree.VAMPIRE, TreeProgress.EMPTY.withEarned(20));
    context.assertEquals(
        SkillShop.buy(vampire, THICK_BLOOD), SkillPurchase.Verdict.ALLOWED, "thick blood");
    context.assertEquals(
        SkillShop.buy(vampire, BLOOD_HEALING), SkillPurchase.Verdict.ALLOWED, "blood healing");
    context.assertEquals(
        SkillShop.buy(vampire, DEEP_VEINS),
        SkillPurchase.Verdict.NEEDS_STAGE,
        "deep veins needs an adult");
    context.assertFalse(
        SkillAttachment.get(vampire, SkillTree.VAMPIRE).owns(DEEP_VEINS), "it is not owned");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theServerRefusesAnUnownedSkillOnThePowerBar(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        TreeProgress.EMPTY.withEarned(3).buy(THICK_BLOOD, 1, null).buy(BLOOD_RUSH, 2, null));
    context.assertFalse(
        PowerBarEdits.set(vampire, 0, Optional.of(VANISH)), "an unowned power is refused");
    context.assertFalse(
        PowerBarEdits.set(vampire, 1, Optional.of(THICK_BLOOD)), "a passive skill is refused");
    context.assertEquals(bar(vampire).at(0), Optional.of(BLOOD_RUSH), "the bar is left as it was");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aPowerIsUsedFromTheSlotItWasMovedTo(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        TreeProgress.EMPTY.withEarned(3).buy(THICK_BLOOD, 1, null).buy(BLOOD_RUSH, 2, null));
    context.assertTrue(PowerBarEdits.set(vampire, 3, Optional.of(BLOOD_RUSH)), "moved to slot 4");
    context.assertEquals(
        Powers.use(vampire, 0), PowerUse.Verdict.NO_POWER, "the old slot is empty");
    context.assertEquals(Powers.use(vampire, 3), PowerUse.Verdict.READY, "the new slot casts");
    context.assertTrue(PowerBarEdits.set(vampire, 3, Optional.empty()), "cleared");
    context.assertTrue(bar(vampire).isEmpty(), "a power on no slot cannot be used");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void deepVeinsMakesTheBloodBarLarger(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    context.assertEquals(BloodCapacity.of(vampire), BloodAttachment.FULL, "twenty to start");
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        TreeProgress.EMPTY.withEarned(5).buy(DEEP_VEINS, 2, null).buy(DEEP_VEINS, 3, null));
    SkillAttributes.apply(vampire);
    context.assertEquals(BloodCapacity.of(vampire), BloodAttachment.FULL + 4, "two ranks");
    BloodAttachment.set(vampire, 100);
    context.assertEquals(
        BloodAttachment.get(vampire), BloodAttachment.FULL + 4, "the bar fills to its new size");
    TestPlayers.finish(context, vampire);
  }

  @BeforeBatch(batchId = NIGHT_BATCH)
  public void pinNight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = NIGHT_BATCH)
  public void unpinNight(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 200)
  public void bloodHealingHealsSixHealthOverThreeSeconds(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        TreeProgress.EMPTY.withEarned(3).buy(THICK_BLOOD, 1, null).buy(BLOOD_HEALING, 2, null));
    final float[] before = new float[1];
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          BloodAttachment.set(vampire, BloodAttachment.FULL);
          vampire.setHealth(4.0F);
          before[0] = vampire.getHealth();
          context.assertEquals(
              Powers.cast(vampire, BLOOD_HEALING), PowerUse.Verdict.READY, "the power casts");
          context.assertEquals(
              BloodAttachment.get(vampire), BloodAttachment.FULL - 2, "it costs two blood");
        });
    context.runAtTick(
        TestPlayers.READY + 30,
        () ->
            context.assertTrue(
                vampire.getHealth() > before[0] && vampire.getHealth() < before[0] + 6.0F,
                "half way through it has healed part of the way"));
    context.runAtTick(
        TestPlayers.READY + 65,
        () -> {
          context.assertTrue(
              Math.abs(vampire.getHealth() - (before[0] + 6.0F)) < 0.05F,
              "six health healed, not " + (vampire.getHealth() - before[0]));
          TestPlayers.finish(context, vampire);
        });
  }

  private static PowerLayout bar(final ServerPlayerEntity vampire) {
    return PowerBar.layout(
        Skills.skills(vampire.getServerWorld().getRegistryManager()).orElse(null),
        SkillAttachment.get(vampire, SkillTree.VAMPIRE));
  }
}
