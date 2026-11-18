package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.rank.GuildRank;
import com.grahambartley.nightborn.rank.RankDeed;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.rank.RankMark;
import com.grahambartley.nightborn.rank.RankOutlook;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildRankOrderTest {

  @ParameterizedTest(name = "next {0}, {1} points left, deed {2}, done {3}")
  @CsvSource({
    "true, 5, true, false, 'nightborn.rank.told, nightborn.rank.next, nightborn.rank.deed'",
    "true, 0, true, true,"
        + " 'nightborn.rank.told, nightborn.rank.next.earned, nightborn.rank.deed.done'",
    "true, 3, false, false, 'nightborn.rank.told, nightborn.rank.next'",
    "false, 0, false, false, 'nightborn.rank.told, nightborn.rank.top'"
  })
  @DisplayName("the Guild Master names the rank, then what the next needs or that none is higher")
  void told(
      final boolean hasNext,
      final int pointsLeft,
      final boolean hasDeed,
      final boolean deedDone,
      final String keys) {
    final Optional<RankDeed> deed =
        hasDeed
            ? Optional.of(new RankDeed(RankDeed.Act.STAKE, 1, Text.literal("stake")))
            : Optional.empty();
    final RankLadder.Rung next =
        new RankLadder.Rung(
            Identifier.of("nightborn", "hunter"),
            new GuildRank(
                1,
                Text.literal("Hunter"),
                new RankMark("+", TextColor.fromFormatting(Formatting.BLUE)),
                deed));
    final List<Text> lines =
        GuildRankOrder.told(
            Text.literal("Abel"),
            Optional.of(Text.literal("Initiate")),
            hasNext ? Optional.of(new RankOutlook(next, pointsLeft, deedDone)) : Optional.empty());
    assertEquals(
        List.of(keys.split(", ")),
        lines.stream()
            .map(line -> ((TranslatableTextContent) line.getContent()).getKey())
            .toList());
  }
}
