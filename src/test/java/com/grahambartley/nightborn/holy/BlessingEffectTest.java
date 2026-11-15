package com.grahambartley.nightborn.holy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BlessingEffectTest {

  @Test
  @DisplayName("a blessing lands at once, like an instant potion")
  void instant() {
    assertEquals("nightborn:blessing", HolyWater.BLESSING.getIdAsString());
    assertTrue(HolyWater.BLESSING.value().isInstant());
  }

  static Stream<Item> bottles() {
    return Stream.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("bottles")
  @DisplayName("holy water tints its bottle white so the drawn holy water shows true")
  void untinted(final Item bottle) {
    assertEquals(
        0xFFFFFFFF,
        PotionContentsComponent.createStack(bottle, HolyWater.POTION)
            .get(DataComponentTypes.POTION_CONTENTS)
            .getColor());
  }
}
