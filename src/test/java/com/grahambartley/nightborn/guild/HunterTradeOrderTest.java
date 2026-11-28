package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.nightborn.wheel.WheelEntry;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterTradeOrderTest {

  @ParameterizedTest(name = "{0}, {1}, {2}")
  @CsvSource({"nightborn:hunter_trade, nightborn:wheel/trade, nightborn.guild.gear"})
  @DisplayName("trading with a hunter looks like the Guild Master's trade but is its own action")
  void entry(final String id, final String icon, final String label) {
    final HunterTradeOrder order = new HunterTradeOrder();
    final WheelEntry entry = order.entries(null, null).getFirst();
    assertEquals(id, order.id().toString());
    assertEquals(id, entry.action().toString());
    assertEquals(icon, entry.icon().toString());
    assertEquals(label, ((TranslatableTextContent) entry.label().getContent()).getKey());
    assertNotEquals(new GuildCounterOrders(GuildCounter.Kind.GEAR).id(), order.id());
  }

  @ParameterizedTest(name = "answers {0}, raiding {1} -> trades {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, false, false",
    "false, true, false"
  })
  @DisplayName("a hunter that would answer trades, unless it rides with a raiding party")
  void trades(final boolean answers, final boolean raiding, final boolean trades) {
    assertEquals(trades, HunterTradeOrder.trades(answers, raiding));
  }
}
