package com.grahambartley.nightborn.balance;

import com.grahambartley.nightborn.balance.Roster.Kind;
import java.util.List;
import java.util.Set;

public record Matchup(Roster vampires, Roster hunters, int size) {

  public static final List<Matchup> ALL =
      List.of(
          pair("nightborn:fledgling", "nightborn:initiate", 1),
          pair("nightborn:fledgling", "nightborn:initiate", 3),
          pair("nightborn:fledgling", "nightborn:initiate", 5),
          pair("nightborn:adult", "nightborn:hunter", 1),
          pair("nightborn:adult", "nightborn:hunter", 3),
          pair("nightborn:adult", "nightborn:hunter", 5),
          pair("nightborn:elder", "nightborn:veteran", 1),
          pair("nightborn:elder", "nightborn:veteran", 3),
          pair("nightborn:elder", "nightborn:veteran", 5),
          pair("nightborn:ancient", "nightborn:master", 1),
          pair("nightborn:ancient", "nightborn:master", 3),
          pair("nightborn:ancient", "nightborn:master", 5),
          new Matchup(Roster.of(Kind.VAMPIRE_LORD), Roster.of(Kind.VETERAN_HUNTER), 1),
          new Matchup(Roster.of(Kind.VAMPIRE_LORD), Roster.of(Kind.GUILD_MASTER), 1),
          new Matchup(Roster.of(Kind.VAMPIRE), Roster.of(Kind.HUNTER), 5));

  private static Matchup pair(final String stage, final String rank, final int size) {
    return new Matchup(Roster.of(Kind.VAMPIRE, stage), Roster.of(Kind.HUNTER, rank), size);
  }

  public static List<Matchup> chosen(final String keys) {
    if (keys == null || keys.isBlank()) {
      return ALL;
    }
    final Set<String> wanted = Set.of(keys.split(","));
    return ALL.stream().filter(matchup -> wanted.contains(matchup.key())).toList();
  }

  public String key() {
    return strip(vampires.label()) + "-" + strip(hunters.label()) + "-" + size;
  }

  public int minimumSeconds() {
    return size >= 5 ? 40 : size >= 3 ? 30 : 20;
  }

  private static String strip(final String label) {
    return label.substring(label.indexOf(':') + 1);
  }
}
