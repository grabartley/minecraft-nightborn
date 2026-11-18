package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryLootTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"nightborn:pick_unowned"})
  @DisplayName("loot tables can name the pick of a piece a striker lacks")
  void registered(final String id) {
    assertEquals(id, Registries.LOOT_FUNCTION_TYPE.getId(AccessoryLoot.PICK_UNOWNED).toString());
  }
}
