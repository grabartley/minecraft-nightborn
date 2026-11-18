package com.grahambartley.nightborn.journal;

import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class Journals {

  private Journals() {}

  public static void register() {
    JournalLogAttachment.register();
    JournalItems.register();
    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> {
          catchUp(handler.getPlayer());
          JournalPages.later(handler.getPlayer());
        });
  }

  public static void give(final ServerPlayerEntity player, final Journal journal) {
    final PlayerInventory inventory = player.getInventory();
    final ItemStack book = new ItemStack(JournalItems.of(journal));
    final int slot = spareSlot(inventory.main, inventory.selectedSlot);
    if (slot < 0) {
      inventory.offerOrDrop(book);
    } else {
      inventory.setStack(slot, book);
    }
    JournalLogAttachment.set(player, JournalLogAttachment.get(player).with(journal));
    player
        .getServerWorld()
        .playSound(
            null,
            player.getBlockPos(),
            SoundEvents.ITEM_BOOK_PAGE_TURN,
            SoundCategory.PLAYERS,
            1.0F,
            1.0F);
  }

  static int spareSlot(final List<ItemStack> main, final int selected) {
    for (int slot = 0; slot < main.size(); slot++) {
      if (slot != selected && main.get(slot).isEmpty()) {
        return slot;
      }
    }
    return -1;
  }

  public static boolean carries(final PlayerEntity player, final Journal journal) {
    return player.getInventory().containsAny(stack -> stack.isOf(JournalItems.of(journal)));
  }

  public static void catchUp(final ServerPlayerEntity player) {
    JournalLogAttachment.get(player)
        .owed(Journal.ofSide(Vampires.isVampire(player), GuildMembership.isHunter(player)))
        .ifPresent(journal -> give(player, journal));
  }
}
