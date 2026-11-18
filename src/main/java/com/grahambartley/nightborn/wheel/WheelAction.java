package com.grahambartley.nightborn.wheel;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public interface WheelAction<T extends Entity> {

  Identifier id();

  boolean allows(ServerPlayerEntity player, T target);

  List<WheelEntry> entries(ServerPlayerEntity player, T target);

  void perform(ServerPlayerEntity player, T target, WheelEntry chosen);
}
