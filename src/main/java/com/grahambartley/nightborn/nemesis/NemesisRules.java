package com.grahambartley.nightborn.nemesis;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NemesisRules {

  public record Growth(boolean ages, boolean boosts, boolean retitles) {}

  public record Rung(String id, int rank, int weight) {}

  private NemesisRules() {}

  public static boolean mayBecome(
      final boolean named,
      final boolean boss,
      final boolean sealedHome,
      final boolean alreadyNemesis,
      final int held,
      final int limit) {
    return named && !boss && !sealedHome && !alreadyNemesis && held < limit;
  }

  public static boolean outlasted(
      final boolean mobIsVampire,
      final boolean mobInNpcHousehold,
      final boolean quarryIsVampire,
      final boolean quarryStruck) {
    if (!quarryIsVampire) {
      return false;
    }
    return mobIsVampire ? mobInNpcHousehold && quarryStruck : true;
  }

  public static boolean fightOver(final long now, final long lastBlow, final int endTicks) {
    return now - lastBlow >= endTicks;
  }

  public static long nextVisit(final long night, final int fewest, final int most, final int roll) {
    final int low = Math.max(1, fewest);
    final int high = Math.max(low, most);
    return night + low + Math.floorMod(roll, high - low + 1);
  }

  public static boolean visitDue(
      final long night,
      final long nextVisit,
      final boolean dark,
      final boolean overworld,
      final boolean openSky) {
    return night >= nextVisit && dark && overworld && openSky;
  }

  public static Growth growth(
      final NemesisKind kind,
      final int boostsTaken,
      final boolean canAge,
      final double epithetRoll) {
    return new Growth(
        kind.ages() && canAge,
        kind.boost() > 0.0F && boostsTaken < kind.maxBoosts(),
        epithetRoll < kind.epithetChance());
  }

  public static double boost(final NemesisKind kind, final int boosts) {
    return kind.boost() * Math.min(Math.max(0, boosts), kind.maxBoosts());
  }

  public static Optional<String> nextRung(final List<Rung> rungs, final String current) {
    final int rank =
        rungs.stream()
            .filter(rung -> rung.id().equals(current))
            .mapToInt(Rung::rank)
            .findFirst()
            .orElse(Integer.MAX_VALUE);
    return rungs.stream()
        .filter(rung -> rung.weight() > 0 && rung.rank() > rank)
        .min(Comparator.comparingInt(Rung::rank))
        .map(Rung::id);
  }

  public static int reward(final int kindExperience, final int times) {
    return Math.max(0, kindExperience) * Math.max(0, times);
  }

  public static int companions(final int most, final int roll) {
    return most <= 0 ? 0 : Math.floorMod(roll, most + 1);
  }

  public static Optional<UUID> ender(
      final Optional<UUID> attacker, final boolean sunKill, final Optional<UUID> downedBy) {
    return attacker.or(() -> sunKill ? downedBy : Optional.empty());
  }

  public static boolean endedByQuarry(final Optional<UUID> ender, final UUID quarry) {
    return ender.filter(quarry::equals).isPresent();
  }

  public static Optional<String> newEpithet(
      final List<String> pool, final String current, final int roll) {
    final List<String> fresh = pool.stream().filter(name -> !name.equals(current)).toList();
    return fresh.isEmpty()
        ? Optional.empty()
        : Optional.of(fresh.get(Math.floorMod(roll, fresh.size())));
  }
}
