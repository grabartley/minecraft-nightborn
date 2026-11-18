package com.grahambartley.nightborn.house.crest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class CrestChargeTest {

  private static final Path CHARGES =
      Path.of("src/main/resources/assets/nightborn/textures/block/crest_charge");

  @Test
  @DisplayName("the crest charges are the twelve the design locks, in its order")
  void lockedList() {
    assertEquals(
        List.of(
            "bat",
            "chalice",
            "rose",
            "tower",
            "key",
            "crescent_moon",
            "raven",
            "serpent",
            "crown",
            "dagger",
            "thorn_branch",
            "skull"),
        Arrays.stream(CrestCharge.values()).map(CrestCharge::asString).toList());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CrestCharge.class)
  @DisplayName("every charge has its mask and its outline, and survives a save and a packet")
  void artAndCodecs(final CrestCharge charge) {
    assertEquals(true, Files.isRegularFile(CHARGES.resolve(charge.asString() + ".png")));
    assertEquals(true, Files.isRegularFile(CHARGES.resolve(charge.asString() + "_outline.png")));
    assertEquals(
        charge,
        CrestCharge.CODEC
            .parse(JsonOps.INSTANCE, new JsonPrimitive(charge.asString()))
            .getOrThrow());
    final ByteBuf buf = Unpooled.buffer();
    CrestCharge.PACKET_CODEC.encode(buf, charge);
    assertEquals(charge, CrestCharge.PACKET_CODEC.decode(buf));
    assertEquals("nightborn.crest.charge." + charge.asString(), charge.translationKey());
  }

  @ParameterizedTest(name = "{0} stepped {1} -> {2}")
  @CsvSource({
    "BAT, 1, CHALICE",
    "BAT, -1, SKULL",
    "SKULL, 1, BAT",
    "ROSE, 12, ROSE",
    "KEY, 0, KEY"
  })
  @DisplayName("stepping through the charges wraps round both ends")
  void step(final CrestCharge from, final int by, final CrestCharge to) {
    assertEquals(to, from.step(by));
  }
}
