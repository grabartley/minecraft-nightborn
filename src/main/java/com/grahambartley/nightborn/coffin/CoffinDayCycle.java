package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.core.NightTime;
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

  private static final Map<RegistryKey<World>, RestTally> LAST_TALLY = new HashMap<>();

  private CoffinDayCycle() {}

  public static void register() {
    ServerTickEvents.END_WORLD_TICK.register(CoffinDayCycle::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> LAST_TALLY.clear());
  }

  private static void tick(final ServerWorld world) {
    settle(
        world,
        world.getPlayers(CoffinDayCycle::counts),
        ServerConfigService.current().coffins().sleepShare());
  }

  private static boolean counts(final ServerPlayerEntity player) {
    return !player.isSpectator() && Vampires.isVampire(player);
  }

  private static void settle(
      final ServerWorld world, final List<ServerPlayerEntity> vampires, final int share) {
    final List<ServerPlayerEntity> resting =
        vampires.stream().filter(CoffinSleepers::inCoffin).toList();
    final RestTally tally = new RestTally(resting.size(), DaySkip.required(vampires.size(), share));
    if (tally.announces(LAST_TALLY.put(world.getRegistryKey(), tally))) {
      tell(vampires, Text.translatable(RESTING, tally.resting(), tally.needed()));
    }
    final int ready =
        (int) resting.stream().filter(ServerPlayerEntity::canResetTimeBySleeping).count();
    if (NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay())
        || !DaySkip.skips(ready, vampires.size(), share)) {
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
