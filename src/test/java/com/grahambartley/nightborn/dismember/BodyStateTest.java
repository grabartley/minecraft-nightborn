package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.Arrays;
import java.util.List;
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
        BodyState.WHOLE.cut(BodyPart.LEFT_ARM, 100L, 0),
        new BodyState(
            Map.of(BodyPart.RIGHT_LEG, SILVER, BodyPart.HEAD, LostPart.missing(3L, 0)), 3L));
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
    final BodyState body = BodyState.WHOLE.cut(part, 40L, 0);
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
    assertEquals(recent, BodyState.WHOLE.cut(BodyPart.LEFT_LEG, cutAt, 0).cutRecently(now, 200L));
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
            Map.of(BodyPart.RIGHT_LEG, SILVER, BodyPart.LEFT_ARM, LostPart.missing(3L, 0)), 3L);
    assertEquals(
        new BodyState(Map.of(BodyPart.RIGHT_LEG, SILVER), BodyState.NEVER), body.afterDeath());
    assertTrue(BodyState.WHOLE.cut(BodyPart.HEAD, 1L, 0).afterDeath().whole());
  }

  @ParameterizedTest(name = "now {0}: open {1}")
  @CsvSource({
    "99, ''",
    "100, 'left_arm right_leg'",
    "299, 'left_arm right_leg'",
    "300, 'right_leg'",
    "700, ''"
  })
  @DisplayName("open wounds are the missing parts still inside their bleed time")
  void openWounds(final long now, final String open) {
    final BodyState body = twoWounds();
    final List<BodyPart> expected =
        open.isBlank()
            ? List.of()
            : Arrays.stream(open.split(" "))
                .map(name -> BodyPart.named(name).orElseThrow())
                .toList();
    assertEquals(expected, body.openWounds(now));
    assertEquals(!expected.isEmpty(), body.bleeding(now));
  }

  @ParameterizedTest(name = "closed at {0}")
  @CsvSource({"100", "250", "299"})
  @DisplayName("closing every wound stops all bleeding and keeps the parts missing")
  void closeWounds(final long now) {
    final BodyState shut = twoWounds().closeWounds(now);
    assertFalse(shut.bleeding(now));
    assertTrue(shut.missing(BodyPart.LEFT_ARM));
    assertTrue(shut.missing(BodyPart.RIGHT_LEG));
    assertEquals(SILVER, shut.parts().get(BodyPart.HEAD));
    assertEquals(150L, shut.lastCutAt());
  }

  @Test
  @DisplayName("closing a body that is not bleeding returns it unchanged")
  void closeWoundsWhenDry() {
    final BodyState body = twoWounds();
    assertSame(body, body.closeWounds(800L));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BodyPart.class)
  @DisplayName("the newest cut is the part lost at the last cut")
  void newestCut(final BodyPart part) {
    final BodyState body = BodyState.WHOLE.cut(BodyPart.HEAD, 10L, 0).cut(part, 90L, 0);
    assertEquals(Optional.of(part), body.newestCut());
  }

  @Test
  @DisplayName("a whole body or one with only silver limbs has no newest cut")
  void noNewestCut() {
    assertEquals(Optional.empty(), BodyState.WHOLE.newestCut());
    assertEquals(Optional.empty(), new BodyState(Map.of(BodyPart.HEAD, SILVER), 5L).newestCut());
  }

  @ParameterizedTest(name = "second cut at {0}")
  @CsvSource({"130", "260"})
  @DisplayName("a second cut moves the pulse clock for every open wound to that cut")
  void secondCutRestartsThePulse(final long second) {
    final BodyState body =
        BodyState.WHOLE.cut(BodyPart.LEFT_ARM, 100L, 1200).cut(BodyPart.RIGHT_LEG, second, 1200);
    assertEquals(second, body.lastCutAt());
    assertTrue(BleedRules.pulses(body.lastCutAt(), second + 60L, 3));
    assertFalse(BleedRules.pulses(body.lastCutAt(), 160L, 3), "the first cut's pulse is gone");
  }

  private static BodyState twoWounds() {
    return new BodyState(
        Map.of(
            BodyPart.LEFT_ARM,
            LostPart.missing(100L, 200),
            BodyPart.RIGHT_LEG,
            LostPart.missing(100L, 600),
            BodyPart.HEAD,
            SILVER),
        150L);
  }
}
