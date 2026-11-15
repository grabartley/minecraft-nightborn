package com.grahambartley.nightborn.coating;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.config.NightbornConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoatingsTest {

  @ParameterizedTest(name = "coat {0}, {1} used, started {2}, now {3}: live {4}")
  @CsvSource({
    "false, 0, -1, 0, false",
    "true, 0, -1, 99999, true",
    "true, 19, 0, 100, true",
    "true, 20, 0, 100, false",
    "true, 0, 0, 6000, false"
  })
  @DisplayName("a weapon shows its coat only while the coat has hits and time left")
  void live(
      final boolean coated,
      final int used,
      final long started,
      final long now,
      final boolean live) {
    final ItemStack sword = new ItemStack(Items.IRON_SWORD);
    if (coated) {
      sword.set(Coatings.COAT, new Coat(Coating.HOLY_WATER, used, started));
    }
    assertEquals(live, Coatings.live(sword, NightbornConfig.DEFAULTS, now).isPresent());
    assertEquals(coated, Coatings.of(sword).isPresent());
  }
}
