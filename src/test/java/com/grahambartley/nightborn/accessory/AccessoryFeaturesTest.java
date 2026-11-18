package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryFeaturesTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("com.grahambartley.nightborn.accessory.AccessoriesTest#ids")
  @DisplayName("registering accessories brings in every piece")
  void registersEveryPiece(final String piece) {
    assertTrue(Registries.ITEM.containsId(Identifier.of("nightborn", piece)));
  }
}
