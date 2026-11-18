package com.grahambartley.nightborn.mind;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringIdentifiable;

public enum CommandOrder implements StringIdentifiable {
  FOLLOW,
  ATTACK,
  OPEN_DOOR;

  public static final Codec<CommandOrder> CODEC =
      StringIdentifiable.createCodec(CommandOrder::values);

  public static CommandOrder pick(final boolean lookingAtDoor, final boolean lookingAtFoe) {
    if (lookingAtFoe) {
      return ATTACK;
    }
    return lookingAtDoor ? OPEN_DOOR : FOLLOW;
  }

  public String translationKey() {
    return "nightborn.command." + asString();
  }

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }
}
