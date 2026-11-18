package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.accessory.AccessoryItems;
import com.grahambartley.nightborn.accessory.AccessoryPiece;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DressedTest {

  @ParameterizedTest(name = "{0} dyed {1}: {2}")
  @CsvSource({
    "top_hat, , 1C1A1D",
    "top_hat, 123456, 123456",
    "mourning_veil, , 1A1719",
    "high_collared_cape, B02E26, B02E26"
  })
  @DisplayName("a worn piece shows its dye, or its own colour when undyed")
  void colour(final String id, final String dye, final String shown) {
    final Item item =
        id.equals("top_hat")
            ? ClothingItems.of(ClothingPiece.TOP_HAT)
            : AccessoryItems.of(AccessoryPiece.valueOf(id.toUpperCase(java.util.Locale.ROOT)));
    final ItemStack stack = new ItemStack(item);
    if (dye != null) {
      stack.set(
          DataComponentTypes.DYED_COLOR, new DyedColorComponent(Integer.parseInt(dye, 16), true));
    }
    assertEquals(Integer.parseInt(shown, 16), ((Dressed) item).color(stack) & 0xFFFFFF);
  }
}
