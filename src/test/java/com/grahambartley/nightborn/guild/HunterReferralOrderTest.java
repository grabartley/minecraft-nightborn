package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.wheel.WheelEntry;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterReferralOrderTest {

  @ParameterizedTest(name = "{0}, {1}, {2}")
  @CsvSource({"nightborn:hunter_referral, nightborn:wheel/place, nightborn.hunter.ask_master"})
  @DisplayName("asking for the Guild Master is one entry with the map icon")
  void entry(final String id, final String icon, final String label) {
    final HunterReferralOrder order = new HunterReferralOrder();
    final WheelEntry entry = order.entries(null, null).getFirst();
    assertEquals(id, order.id().toString());
    assertEquals(id, entry.action().toString());
    assertEquals(icon, entry.icon().toString());
    assertEquals(label, ((TranslatableTextContent) entry.label().getContent()).getKey());
    assertEquals(1, order.entries(null, null).size());
  }
}
