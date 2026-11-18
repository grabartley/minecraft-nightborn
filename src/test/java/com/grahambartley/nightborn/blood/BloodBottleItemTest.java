package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.config.NightbornConfig;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class BloodBottleItemTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"ANIMAL, 1", "VILLAGER, 2", "PLAYER, 4"})
  @DisplayName("a bottle of 4 health restores quality times 60% by default")
  void restoresByQuality(final BloodQuality quality, final int blood) {
    assertEquals(blood, BloodBottleItem.bloodRestored(quality, NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BloodQuality.class)
  @DisplayName("bottled blood restores less than the same blood drunk fresh")
  void bottledIsWorthLessThanFresh(final BloodQuality quality) {
    final NightbornConfig config = NightbornConfig.DEFAULTS;
    final float fresh =
        BloodValue.worth(
            config.feeding().bottleSize(), quality.worthPercent(config), BloodValue.FRESH_PERCENT);
    assertTrue(BloodBottleItem.bloodRestored(quality, config) < fresh);
  }

  @ParameterizedTest(name = "{0} with gift {1}")
  @CsvSource({"ANIMAL, nightborn:horse", "VILLAGER, nightborn:villager", "PLAYER, ''"})
  @DisplayName("a bottle keeps the quality and the gift of the blood it holds")
  void keepsQualityAndGift(final BloodQuality quality, final String gift) {
    final Optional<Identifier> kept =
        gift.isEmpty() ? Optional.empty() : Optional.of(Identifier.of(gift));
    final ItemStack bottle = BloodBottleItem.of(quality, kept);
    assertEquals(quality, BloodBottleItem.qualityOf(bottle));
    assertEquals(kept, BloodBottleItem.giftOf(bottle));
  }
}
