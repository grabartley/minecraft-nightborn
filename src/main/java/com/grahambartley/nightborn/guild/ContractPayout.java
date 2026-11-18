package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.skill.SkillEarning;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class ContractPayout {

  private ContractPayout() {}

  public static void pay(
      final ServerPlayerEntity hunter,
      final ItemStack contractStack,
      final HunterContract contract,
      final SkillSettings settings) {
    contractStack.decrement(1);
    final ServerWorld world = hunter.getServerWorld();
    final LootTable table =
        world.getServer().getReloadableRegistries().getLootTable(contract.reward());
    final LootContextParameterSet parameters =
        new LootContextParameterSet.Builder(world)
            .add(LootContextParameters.THIS_ENTITY, hunter)
            .add(LootContextParameters.ORIGIN, hunter.getPos())
            .luck(hunter.getLuck())
            .build(LootContextTypes.GIFT);
    table.generateLoot(parameters).forEach(stack -> hunter.getInventory().offerOrDrop(stack));
    SkillEarning.fromContract(hunter, settings);
    Deeds.record(hunter, Deed.of(DeedKinds.CONTRACT_FINISHED));
    hunter.sendMessage(Text.translatable("nightborn.contract.paid", contract.description()), true);
    world.playSound(
        null,
        hunter.getBlockPos(),
        SoundEvents.ENTITY_VILLAGER_YES,
        SoundCategory.NEUTRAL,
        1.0F,
        0.9F);
  }
}
