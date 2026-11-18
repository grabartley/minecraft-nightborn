package com.grahambartley.nightborn.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class JournalsTest {

  @ParameterizedTest(name = "{0} in slot {1}: carries hunter {2}, vampire {3}")
  @CsvSource({
    "NONE, 0, false, false",
    "HUNTER, 0, true, false",
    "HUNTER, 35, true, false",
    "VAMPIRE, 8, false, true",
    "VAMPIRE, 40, false, true",
    "BOOK, 0, false, false"
  })
  @DisplayName("a player carries a journal wherever it sits in the inventory, and only their own")
  void carries(final String held, final int slot, final boolean hunter, final boolean vampire) {
    final PlayerEntity player = mock(PlayerEntity.class);
    final PlayerInventory inventory = new PlayerInventory(player);
    when(player.getInventory()).thenReturn(inventory);
    inventory.setStack(
        slot,
        switch (held) {
          case "HUNTER" -> new ItemStack(JournalItems.HUNTER_JOURNAL);
          case "VAMPIRE" -> new ItemStack(JournalItems.VAMPIRE_JOURNAL);
          case "BOOK" -> new ItemStack(Items.WRITTEN_BOOK);
          default -> ItemStack.EMPTY;
        });
    assertEquals(hunter, Journals.carries(player, Journal.HUNTER));
    assertEquals(vampire, Journals.carries(player, Journal.VAMPIRE));
  }

  @ParameterizedTest(name = "filled {0}, selected {1} -> slot {2}")
  @CsvSource({
    "'', 0, 1",
    "'', 4, 0",
    "'0', 1, 2",
    "'1,2,3', 0, 4",
    "'0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35', 0, -1",
    "'0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34', 35, -1"
  })
  @DisplayName("a given journal goes in the first free slot but never the empty hand in use")
  void spareSlot(final String filled, final int selected, final int expected) {
    final List<ItemStack> main = new ArrayList<>();
    for (int slot = 0; slot < 36; slot++) {
      main.add(ItemStack.EMPTY);
    }
    if (!filled.isEmpty()) {
      for (final String slot : filled.split(",")) {
        main.set(Integer.parseInt(slot), new ItemStack(Items.BREAD));
      }
    }
    assertEquals(expected, Journals.spareSlot(main, selected));
  }
}
