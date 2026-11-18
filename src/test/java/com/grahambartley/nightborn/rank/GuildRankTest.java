package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildRankTest {

  static final Path RANKS = Path.of("src/main/resources/data/nightborn/nightborn/guild_rank");

  static Map<Identifier, GuildRank> shipped() throws IOException {
    final Map<Identifier, GuildRank> ranks = new HashMap<>();
    try (Stream<Path> files = Files.list(RANKS)) {
      for (final Path file : files.toList()) {
        ranks.put(
            Identifier.of("nightborn", file.getFileName().toString().replace(".json", "")),
            GuildRank.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
                .getOrThrow());
      }
    }
    return ranks;
  }

  @Test
  @DisplayName("the shipped ladder runs Initiate, Hunter, Veteran, Master")
  void shippedLadder() throws IOException {
    assertEquals(
        Stream.of("initiate", "hunter", "veteran", "master")
            .map(path -> Identifier.of("nightborn", path))
            .toList(),
        RankLadder.of(shipped()).rungs().stream().map(RankLadder.Rung::id).toList());
  }

  @ParameterizedTest(name = "{0} -> {1} {2}")
  @CsvSource({
    "initiate, '', 0",
    "hunter, STAKE, 1",
    "veteran, CLEAR_HOUSEHOLD, 0",
    "master, END_ELDEST, 0"
  })
  @DisplayName("each shipped rank has the deed the design names, and a mark of its own shape")
  void shippedDeeds(final String path, final String act, final int minimumStage)
      throws IOException {
    final Map<Identifier, GuildRank> ranks = shipped();
    final GuildRank rank = ranks.get(Identifier.of("nightborn", path));
    assertEquals(act.isEmpty(), rank.deed().isEmpty());
    rank.deed()
        .ifPresent(
            deed -> {
              assertEquals(RankDeed.Act.valueOf(act), deed.act());
              assertEquals(minimumStage, deed.minimumStage());
            });
    assertEquals(
        "guild_rank.nightborn." + path,
        ((TranslatableTextContent) rank.name().getContent()).getKey());
    assertTrue(
        ranks.values().stream()
                .filter(other -> other.mark().glyph().equals(rank.mark().glyph()))
                .count()
            == 1);
  }
}
