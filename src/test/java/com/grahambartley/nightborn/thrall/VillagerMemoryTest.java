package com.grahambartley.nightborn.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.village.TradeOfferList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class VillagerMemoryTest {

  @ParameterizedTest(name = "{0} xp")
  @ValueSource(ints = {0, 30, 250})
  @DisplayName("a thrall saves and loads the villager's experience and gossip")
  void roundTrips(final int experience) {
    final NbtList gossip = new NbtList();
    gossip.add(NbtString.of("remembered"));
    final VillagerMemory memory = new VillagerMemory(new TradeOfferList(), gossip, experience);
    final VillagerMemory loaded =
        VillagerMemory.read(memory.write(new NbtCompound(), NbtOps.INSTANCE), NbtOps.INSTANCE);
    assertEquals(experience, loaded.experience());
    assertEquals(gossip, loaded.gossip());
    assertTrue(loaded.offers().isEmpty());
  }

  @Test
  @DisplayName("a thrall saved before it kept any memory loads with none")
  void readsNothing() {
    final VillagerMemory loaded = VillagerMemory.read(new NbtCompound(), NbtOps.INSTANCE);
    assertEquals(0, loaded.experience());
    assertEquals(new NbtList(), loaded.gossip());
    assertTrue(loaded.offers().isEmpty());
  }
}
