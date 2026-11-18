package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.house.crest.Crest;
import com.grahambartley.nightborn.house.crest.CrestCharge;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NameHousePayloadTest {

  @ParameterizedTest(name = "{0}, {1}")
  @CsvSource({
    "Valcourt, RAVEN, PURPLE, YELLOW",
    "'', BAT, RED, BLACK",
    "House of Thorns, SKULL, WHITE, WHITE"
  })
  @DisplayName("the house name and the crest cross unchanged")
  void roundTrips(
      final String name, final CrestCharge charge, final DyeColor field, final DyeColor tincture) {
    final NameHousePayload payload = new NameHousePayload(name, new Crest(charge, field, tincture));
    final ByteBuf buf = Unpooled.buffer();
    NameHousePayload.CODEC.encode(buf, payload);
    assertEquals(payload, NameHousePayload.CODEC.decode(buf));
  }
}
