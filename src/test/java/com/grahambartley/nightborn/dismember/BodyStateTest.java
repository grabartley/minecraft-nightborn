package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class BodyStateTest {

  private static final LostPart SILVER =
      new LostPart(LimbCondition.REPLACED, 5L, 0, Optional.empty());

  static Stream<BodyState> bodies() {
    return Stream.of(
        BodyState.WHOLE,
        BodyState.WHOLE.cut(BodyPart.LEFT_ARM, 100L),
        new BodyState(Map.of(BodyPart.RIGHT_LEG, SILVER, BodyPart.HEAD, LostPart.missing(3L)), 3L));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("bodies")
  @DisplayName("a body is saved and read back unchanged")
  void saved(final BodyState body) {
    assertEquals(
        body,
        BodyState.CODEC
            .parse(
                JsonOps.INSTANCE, BodyState.CODEC.encodeStart(JsonOps.INSTANCE, body).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("bodies")
  @DisplayName("a body crosses the network unchanged")
  void synced(final BodyState body) {
    final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
    BodyState.PACKET_CODEC.encode(buf, body);
    assertEquals(body, BodyState.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BodyPart.class)
  @DisplayName("cutting a part marks only that part missing and stamps the time")
  void cut(final BodyPart part) {
    final BodyState body = BodyState.WHOLE.cut(part, 40L);
    for (final BodyPart other : BodyPart.values()) {
      assertEquals(other == part, body.missing(other), other.asString());
    }
    assertEquals(40L, body.lastCutAt());
    assertFalse(body.whole());
  }

  @Test
  @DisplayName("a whole part is never stored, so a whole body stays empty")
  void wholeNotStored() {
    final BodyState body =
        new BodyState(
            Map.of(BodyPart.HEAD, new LostPart(LimbCondition.WHOLE, 1L, 0, Optional.empty())), 1L);
    assertTrue(body.whole());
    assertEquals(LimbCondition.WHOLE, body.condition(BodyPart.HEAD));
  }

  @ParameterizedTest(name = "cut at {0}, now {1}: recent {2}")
  @CsvSource({"100, 100, true", "100, 299, true", "100, 300, false", "0, 5000, false"})
  @DisplayName("a cut counts as recent for the cooldown and no longer")
  void cutRecently(final long cutAt, final long now, final boolean recent) {
    assertEquals(recent, BodyState.WHOLE.cut(BodyPart.LEFT_LEG, cutAt).cutRecently(now, 200L));
  }

  @Test
  @DisplayName("a body that was never cut is never cut recently")
  void neverCut() {
    assertFalse(BodyState.WHOLE.cutRecently(0L, 200L));
  }

  @Test
  @DisplayName("death keeps silver limbs and drops every missing part")
  void afterDeath() {
    final BodyState body =
        new BodyState(
            Map.of(BodyPart.RIGHT_LEG, SILVER, BodyPart.LEFT_ARM, LostPart.missing(3L)), 3L);
    assertEquals(
        new BodyState(Map.of(BodyPart.RIGHT_LEG, SILVER), BodyState.NEVER), body.afterDeath());
    assertTrue(BodyState.WHOLE.cut(BodyPart.HEAD, 1L).afterDeath().whole());
  }
}
