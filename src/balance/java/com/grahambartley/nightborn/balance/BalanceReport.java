package com.grahambartley.nightborn.balance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class BalanceReport {

  static final double FAIR_LOW = 0.45;
  static final double FAIR_HIGH = 0.55;
  static final double DRAW_LIMIT = 0.10;
  static final double MELEE_SHARE = 0.30;
  static final double TAG_LIMIT = 0.60;
  static final int TAG_SAMPLE = 10;

  public record Row(
      Matchup matchup,
      int rounds,
      int vampireWins,
      int hunterWins,
      double averageSeconds,
      Tally.Blows vampireBlows,
      Tally.Blows hunterBlows) {

    public int decided() {
      return vampireWins + hunterWins;
    }

    public int draws() {
      return rounds - decided();
    }

    public double vampireShare() {
      return decided() == 0 ? 0.0 : (double) vampireWins / decided();
    }

    public boolean fair() {
      return decided() > 0 && vampireShare() >= FAIR_LOW && vampireShare() <= FAIR_HIGH;
    }

    public boolean fewDraws() {
      return rounds > 0 && (double) draws() / rounds < DRAW_LIMIT;
    }

    public boolean meleeOnBothSides() {
      return melee(vampireBlows) && melee(hunterBlows);
    }

    public boolean longEnough() {
      return averageSeconds >= matchup.minimumSeconds();
    }

    public boolean passes() {
      return fair() && fewDraws() && meleeOnBothSides() && longEnough();
    }

    private static boolean melee(final Tally.Blows blows) {
      return blows.meleeHits() > 0 && blows.meleeShare() >= MELEE_SHARE;
    }
  }

  public record TagRow(String tag, int rounds, int wins) {

    public double share() {
      return rounds == 0 ? 0.0 : (double) wins / rounds;
    }

    public boolean measured() {
      return rounds >= TAG_SAMPLE;
    }

    public boolean passes() {
      return !measured() || share() <= TAG_LIMIT;
    }
  }

  private final List<Row> rows;
  private final List<TagRow> tags;

  public BalanceReport(final List<Matchup> matchups, final List<RoundResult> results) {
    rows = matchups.stream().map(matchup -> row(matchup, results)).toList();
    tags = tags(results);
  }

  public List<Row> rows() {
    return rows;
  }

  public List<TagRow> tags() {
    return tags;
  }

  public boolean passes() {
    return rows.stream().allMatch(Row::passes) && tags.stream().allMatch(TagRow::passes);
  }

  public String markdown() {
    final StringBuilder out = new StringBuilder();
    out.append(
        "| Matchup | Rounds | Vampire wins | Hunter wins | Draws | Vampire share | Avg seconds"
            + " | Vampire melee hits (share) | Hunter melee hits (share) | Other hits V/H | Pass |\n");
    out.append("|---|---|---|---|---|---|---|---|---|---|---|\n");
    for (final Row row : rows) {
      out.append(
          String.format(
              Locale.ROOT,
              "| %s | %d | %d | %d | %d | %.0f%% | %.1f | %d (%.0f%%) | %d (%.0f%%) | %d/%d | %s |\n",
              row.matchup().key(),
              row.rounds(),
              row.vampireWins(),
              row.hunterWins(),
              row.draws(),
              row.vampireShare() * 100.0,
              row.averageSeconds(),
              row.vampireBlows().meleeHits(),
              row.vampireBlows().meleeShare() * 100.0,
              row.hunterBlows().meleeHits(),
              row.hunterBlows().meleeShare() * 100.0,
              row.vampireBlows().otherHits(),
              row.hunterBlows().otherHits(),
              row.passes() ? "yes" : "no"));
    }
    out.append("\n| Kit, path, bloodline, temperament or power | Rounds | Wins | Share | Pass |\n");
    out.append("|---|---|---|---|---|\n");
    for (final TagRow tag : tags) {
      out.append(
          String.format(
              Locale.ROOT,
              "| %s | %d | %d | %.0f%% | %s |\n",
              tag.tag(),
              tag.rounds(),
              tag.wins(),
              tag.share() * 100.0,
              tag.measured() ? (tag.passes() ? "yes" : "no") : "too few"));
    }
    return out.toString();
  }

  private static Row row(final Matchup matchup, final List<RoundResult> results) {
    final List<RoundResult> mine =
        results.stream().filter(result -> result.matchup().equals(matchup.key())).toList();
    Tally.Blows vampires = Tally.Blows.NONE;
    Tally.Blows hunters = Tally.Blows.NONE;
    double seconds = 0.0;
    for (final RoundResult result : mine) {
      vampires = vampires.plus(result.blowsOf(Side.VAMPIRES));
      hunters = hunters.plus(result.blowsOf(Side.HUNTERS));
      seconds += result.seconds();
    }
    return new Row(
        matchup,
        mine.size(),
        wins(mine, Side.VAMPIRES),
        wins(mine, Side.HUNTERS),
        mine.isEmpty() ? 0.0 : seconds / mine.size(),
        vampires,
        hunters);
  }

  private static int wins(final List<RoundResult> results, final Side side) {
    return (int)
        results.stream().filter(result -> result.winner().equals(Optional.of(side))).count();
  }

  private static List<TagRow> tags(final List<RoundResult> results) {
    final Map<String, int[]> counts = new TreeMap<>();
    for (final RoundResult result : results) {
      if (result.winner().isEmpty()) {
        continue;
      }
      for (final Side side : Side.values()) {
        for (final String tag : result.tagsOf(side)) {
          final int[] count = counts.computeIfAbsent(tag, key -> new int[2]);
          count[0]++;
          if (result.winner().get() == side) {
            count[1]++;
          }
        }
      }
    }
    final List<TagRow> rows = new ArrayList<>();
    counts.forEach((tag, count) -> rows.add(new TagRow(tag, count[0], count[1])));
    return rows;
  }
}
