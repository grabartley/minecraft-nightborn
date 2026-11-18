package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.network.ChooseWheelPayload;
import com.grahambartley.nightborn.wheel.CommandWheel;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

final class WheelUse {

  private WheelUse() {}

  static boolean pick(
      final ServerPlayerEntity player,
      final Entity target,
      final Identifier action,
      final int argument) {
    return CommandWheel.open(player, target)
        && CommandWheel.choose(player, new ChooseWheelPayload(target.getId(), action, argument));
  }

  static boolean forge(
      final ServerPlayerEntity player,
      final Entity target,
      final Identifier action,
      final int argument) {
    return CommandWheel.choose(player, new ChooseWheelPayload(target.getId(), action, argument));
  }
}
