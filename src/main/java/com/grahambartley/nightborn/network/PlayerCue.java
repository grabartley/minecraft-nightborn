package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.mob.VampirePose;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

public enum PlayerCue {
  VOMIT,
  DRAIN,
  FINISHER,
  SMALL_FLINCH(VampirePose.SMALL_FLINCH),
  RECOIL_GARLIC(VampirePose.RECOIL_GARLIC),
  RECOIL_CRUCIFIX(VampirePose.RECOIL_CRUCIFIX),
  WAKE_STRETCH(VampirePose.WAKE_STRETCH),
  LIGHT_CANDLE(VampirePose.LIGHT_CANDLE),
  DOUSE_CANDLE(VampirePose.DOUSE_CANDLE),
  START_GRAMOPHONE(VampirePose.START_GRAMOPHONE);

  private static final IntFunction<PlayerCue> BY_INDEX =
      ValueLists.createIdToValueFunction(
          PlayerCue::ordinal, values(), ValueLists.OutOfBoundsHandling.ZERO);

  public static final PacketCodec<ByteBuf, PlayerCue> PACKET_CODEC =
      PacketCodecs.indexed(BY_INDEX, PlayerCue::ordinal);

  private final Optional<VampirePose> gesture;

  PlayerCue() {
    this.gesture = Optional.empty();
  }

  PlayerCue(final VampirePose gesture) {
    this.gesture = Optional.of(gesture);
  }

  public Optional<VampirePose> gesture() {
    return gesture;
  }

  public static Optional<PlayerCue> of(final VampirePose gesture) {
    return Arrays.stream(values())
        .filter(cue -> cue.gesture.filter(gesture::equals).isPresent())
        .findFirst();
  }
}
