package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.faith.Rebukes;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.guild.build.HunterBuilds;
import com.grahambartley.nightborn.guild.build.HunterGrowth;
import com.grahambartley.nightborn.guild.build.HunterPurchases;
import com.grahambartley.nightborn.guild.build.HunterSkills;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.skill.BoughtSkill;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class HunterBuildGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final int START = 5;
  static final Identifier REBUKE = Identifier.of(Nightborn.MOD_ID, "faithful/rebuke");
  static final int REBUKE_TELL = 10;

  private static HunterPurchases.Shop shop(final TestContext context, final HunterEntity hunter) {
    return HunterSkills.shop(
        hunter,
        context.getWorld().getRegistryManager(),
        HunterBuilds.build(context.getWorld().getRegistryManager()),
        RankLadder.of(context.getWorld().getRegistryManager()),
        Optional.empty());
  }

  @GameTest(templateName = ARENA, tickLimit = START + 10)
  public void aHunterSpawnsWithARankTheCoreAndTwoPathsBoughtByThePlayersRules(
      final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(3, 2, 3));
    hunter.setAiDisabled(true);
    context.runAtTick(
        START,
        () -> {
          context.assertTrue(HunterSkills.built(hunter), "it is given a build when it spawns");
          context.assertTrue(hunter.shownRank().isPresent(), "its rank mark shows by its name");
          final TreeProgress progress = SkillAttachment.get(hunter, SkillTree.HUNTER);
          context.assertEquals(progress.lines().size(), 2, "it walks exactly two paths");
          context.assertTrue(
              HunterPurchases.replays(
                  shop(context, hunter), progress, OathAttachment.get(hunter).rank()),
              "its skills replay through the player's purchase rules");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = START + 10)
  public void aGuildMasterIsMasterRankWithEverySkillOfTheCoreAndHisPaths(
      final TestContext context) {
    final GuildMasterEntity master =
        context.spawnEntity(GuildMobs.GUILD_MASTER, new BlockPos(3, 2, 3));
    master.setAiDisabled(true);
    context.runAtTick(
        START,
        () -> {
          final RankLadder ladder = RankLadder.of(context.getWorld().getRegistryManager());
          context.assertEquals(
              OathAttachment.get(master).rank(), ladder.size() - 1, "he holds the top rank");
          final TreeProgress progress = SkillAttachment.get(master, SkillTree.HUNTER);
          final Map<Identifier, Skill> skills =
              Skills.allSkills(context.getWorld().getRegistryManager());
          final Set<Identifier> branches = new HashSet<>(progress.lines());
          branches.add(Identifier.of(Nightborn.MOD_ID, "hunter"));
          final Set<Identifier> expected =
              skills.entrySet().stream()
                  .filter(e -> branches.contains(e.getValue().branch()))
                  .map(Map.Entry::getKey)
                  .collect(Collectors.toSet());
          context.assertEquals(
              Set.copyOf(progress.ownedInOrder()), expected, "every skill of core and paths");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = START + 10)
  public void aHunterThatEarnsExperienceGainsPointsAndASkill(final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(3, 2, 3));
    hunter.setAiDisabled(true);
    context.runAtTick(
        START,
        () -> {
          final TreeProgress before = SkillAttachment.get(hunter, SkillTree.HUNTER);
          hunter.earn(5000.0F);
          final TreeProgress after = SkillAttachment.get(hunter, SkillTree.HUNTER);
          context.assertTrue(after.earned() > before.earned(), "it earns points");
          context.assertTrue(after.bought().size() > before.bought().size(), "and buys a skill");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = START + 10)
  public void aHuntersRankPathsAndSkillsSurviveASaveAndReload(final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(2, 2, 3));
    hunter.setAiDisabled(true);
    context.runAtTick(
        START,
        () -> {
          final NbtCompound saved = hunter.writeNbt(new NbtCompound());
          saved.remove("UUID");
          final HunterEntity loaded = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(4, 2, 3));
          loaded.setAiDisabled(true);
          loaded.readNbt(saved);
          context.assertEquals(
              OathAttachment.get(loaded), OathAttachment.get(hunter), "its rank comes back");
          context.assertEquals(
              SkillAttachment.get(loaded, SkillTree.HUNTER),
              SkillAttachment.get(hunter, SkillTree.HUNTER),
              "its paths, points and skills come back");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = START + REBUKE_TELL + 10)
  public void aFaithfulHunterRebukesAVampireAfterItsTell(final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(3, 2, 2));
    hunter.setAiDisabled(true);
    final RankLadder ladder = RankLadder.of(context.getWorld().getRegistryManager());
    OathAttachment.set(hunter, HunterGrowth.sworn(ladder, 0));
    SkillAttachment.set(
        hunter,
        SkillTree.HUNTER,
        new TreeProgress(
            5,
            0.0F,
            List.of(
                new BoughtSkill(Identifier.of(Nightborn.MOD_ID, "hunter/tracking"), 1),
                new BoughtSkill(Identifier.of(Nightborn.MOD_ID, "hunter/faith"), 2),
                new BoughtSkill(REBUKE, 2)),
            List.of(Identifier.of(Nightborn.MOD_ID, "faithful"))));
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(3, 2, 4));
    vampire.setAiDisabled(true);
    context.runAtTick(
        START,
        () -> {
          hunter.arm("nightborn:stake");
          hunter.setYaw(0.0F);
          hunter.setHeadYaw(0.0F);
          hunter.setBodyYaw(0.0F);
          hunter.setPitch(0.0F);
          hunter.setTarget(vampire);
          hunter.fight();
          context.assertFalse(Rebukes.silenced(vampire), "nothing lands during the tell");
        });
    context.runAtTick(
        START + REBUKE_TELL,
        () -> {
          hunter.setYaw(0.0F);
          hunter.setPitch(0.0F);
          hunter.fight();
          context.assertTrue(Rebukes.silenced(vampire), "the rebuke lands on the vampire");
          context.assertTrue(
              PowerCooldownAttachment.get(hunter).ticksLeft(REBUKE, context.getWorld().getTime())
                  > 0,
              "and the power cools down as a player's does");
          context.complete();
        });
  }
}
