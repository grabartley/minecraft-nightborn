package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Map;
import java.util.Optional;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MobActionsTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"nightborn:leap", "nightborn:holy_water", "nightborn:unknown"})
  @DisplayName("with no action data loaded, no action is found and none are listed")
  void emptyWithoutData(final String id) {
    assertEquals(Optional.empty(), MobActions.get(DynamicRegistryManager.EMPTY, Identifier.of(id)));
    assertEquals(Map.of(), MobActions.all(DynamicRegistryManager.EMPTY));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"nightborn:mob_action"})
  @DisplayName("built-in actions live in their own data folder")
  void folder(final String id) {
    assertEquals(Identifier.of(id), MobActions.ACTION.getValue());
  }
}
