package com.grahambartley.nightborn.house.crest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CrestTest {

  @ParameterizedTest(name = "{0}, {1} on {2}")
  @CsvSource({"BAT, RED, BLACK", "SKULL, WHITE, WHITE", "THORN_BRANCH, PURPLE, YELLOW"})
  @DisplayName("a crest of any charge and any two colours survives a save and a packet")
  void roundTrips(final CrestCharge charge, final DyeColor field, final DyeColor tincture) {
    final Crest crest = new Crest(charge, field, tincture);
    assertEquals(
        crest,
        Crest.CODEC
            .parse(JsonOps.INSTANCE, Crest.CODEC.encodeStart(JsonOps.INSTANCE, crest).getOrThrow())
            .getOrThrow());
    final ByteBuf buf = Unpooled.buffer();
    Crest.PACKET_CODEC.encode(buf, crest);
    assertEquals(crest, Crest.PACKET_CODEC.decode(buf));
  }
}
