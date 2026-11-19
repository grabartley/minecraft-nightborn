package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterTentBlockEntityTest {

  private static final DynamicRegistryManager REGISTRIES =
      DynamicRegistryManager.of(Registries.REGISTRIES);

  private static HunterTentBlockEntity tent() {
    return new HunterTentBlockEntity(BlockPos.ORIGIN, TentBlocks.HUNTER_TENT.getDefaultState());
  }

  @ParameterizedTest(name = "pitched at {0}")
  @ValueSource(longs = {0L, 13_000L, 2_000_000L})
  @DisplayName("a tent keeps the time it went up through later loads and a save")
  void keepsWhenItWentUp(final long pitchedAt) {
    final HunterTentBlockEntity tent = tent();
    tent.pitch(pitchedAt);
    tent.pitch(pitchedAt + 500L);
    assertEquals(30L, tent.age(pitchedAt + 30L));
    final NbtCompound nbt = tent.createNbt(REGISTRIES);
    final HunterTentBlockEntity loaded = tent();
    loaded.read(nbt, REGISTRIES);
    assertEquals(30L, loaded.age(pitchedAt + 30L));
  }

  @ParameterizedTest(name = "now {0}")
  @ValueSource(longs = {0L, 24_000L})
  @DisplayName("a tent with no known pitch time counts as long standing")
  void unknownIsOld(final long now) {
    assertEquals(Long.MAX_VALUE, tent().age(now));
  }
}
