package com.grahambartley.nightborn.journal;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import vazkii.patchouli.api.PatchouliAPI;

public class JournalItem extends Item {

  private final Journal journal;

  public JournalItem(final Journal journal, final Settings settings) {
    super(settings);
    this.journal = journal;
  }

  public Journal journal() {
    return journal;
  }

  @Override
  public TypedActionResult<ItemStack> use(
      final World world, final PlayerEntity user, final Hand hand) {
    if (user instanceof ServerPlayerEntity reader) {
      PatchouliAPI.get().openBookGUI(reader, journal.id());
    }
    return TypedActionResult.success(user.getStackInHand(hand), world.isClient);
  }
}
