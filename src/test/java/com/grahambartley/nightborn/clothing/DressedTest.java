package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DressedTest {

  @ParameterizedTest(name = "{0} dyed {1}: {2}")
  @CsvSource({
    "TOP_HAT, , 1C1A1D",
    "TOP_HAT, 123456, 123456",
    "WIDE_BRIMMED_HAT, , 211D1E",
    "BUCKLED_SHOES, , FFFFFF"
  })
  @DisplayName("a worn piece shows its dye, or its own colour when undyed")
  void colour(final ClothingPiece piece, final String dye, final String shown) {
    final Dressed item = ClothingItems.of(piece);
    final ItemStack stack = new ItemStack(ClothingItems.of(piece));
    if (dye != null) {
      stack.set(
          DataComponentTypes.DYED_COLOR, new DyedColorComponent(Integer.parseInt(dye, 16), true));
    }
    assertEquals(Integer.parseInt(shown, 16), item.color(stack, null) & 0xFFFFFF);
    assertEquals(piece.id(), item.artId());
  }
}
