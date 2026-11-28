package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.guild.GuildData;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.guild.HunterOffer;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.wheel.CommandWheel;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOffer;

public class HunterWaresGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final Identifier TRADE = Identifier.of("nightborn", "hunter_trade");

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aHumanBuysFromAHunterOnlyWhatItsTwoPathsSell(final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(4, 2, 4));
    hunter.setAiDisabled(true);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final List<Identifier> paths = SkillAttachment.get(hunter, SkillTree.HUNTER).lines();
          context.assertEquals(paths.size(), 2, "the hunter has walked two paths");
          context.assertTrue(
              hunter.interactMob(human, Hand.MAIN_HAND).isAccepted(), "using it opens the wheel");
          context.assertTrue(hunter.wares().getCustomer() == null, "the wheel comes first");
          context.assertTrue(
              WheelUse.pick(human, hunter, TRADE, WheelEntry.NO_ARGUMENT), "the human picks trade");
          context.assertTrue(hunter.wares().getCustomer() == human, "the human is trading");
          final List<HunterOffer> all =
              List.copyOf(GuildData.hunterOffers(context.getWorld().getRegistryManager()).values());
          final List<ItemStack> sold =
              human.currentScreenHandler instanceof MerchantScreenHandler trading
                  ? trading.getRecipes().stream().map(TradeOffer::getSellItem).toList()
                  : List.of();
          context.assertFalse(sold.isEmpty(), "the hunter has wares");
          context.assertTrue(
              sold.stream()
                  .allMatch(
                      stack ->
                          all.stream()
                              .filter(offer -> paths.contains(offer.path()))
                              .anyMatch(offer -> ItemStack.areEqual(offer.offer().gives(), stack))),
              "every ware comes from the hunter's own paths");
          context.assertTrue(
              all.stream()
                  .filter(offer -> paths.contains(offer.path()))
                  .filter(offer -> offer.hunterRank().isEmpty() && offer.pathSkills() == 0)
                  .allMatch(
                      offer ->
                          sold.stream()
                              .anyMatch(stack -> ItemStack.areEqual(offer.offer().gives(), stack))),
              "and it sells every plain ware of both");
          human.closeHandledScreen();
          context.assertTrue(hunter.wares().getCustomer() == null, "closing ends the trade");
          context.assertFalse(CommandWheel.open(vampire, hunter), "a vampire gets no wheel");
          TestPlayers.finish(context, human, vampire);
        });
  }
}
