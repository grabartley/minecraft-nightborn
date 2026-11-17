package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RitualAltarBlockEntityTest {

  private static RitualAltarBlockEntity altar() {
    return new RitualAltarBlockEntity(BlockPos.ORIGIN, RitualBlocks.ALTAR.getDefaultState());
  }

  private static List<Item> items(final RitualAltarBlockEntity altar) {
    return altar.relics().stream().map(ItemStack::getItem).toList();
  }

  @ParameterizedTest(name = "{0} relics, ritual running: {1}")
  @ValueSource(ints = {0, 1, 4})
  @DisplayName("relics and a running ritual survive a save and load")
  void roundTrips(final int count) {
    final DynamicRegistryManager registries = DynamicRegistryManager.of(Registries.REGISTRIES);
    final RitualAltarBlockEntity altar = altar();
    RitualItems.ALL.subList(0, count).forEach(relic -> altar.place(new ItemStack(relic)));
    if (count == 4) {
      altar.begin(200);
    }
    final NbtCompound nbt = altar.createNbt(registries);
    final RitualAltarBlockEntity loaded = altar();
    loaded.place(new ItemStack(RitualItems.LAIR_FANG));
    loaded.read(nbt, registries);
    assertEquals(RitualItems.ALL.subList(0, count), items(loaded));
    assertEquals(count == 4, loaded.active());
  }

  @Test
  @DisplayName("relics come back off in the reverse of the order they went on")
  void takesLastFirst() {
    final RitualAltarBlockEntity altar = altar();
    assertTrue(altar.take().isEmpty());
    altar.place(new ItemStack(RitualItems.CASTLE_SIGNET));
    altar.place(new ItemStack(RitualItems.CRYPT_RELIQUARY));
    assertTrue(altar.holds(RitualItems.CASTLE_SIGNET));
    assertFalse(altar.holds(RitualItems.MANOR_LOCKET));
    assertTrue(altar.take().isOf(RitualItems.CRYPT_RELIQUARY));
    assertEquals(List.of(RitualItems.CASTLE_SIGNET), items(altar));
  }

  @Test
  @DisplayName("an altar is idle until its ritual begins")
  void begins() {
    final RitualAltarBlockEntity altar = altar();
    assertFalse(altar.active());
    altar.begin(40);
    assertTrue(altar.active());
  }
}
