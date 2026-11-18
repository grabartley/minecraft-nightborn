package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.blood.BloodItems;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WornStoresTest {

  static Stream<Arguments> stores() {
    return Stream.of(
        Arguments.of("empty", List.of(), false),
        Arguments.of("a blood bottle", List.of(new ItemStack(BloodItems.BLOOD_BOTTLE)), true),
        Arguments.of("a glass bottle", List.of(new ItemStack(Items.GLASS_BOTTLE)), false));
  }

  @ParameterizedTest(name = "{0}: {2}")
  @MethodSource("stores")
  @DisplayName("only a stored Blood Bottle is drunk when the bar runs dry")
  void drinkable(final String name, final List<ItemStack> held, final boolean drinkable) {
    assertEquals(drinkable, WornStores.drinkable(new BundleContentsComponent(held)));
  }
}
