package com.grahambartley.nightborn.wheel;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record WheelEntry(
    Identifier action, int argument, Identifier icon, Text label, boolean current) {

  public static final int NO_ARGUMENT = -1;

  public static final PacketCodec<RegistryByteBuf, WheelEntry> CODEC =
      PacketCodec.tuple(
          Identifier.PACKET_CODEC,
          WheelEntry::action,
          PacketCodecs.VAR_INT,
          WheelEntry::argument,
          Identifier.PACKET_CODEC,
          WheelEntry::icon,
          TextCodecs.REGISTRY_PACKET_CODEC,
          WheelEntry::label,
          PacketCodecs.BOOL,
          WheelEntry::current,
          WheelEntry::new);

  public static WheelEntry of(
      final Identifier action, final Identifier icon, final Text label, final boolean current) {
    return new WheelEntry(action, NO_ARGUMENT, icon, label, current);
  }

  public boolean names(final Identifier chosenAction, final int chosenArgument) {
    return action.equals(chosenAction) && argument == chosenArgument;
  }
}
