package com.grahambartley.nightborn.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.faction.Oath;
import com.grahambartley.nightborn.rank.GuildRank;
import com.grahambartley.nightborn.rank.RankDeed;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.rank.RankMark;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankCommandTest {

  static RankLadder.Rung rung(final String path, final boolean deed) {
    return new RankLadder.Rung(
        Identifier.of("nightborn", path),
        new GuildRank(
            0,
            Text.literal(path),
            new RankMark("+", TextColor.fromFormatting(Formatting.BLUE)),
            deed
                ? Optional.of(new RankDeed(RankDeed.Act.STAKE, 0, Text.literal(path)))
                : Optional.empty()));
  }

  @ParameterizedTest(name = "deeds {0} -> {1}")
  @CsvSource({"0, 0/3", "2, 1/3", "6, 2/3", "14, 3/3", "1, 0/3"})
  @DisplayName("an operator reads how many of the ladder's deeds a hunter has done")
  void deeds(final int deeds, final String shown) {
    final RankLadder ladder =
        new RankLadder(
            List.of(
                rung("initiate", false),
                rung("hunter", true),
                rung("veteran", true),
                rung("master", true)));
    assertEquals(shown, RankCommand.deeds(ladder, new Oath(true, true, 0, deeds)));
  }
}
