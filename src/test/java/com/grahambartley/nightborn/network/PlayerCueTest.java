package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.mob.VampirePose;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class PlayerCueTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(PlayerCue.class)
  @DisplayName("every cue crosses the network unchanged")
  void roundTrips(final PlayerCue cue) {
    final ByteBuf buf = Unpooled.buffer();
    PlayerCue.PACKET_CODEC.encode(buf, cue);
    assertEquals(cue, PlayerCue.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "index {0} -> {1}")
  @CsvSource({"-1, VOMIT", "10, VOMIT", "99, VOMIT"})
  @DisplayName("an index no cue owns falls back to the first cue instead of failing")
  void unknownIndex(final int index, final PlayerCue expected) {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.VAR_INT.encode(buf, index);
    assertEquals(expected, PlayerCue.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(PlayerCue.class)
  @DisplayName("a gesture cue finds its way back from the pose it plays")
  void gestureRoundTrips(final PlayerCue cue) {
    cue.gesture()
        .ifPresent(
            pose -> {
              assertTrue(pose.gesture(), pose.name());
              assertEquals(Optional.of(cue), PlayerCue.of(pose));
            });
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(VampirePose.class)
  @DisplayName("every gesture a vampire makes can be sent to a vampire player")
  void everyGestureHasACue(final VampirePose pose) {
    assertEquals(pose.gesture(), PlayerCue.of(pose).isPresent());
  }

  @ParameterizedTest(name = "{0}: gesture {1}")
  @CsvSource({"VOMIT, false", "DRAIN, false", "FINISHER, false", "RECOIL_GARLIC, true"})
  @DisplayName("vomiting, draining and the finisher are their own cues, not gestures")
  void ownCues(final PlayerCue cue, final boolean gesture) {
    assertEquals(gesture, cue.gesture().isPresent());
  }
}
