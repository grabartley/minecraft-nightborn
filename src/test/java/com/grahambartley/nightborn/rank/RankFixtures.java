package com.grahambartley.nightborn.rank;

import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

final class RankFixtures {

  static final RankNeeds NEEDS = new RankNeeds(List.of(5, 20, 45));

  static final RankLadder LADDER =
      new RankLadder(
          List.of(
              rung("initiate", 0, Optional.empty(), RankGifts.NONE),
              rung("hunter", 1, Optional.of(deed(RankDeed.Act.STAKE, 1)), new RankGifts(4.0, 0.5F)),
              rung(
                  "veteran",
                  2,
                  Optional.of(deed(RankDeed.Act.CLEAR_HOUSEHOLD, 0)),
                  new RankGifts(8.0, 1.0F)),
              rung(
                  "master",
                  3,
                  Optional.of(deed(RankDeed.Act.END_ELDEST, 0)),
                  new RankGifts(12.0, 1.5F))));

  private RankFixtures() {}

  static RankLadder.Rung rung(final String path, final int order, final Optional<RankDeed> deed) {
    return rung(path, order, deed, RankGifts.NONE);
  }

  static RankLadder.Rung rung(
      final String path, final int order, final Optional<RankDeed> deed, final RankGifts gifts) {
    return new RankLadder.Rung(
        Identifier.of("nightborn", path),
        new GuildRank(
            order,
            Text.literal(path),
            new RankMark("+", TextColor.fromFormatting(Formatting.BLUE)),
            deed,
            gifts));
  }

  static RankDeed deed(final RankDeed.Act act, final int minimumStage) {
    return new RankDeed(act, minimumStage, Text.literal(act.asString()));
  }
}
