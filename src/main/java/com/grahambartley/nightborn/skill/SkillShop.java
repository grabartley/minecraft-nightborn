package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.network.BuySkillPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class SkillShop {

  private SkillShop() {}

  public static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        BuySkillPayload.ID, (payload, context) -> buy(context.player(), payload.skill()));
  }

  public static SkillPurchase.Verdict buy(final ServerPlayerEntity player, final Identifier id) {
    final DynamicRegistryManager registries = player.getServerWorld().getRegistryManager();
    final SkillTree tree = SkillTree.of(FactionAttachment.get(player));
    final TreeProgress progress = SkillAttachment.get(player, tree);
    final SkillLookup lookup = SkillLookup.of(registries, progress, tree, id);
    if (lookup.verdict() != SkillPurchase.Verdict.ALLOWED) {
      player.sendMessage(Text.translatable(lookup.verdict().translationKey()), true);
      return lookup.verdict();
    }
    SkillAttachment.set(
        player, tree, SkillPurchase.apply(progress, id, lookup.skill(), lookup.branch()));
    SkillAttributes.apply(player);
    player.sendMessage(
        Text.translatable("nightborn.skills.bought", Text.translatable(SkillNames.skill(id))),
        true);
    player
        .getServerWorld()
        .playSound(
            null,
            player.getBlockPos(),
            SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
            SoundCategory.PLAYERS,
            0.8F,
            0.7F);
    return lookup.verdict();
  }
}
