package com.grahambartley.nightborn.wild;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RumourMapsTest {

  @BeforeAll
  static void register() {
    RumourMaps.register();
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"cartographer", "cleric"})
  @DisplayName("a novice cartographer and a novice cleric sell a map to the nearest vampire house")
  void sold(final String profession) {
    final VillagerProfession seller =
        "cleric".equals(profession) ? VillagerProfession.CLERIC : VillagerProfession.CARTOGRAPHER;
    assertTrue(
        java.util.Arrays.stream(
                TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(seller).get(RumourMaps.LEVEL))
            .anyMatch(factory -> factory instanceof TradeOffers.SellMapFactory));
  }
}
