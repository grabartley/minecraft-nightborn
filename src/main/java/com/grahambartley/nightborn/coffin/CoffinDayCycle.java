package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public final class CoffinDayCycle {

  public static final String RESTING = "nightborn.coffin.resting";
  public static final String SKIPPING = "nightborn.coffin.skipping";

  private static final Map<RegistryKey<World>, Integer> LAST_RESTING = new HashMap<>();

  private CoffinDayCycle() {}

  static void register() {
    ServerTickEvents.END_WORLD_TICK.register(CoffinDayCycle::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> LAST_RESTING.clear());
  }

  static void tick(final ServerWorld world) {
    settle(
        world,
        world.getPlayers(player -> !player.isSpectator() && Vampires.isVampire(player)),
        ServerConfigService.current().coffins().sleepShare());
  }

  public static void settle(
      final ServerWorld world, final List<ServerPlayerEntity> vampires, final int share) {
    final List<ServerPlayerEntity> resting =
        vampires.stream().filter(CoffinSleepers::inCoffin).toList();
    final int needed = DaySkip.required(vampires.size(), share);
    final Integer last = LAST_RESTING.put(world.getRegistryKey(), resting.size());
    if (!resting.isEmpty() && (last == null || last != resting.size())) {
      tell(vampires, Text.translatable(RESTING, resting.size(), needed));
    }
    final int ready =
        (int) resting.stream().filter(ServerPlayerEntity::canResetTimeBySleeping).count();
    if (!world.isDay() || !DaySkip.skips(ready, vampires.size(), share)) {
      return;
    }
    if (world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
      world.setTimeOfDay(DaySkip.nextDusk(world.getTimeOfDay()));
      tell(vampires, Text.translatable(SKIPPING));
    }
    resting.forEach(player -> player.wakeUp(false, false));
  }

  private static void tell(final List<ServerPlayerEntity> vampires, final Text message) {
    vampires.forEach(player -> player.sendMessage(message, true));
  }
}
