package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TrialLinesTest {

  static final Identifier ID = Identifier.of("nightborn", "clean_kill");
  static final Trial TRIAL =
      new Trial(
          Text.literal("Clean Kill"),
          Text.literal("Stake 3 vampires in one night"),
          Side.HUNTER,
          new DeedGoal(
              DeedMatch.of(Identifier.of("nightborn", "vampire_staked")),
              3,
              DeedGoal.Measure.TOTAL,
              Optional.empty(),
              DeedGoal.Within.ONE_NIGHT,
              Optional.empty()),
          Optional.empty(),
          List.of(),
          TrialSize.MEDIUM,
          Optional.empty());

  @ParameterizedTest(name = "taken {0}, done {1}, counted {2}: {3}")
  @CsvSource({
    "false, false, 0, nightborn.journal.trials.untaken",
    "true, false, 2, nightborn.journal.trials.progress",
    "true, true, 3, nightborn.journal.trials.done"
  })
  void showsWhereEachTrialStands(
      final boolean taken, final boolean done, final int counted, final String key) {
    final TrialOffer offer =
        new TrialOffer(
            Optional.of(Side.HUNTER),
            1L,
            List.of(
                new OfferedTrial(ID, taken, done, new Tally(counted, List.of(), 0L)),
                OfferedTrial.fresh(Identifier.of("nightborn", "gone"), true)));
    final List<TrialLines.Line> lines =
        TrialLines.of(offer, Map.of(ID, TRIAL), OptionalLong.of(0L));
    assertEquals(1, lines.size());
    assertEquals(key, ((TranslatableTextContent) lines.get(0).status().getContent()).getKey());
    assertEquals("Stake 3 vampires in one night", lines.get(0).description().getString());
  }
}
