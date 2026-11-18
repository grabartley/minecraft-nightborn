package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.guild.GuildJournalOrder;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.GuildOathOrder;
import com.grahambartley.nightborn.journal.Journal;
import com.grahambartley.nightborn.journal.JournalItems;
import com.grahambartley.nightborn.journal.Journals;
import com.grahambartley.nightborn.oldblood.OldBloodBlocks;
import com.grahambartley.nightborn.oldblood.OldBloodFontBlockEntity;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import com.grahambartley.nightborn.wheel.WheelEntry;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class JournalGameTest implements FabricGameTest {

  private static final String ARENA = "nightborn:vampire_arena";
  private static final BlockPos FONT = new BlockPos(3, 2, 3);

  private static int journals(final ServerPlayerEntity player, final Journal journal) {
    return player.getInventory().count(JournalItems.of(journal));
  }

  private static GuildMasterEntity guildMaster(final TestContext context) {
    final GuildMasterEntity master =
        context.spawnEntity(GuildMobs.GUILD_MASTER, new BlockPos(4, 2, 4));
    master.setAiDisabled(true);
    return master;
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void swearingTheOathHandsAHunterTheirJournalAndLeavesTheHandFree(
      final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    GuildMembership.prove(human);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertEquals(journals(human, Journal.HUNTER), 0, "a human has no journal");
          context.assertTrue(
              WheelUse.pick(human, master, GuildOathOrder.ID, WheelEntry.NO_ARGUMENT),
              "the human swears the oath");
          context.assertEquals(journals(human, Journal.HUNTER), 1, "the oath gives the journal");
          context.assertTrue(human.getMainHandStack().isEmpty(), "the hand stays free");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void turningHandsANewVampireTheirJournal(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(VampireTransitions.turn(human), "the human turns");
          context.assertEquals(journals(human, Journal.VAMPIRE), 1, "turning gives the journal");
          context.assertEquals(journals(human, Journal.HUNTER), 0, "and no hunter's journal");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aVampireWhoLostTheirJournalGetsOneBackFromTheFontWithoutDrinkingIt(
      final TestContext context) {
    context.setBlockState(FONT, OldBloodBlocks.FONT.getDefaultState());
    final OldBloodFontBlockEntity font = context.getBlockEntity(FONT);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.getInventory().clear();
          vampire.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
          context.useBlock(FONT, vampire);
          context.assertEquals(journals(vampire, Journal.VAMPIRE), 1, "the font gives one back");
          context.useBlock(FONT, vampire);
          context.assertEquals(
              journals(vampire, Journal.VAMPIRE), 1, "but none while one is carried");
          context.assertTrue(font.full(), "and the old blood stays in the font");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aGuildMasterReplacesALostJournalOnlyForAHunterWithoutOne(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertFalse(
              WheelUse.pick(human, master, GuildJournalOrder.ID, WheelEntry.NO_ARGUMENT),
              "a human who has not joined gets no journal");
          context.assertTrue(
              WheelUse.pick(hunter, master, GuildJournalOrder.ID, WheelEntry.NO_ARGUMENT),
              "a hunter without one asks for a journal");
          context.assertEquals(journals(hunter, Journal.HUNTER), 1, "and is given one");
          context.assertFalse(
              WheelUse.pick(hunter, master, GuildJournalOrder.ID, WheelEntry.NO_ARGUMENT),
              "but not a second while one is carried");
          TestPlayers.finish(context, hunter, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aPlayerWhoChoseASideBeforeJournalsIsGivenTheirsOnce(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          FactionAttachment.set(vampire, Faction.VAMPIRE);
          GuildMembership.swear(hunter);
          for (int join = 0; join < 2; join++) {
            Journals.catchUp(vampire);
            Journals.catchUp(hunter);
          }
          context.assertEquals(journals(vampire, Journal.VAMPIRE), 1, "the vampire gets one");
          context.assertEquals(journals(hunter, Journal.HUNTER), 1, "the hunter gets one");
          TestPlayers.finish(context, vampire, hunter);
        });
  }
}
