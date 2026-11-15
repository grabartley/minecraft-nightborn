package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillEarning;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillShop;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.Tracking;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.VampireTransitions;
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

public class HunterGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String PVP = "hunter-pvp";
  static final Identifier HUNTER = Identifier.of("nightborn", "hunter");
  static final Identifier TRACKING = Identifier.of("nightborn", "hunter/tracking");
  static final Identifier BITE_WARD = Identifier.of("nightborn", "hunter/bite_ward");
  static final Identifier FAITH = Identifier.of("nightborn", "hunter/faith");
  static final Identifier IRON_WILL = Identifier.of("nightborn", "hunter/iron_will");
  static final Identifier[] HUNTER_SKILLS = {
    TRACKING,
    BITE_WARD,
    FAITH,
    IRON_WILL,
    Identifier.of("nightborn", "hunter/steeled_blood"),
    Identifier.of("nightborn", "hunter/devotion"),
    Identifier.of("nightborn", "hunter/clear_mind")
  };

  @BeforeBatch(batchId = PVP)
  public void pvpOn(final ServerWorld world) {
    BatchPvp.set(world.getServer(), true);
  }

  @AfterBatch(batchId = PVP)
  public void restorePvp(final ServerWorld world) {
    BatchPvp.restore(world.getServer());
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void theHunterTreeLoadsAsData(final TestContext context) {
    final var registries = context.getWorld().getRegistryManager();
    final SkillBranch branch = Skills.branch(registries, HUNTER);
    context.assertTrue(
        branch != null && branch.tree() == SkillTree.HUNTER, "the hunter branch is in its tree");
    for (final Identifier id : HUNTER_SKILLS) {
      context.assertTrue(Skills.skill(registries, id) != null, "skill " + id + " loads");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = PVP, tickLimit = TestPlayers.READY + 40)
  public void aHumanWhoDownedAVampireEarnsPointsWhenTheSunFinishesIt(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.damage(hunter.getDamageSources().playerAttack(hunter), 1000.0F);
          final DownedState state = Downed.state(vampire);
          context.assertTrue(state != null && state.isDowned(), "the vampire is downed");
          context.assertEquals(
              state.downedBy(), Optional.of(hunter.getUuid()), "the hunter is remembered");
          context.assertEquals(
              SkillAttachment.get(hunter, SkillTree.HUNTER).earned(), 0, "downing earns nothing");
        });
    context.runAtTick(
        TestPlayers.READY + 25,
        () -> {
          vampire.damage(NightbornDamage.sun(context.getWorld()), 1000.0F);
          context.assertTrue(vampire.isDead(), "the sun finishes the downed vampire");
          context.assertEquals(
              SkillAttachment.get(hunter, SkillTree.HUNTER).earned(),
              ServerConfigService.current().skills().huntPoints(),
              "the hunter is paid");
          context.assertEquals(
              SkillAttachment.get(hunter, SkillTree.VAMPIRE).earned(),
              0,
              "no vampire points are made");
          TestPlayers.finish(context, hunter, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = PVP, tickLimit = TestPlayers.READY + 40)
  public void noHumanIsPaidForAVampireAnotherVampireDowned(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(1, 2, 4));
    final ServerPlayerEntity biter = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.damage(biter.getDamageSources().playerAttack(biter), 1000.0F);
          final DownedState state = Downed.state(vampire);
          context.assertTrue(state != null && state.isDowned(), "the vampire is downed");
          context.assertTrue(state.downedBy().isEmpty(), "a vampire is not a hunter");
        });
    context.runAtTick(
        TestPlayers.READY + 25,
        () -> {
          vampire.damage(NightbornDamage.sun(context.getWorld()), 1000.0F);
          context.assertTrue(vampire.isDead(), "the sun finishes it");
          context.assertEquals(
              SkillAttachment.get(human, SkillTree.HUNTER).earned(), 0, "nobody is paid");
          context.assertEquals(
              SkillAttachment.get(biter, SkillTree.HUNTER).earned(), 0, "nor the vampire");
          TestPlayers.finish(context, human, biter, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aTurnedPlayersHunterTreeIsSetAsideAndComesBack(final TestContext context) {
    final ServerPlayerEntity player = TestPlayers.human(context, new BlockPos(3, 2, 4));
    SkillAttachment.set(player, SkillTree.HUNTER, TreeProgress.EMPTY.withEarned(5));
    context.assertEquals(SkillShop.buy(player, TRACKING), SkillPurchase.Verdict.ALLOWED, "track");
    context.assertEquals(SkillShop.buy(player, BITE_WARD), SkillPurchase.Verdict.ALLOWED, "ward");
    context.assertEquals(
        player.getAttributeValue(HunterAttributes.BITE_RESISTANCE), 0.35, "bite ward resists");
    VampireTransitions.turn(player);
    context.assertEquals(
        player.getAttributeValue(HunterAttributes.BITE_RESISTANCE),
        0.0,
        "a vampire loses its hunter gifts");
    context.assertEquals(
        SkillShop.buy(player, FAITH), SkillPurchase.Verdict.WRONG_TREE, "no hunter skills");
    context.assertFalse(
        SkillEarning.fromHunt(player, ServerConfigService.current().skills()),
        "a vampire earns no hunter points");
    final TreeProgress kept = SkillAttachment.get(player, SkillTree.HUNTER);
    context.assertTrue(kept.owns(TRACKING) && kept.owns(BITE_WARD), "the tree is kept aside");
    context.assertEquals(kept.earned(), 5, "with its points");
    VampireTransitions.makeHuman(player);
    context.assertEquals(
        player.getAttributeValue(HunterAttributes.BITE_RESISTANCE),
        0.35,
        "made human again, the gifts come back");
    TestPlayers.finish(context, player);
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void trackingFindsTheNearestVampireForAShortTime(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(1, 2, 1));
    final ServerPlayerEntity near = TestPlayers.vampire(context, new BlockPos(3, 2, 3));
    final ServerPlayerEntity far = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    SkillAttachment.set(hunter, SkillTree.HUNTER, TreeProgress.EMPTY.withEarned(1));
    SkillShop.buy(hunter, TRACKING);
    context.assertTrue(Tracking.state(hunter) == null, "not tracking before the power");
    context.assertEquals(Powers.use(hunter, 0), PowerUse.Verdict.READY, "a human uses tracking");
    context.assertTrue(Tracking.state(hunter) != null, "tracking starts");
    context.assertEquals(
        Tracking.nearest(hunter, 64).orElse(null), near, "the nearest vampire is tracked");
    context.assertEquals(Powers.use(hunter, 0), PowerUse.Verdict.COOLING, "it then cools down");
    TestPlayers.remove(context, near);
    context.assertEquals(
        Tracking.nearest(hunter, 64).orElse(null), far, "the next nearest takes its place");
    context.assertTrue(Tracking.nearest(hunter, 1).isEmpty(), "nothing beyond range");
    TestPlayers.finish(context, hunter, far);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aHunterWhoTurnsStopsTracking(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(3, 2, 3));
    SkillAttachment.set(hunter, SkillTree.HUNTER, TreeProgress.EMPTY.withEarned(1));
    SkillShop.buy(hunter, TRACKING);
    Powers.use(hunter, 0);
    context.assertTrue(Tracking.state(hunter) != null, "tracking starts");
    VampireTransitions.turn(hunter);
    context.runAtTick(
        2,
        () -> {
          context.assertTrue(Tracking.state(hunter) == null, "a turned hunter stops tracking");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 260)
  public void trackingEnds(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(3, 2, 3));
    SkillAttachment.set(hunter, SkillTree.HUNTER, TreeProgress.EMPTY.withEarned(1));
    SkillShop.buy(hunter, TRACKING);
    Powers.use(hunter, 0);
    final int duration =
        Skills.skill(context.getWorld().getRegistryManager(), TRACKING)
            .power()
            .orElseThrow()
            .track()
            .orElseThrow()
            .durationTicks();
    context.runAtTick(
        duration + 5,
        () -> {
          context.assertTrue(Tracking.state(hunter) == null, "tracking stops after its time");
          TestPlayers.finish(context, hunter);
        });
  }
}
