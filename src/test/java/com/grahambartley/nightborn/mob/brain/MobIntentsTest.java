package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MobIntentsTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Intent.class)
  @DisplayName("each intent's budget file is named after it")
  void id(final Intent intent) {
    assertEquals(Identifier.of("nightborn", intent.key()), MobIntents.id(intent));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Intent.class)
  @DisplayName("an intent with no budget loaded is free")
  void freeWithoutData(final Intent intent) {
    assertEquals(IntentBudget.FREE, MobIntents.budget(DynamicRegistryManager.EMPTY, intent));
  }
}
