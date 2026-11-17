package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.decor.ChaliceBlood;
import com.grahambartley.nightborn.decor.ChaliceContents;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RitualAltarBlockEntityTest {

  private static final DynamicRegistryManager REGISTRIES =
      DynamicRegistryManager.of(Registries.REGISTRIES);

  private static RitualAltarBlockEntity altar() {
    return new RitualAltarBlockEntity(BlockPos.ORIGIN, RitualBlocks.ALTAR.getDefaultState());
  }

  static Stream<Arguments> chalices() {
    return Stream.of(
        Arguments.of(Optional.empty(), false),
        Arguments.of(Optional.of(ChaliceContents.EMPTY), false),
        Arguments.of(Optional.of(new ChaliceContents(ChaliceBlood.ANIMAL, 3)), false),
        Arguments.of(Optional.of(new ChaliceContents(ChaliceBlood.PLAYER, 7)), true));
  }

  @ParameterizedTest(name = "{0}, ritual running {1}")
  @MethodSource("chalices")
  @DisplayName("the chalice, its blood and a running ritual survive a save and load")
  void roundTrips(final Optional<ChaliceContents> chalice, final boolean running) {
    final RitualAltarBlockEntity altar = altar();
    chalice.ifPresent(altar::setChalice);
    final UUID caller = UUID.randomUUID();
    if (running) {
      altar.begin(200, caller);
    }
    final NbtCompound nbt = altar.createNbt(REGISTRIES);
    final RitualAltarBlockEntity loaded = altar();
    loaded.setChalice(new ChaliceContents(ChaliceBlood.VILLAGER, 1));
    loaded.read(nbt, REGISTRIES);
    assertEquals(chalice, loaded.chalice());
    assertEquals(running, loaded.active());
    assertEquals(running ? Optional.of(caller) : Optional.empty(), loaded.summoner());
  }

  @ParameterizedTest(name = "{0} relics left on an old altar")
  @ValueSource(ints = {0, 1, 4})
  @DisplayName("an altar from an older world keeps the relics left on it until it hands them back")
  void readsOldRelics(final int count) {
    final NbtCompound nbt = altar().createNbt(REGISTRIES);
    final NbtList relics = new NbtList();
    RitualItems.ALL.subList(0, count).forEach(r -> relics.add(new ItemStack(r).encode(REGISTRIES)));
    nbt.put(RitualAltarBlockEntity.RELICS_KEY, relics);
    final RitualAltarBlockEntity loaded = altar();
    loaded.read(nbt, REGISTRIES);
    final List<Item> kept = loaded.oldRelics().stream().map(ItemStack::getItem).toList();
    assertEquals(RitualItems.ALL.subList(0, count), kept);
    assertTrue(loaded.chalice().isEmpty());
    final RitualAltarBlockEntity again = altar();
    again.read(loaded.createNbt(REGISTRIES), REGISTRIES);
    assertEquals(count, again.oldRelics().size());
  }

  @Test
  @DisplayName("taking the chalice hands back its blood and leaves the altar bare")
  void takesTheChalice() {
    final RitualAltarBlockEntity altar = altar();
    assertTrue(altar.takeChalice().isEmpty());
    final ChaliceContents held = new ChaliceContents(ChaliceBlood.VILLAGER, 5);
    altar.setChalice(held);
    assertEquals(Optional.of(held), altar.takeChalice());
    assertTrue(altar.chalice().isEmpty());
  }

  @Test
  @DisplayName("an altar is idle until its ritual begins")
  void begins() {
    final RitualAltarBlockEntity altar = altar();
    assertFalse(altar.active());
    altar.begin(40, null);
    assertTrue(altar.active());
    assertTrue(altar.summoner().isEmpty());
  }
}
