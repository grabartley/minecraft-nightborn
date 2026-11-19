package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.entity.effect.StatusEffects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PrayerGiftTest {

  @ParameterizedTest(name = "weakness gifts {0}, poison gifts {1} -> {2} cleared")
  @CsvSource({"0, 0, 0", "1, 0, 1", "2, 0, 1", "1, 1, 2"})
  @DisplayName("each effect the held gifts clear is cleared once")
  void cleared(final int weakness, final int poison, final int expected) {
    final List<PrayerGift> gifts =
        Stream.concat(
                Stream.generate(() -> new PrayerGift(List.of(StatusEffects.WEAKNESS)))
                    .limit(weakness),
                Stream.generate(() -> new PrayerGift(List.of(StatusEffects.POISON))).limit(poison))
            .toList();
    assertEquals(expected, PrayerGift.cleared(gifts).size());
  }
}
