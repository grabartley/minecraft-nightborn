package com.grahambartley.nightborn.guild.rite;

public final class RiteAccess {

  static final int TICKS_PER_SECOND = 20;
  static final int SECONDS_PER_MINUTE = 60;

  private RiteAccess() {}

  public static boolean offered(
      final boolean vampire, final boolean hunter, final boolean needsOath) {
    return !vampire && (hunter || !needsOath);
  }

  public enum Refusal {
    NONE,
    RANK,
    PRICE,
    WAIT
  }

  public record Standing(boolean sworn, int rank, int emeralds, long waitSeconds) {}

  public record Terms(int minRank, int price) {}

  public static Refusal refusal(final Standing standing, final Terms terms) {
    if (standing.sworn() && standing.rank() < terms.minRank()) {
      return Refusal.RANK;
    }
    if (standing.waitSeconds() > 0) {
      return Refusal.WAIT;
    }
    return standing.emeralds() < terms.price() ? Refusal.PRICE : Refusal.NONE;
  }

  public static long waitSeconds(final long now, final long readyAt) {
    return Math.max(0L, Math.ceilDiv(readyAt - now, TICKS_PER_SECOND));
  }

  public static String clock(final long seconds) {
    return String.format("%d:%02d", seconds / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE);
  }

  public static long readyAt(final long now, final int cooldownSeconds) {
    return now + (long) cooldownSeconds * TICKS_PER_SECOND;
  }
}
