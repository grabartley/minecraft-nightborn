package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClothingFeaturesTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("registering clothing brings in every piece")
  void registersEveryPiece(final ClothingPiece piece) {
    assertTrue(Registries.ITEM.containsId(Identifier.of("nightborn", piece.id())));
  }

  @Test
  @DisplayName("registering clothing brings in the night sight power")
  void registersNightSight() {
    assertTrue(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE.containsId(NightSightEffect.ID));
  }
}
