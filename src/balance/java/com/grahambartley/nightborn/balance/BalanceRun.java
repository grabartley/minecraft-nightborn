package com.grahambartley.nightborn.balance;

import com.google.gson.Gson;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

final class BalanceRun {

  private static final int TIME_OF_NIGHT = 18000;
  private static final Gson GSON = new Gson();

  private final MinecraftServer server;
  private final List<Arena> arenas = new ArrayList<>();
  private final long started = System.currentTimeMillis();
  private boolean finished;

  BalanceRun(final MinecraftServer server, final List<Matchup> matchups, final int rounds) {
    this.server = server;
    final ServerWorld world = server.getOverworld();
    quiet(server, world);
    for (int i = 0; i < matchups.size(); i++) {
      arenas.add(new Arena(world, matchups.get(i), i, rounds));
    }
    arenas.forEach(Arena::open);
    server.getTickManager().startSprint(Integer.MAX_VALUE);
  }

  void tick() {
    if (finished) {
      return;
    }
    server.getOverworld().setTimeOfDay(TIME_OF_NIGHT);
    arenas.forEach(Arena::tick);
    if (arenas.stream().allMatch(Arena::done)) {
      finished = true;
      write();
      server.stop(false);
    }
  }

  void afterDamage(final LivingEntity victim, final DamageSource source, final float taken) {
    final Entity attacker = source.getAttacker();
    if (attacker == null) {
      return;
    }
    for (final Arena arena : arenas) {
      final Side side = arena.sideOf(attacker);
      if (side != null && arena.sideOf(victim) == side.other()) {
        arena.tally().hit(side, melee(source), taken);
        return;
      }
    }
  }

  private static boolean melee(final DamageSource source) {
    return source.isDirect()
        && (source.isOf(DamageTypes.MOB_ATTACK)
            || source.isOf(DamageTypes.MOB_ATTACK_NO_AGGRO)
            || source.isOf(NightbornDamage.BITE));
  }

  private static void quiet(final MinecraftServer server, final ServerWorld world) {
    final GameRules rules = world.getGameRules();
    rules.get(GameRules.DO_MOB_SPAWNING).set(false, server);
    rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
    rules.get(GameRules.DO_WEATHER_CYCLE).set(false, server);
    rules.get(GameRules.DO_PATROL_SPAWNING).set(false, server);
    rules.get(GameRules.DO_TRADER_SPAWNING).set(false, server);
    rules.get(GameRules.DO_MOB_LOOT).set(false, server);
    server.setDifficulty(Difficulty.NORMAL, true);
    world.setWeather(Integer.MAX_VALUE, 0, false, false);
    world.setTimeOfDay(TIME_OF_NIGHT);
  }

  private static Map<String, Object> json(final RoundResult result) {
    final Map<String, Object> out = new LinkedHashMap<>();
    out.put("matchup", result.matchup());
    out.put("winner", result.winner().map(Side::name).orElse("DRAW"));
    out.put("ticks", result.ticks());
    out.put("blows", result.blows());
    out.put("tags", result.tags());
    return out;
  }

  private void write() {
    final List<RoundResult> results = new ArrayList<>();
    arenas.forEach(arena -> results.addAll(arena.results()));
    final BalanceReport report =
        new BalanceReport(arenas.stream().map(Arena::matchup).toList(), results);
    final Path dir = server.getRunDirectory();
    final StringBuilder lines = new StringBuilder();
    results.forEach(result -> lines.append(GSON.toJson(json(result))).append('\n'));
    try {
      Files.writeString(dir.resolve("balance-results.jsonl"), lines, StandardCharsets.UTF_8);
      Files.writeString(
          dir.resolve("balance-report.md"),
          report.markdown()
              + String.format(
                  "%nAll pass: %s. Took %d seconds.%n",
                  report.passes() ? "yes" : "no", (System.currentTimeMillis() - started) / 1000),
          StandardCharsets.UTF_8);
    } catch (final IOException e) {
      throw new IllegalStateException("Could not write the balance report", e);
    }
  }
}
