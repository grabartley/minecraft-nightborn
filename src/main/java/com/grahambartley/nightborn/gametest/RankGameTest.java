package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.guild.GuildOath;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.rank.GuildRanks;
import com.grahambartley.nightborn.rank.RankBody;
import com.grahambartley.nightborn.rank.RankDeed;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.rank.Ranking;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class RankGameTest implements FabricGameTest {

  static final int ADULT = 1;

  @GameTest(templateName = "nightborn:vampire_arena", tickLimit = 20)
  public void aSwornHunterRisesWithPointsAndADeedAndStrikesVampiresHarder(
      final TestContext context) {
    final ServerPlayerEntity hunter = sworn(context);
    final float base = hunter.getMaxHealth();
    SkillAttachment.set(hunter, SkillTree.HUNTER, TreeProgress.EMPTY.withEarned(5));
    Ranking.settle(hunter);
    context.assertEquals(GuildRanks.of(hunter), 0, "points alone never raise a rank");
    Ranking.did(hunter, List.of(RankDeed.Act.STAKE), ADULT);
    context.assertEquals(GuildRanks.of(hunter), 1, "rank after the deed and the points");
    context.assertEquals(
        hunter.getMaxHealth(),
        base + ServerConfigService.current().skills().rankHealth(),
        "health after rising");
    context.assertEquals(
        RankLadder.of(hunter.getRegistryManager()).markOf(OathAttachment.get(hunter)),
        Optional.of(Identifier.of("nightborn", "hunter")),
        "the mark beside the name");
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(1, 2, 1));
    vampire.setAiDisabled(true);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 2, 3));
    zombie.setAiDisabled(true);
    context.assertTrue(RankBody.strike(hunter, vampire, 4.0F) > 4.0F, "harder against vampires");
    context.assertEquals(RankBody.strike(hunter, zombie, 4.0F), 4.0F, "no harder against others");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void aHunterWhoTurnsSetsTheRankAsideAndKeepsTheMark(final TestContext context) {
    final ServerPlayerEntity hunter = sworn(context);
    final float base = hunter.getMaxHealth();
    Ranking.setRank(hunter, 2);
    VampireTransitions.turn(hunter);
    RankBody.apply(hunter, ServerConfigService.current().skills());
    context.assertEquals(GuildRanks.of(hunter), 0, "a vampire holds no rank");
    context.assertEquals(OathAttachment.get(hunter).rank(), 2, "the rank is set aside, not lost");
    context.assertTrue(hunter.getMaxHealth() <= base + 0.001F, "no rank health as a vampire");
    context.assertEquals(
        RankLadder.of(hunter.getRegistryManager()).markOf(OathAttachment.get(hunter)),
        Optional.of(Identifier.of("nightborn", "veteran")),
        "the mark that was set aside");
    context.complete();
  }

  @GameTest(templateName = "nightborn:vampire_arena", tickLimit = TestPlayers.READY + 20)
  public void stakingAnAdultVampireDoesTheHuntersDeed(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(2, 2, 4));
    hunter.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.WOODEN_STAKE));
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    vampire.setAiDisabled(true);
    vampire.becomeStage(Identifier.of("nightborn", "adult"));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.damage(vampire.getDamageSources().generic(), 1000.0F);
          vampire.damage(hunter.getDamageSources().playerAttack(hunter), 1.0F);
          context.assertFalse(vampire.isAlive(), "the vampire is staked");
          context.assertTrue(OathAttachment.get(hunter).deedDone(1), "the Hunter deed is done");
          context.assertFalse(
              OathAttachment.get(hunter).deedDone(2), "a lone vampire with no home clears nothing");
          TestPlayers.finish(context, hunter);
        });
  }

  private static ServerPlayerEntity sworn(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    GuildOath.swear(player, Optional.empty());
    context.assertTrue(OathAttachment.get(player).sworn(), "sworn in");
    return player;
  }
}
