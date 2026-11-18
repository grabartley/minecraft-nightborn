package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class FeatLinesTest {

  static final Identifier A = Identifier.of("nightborn", "ash_feat");
  static final Identifier B = Identifier.of("nightborn", "bone_feat");

  static Feat feat(final String title) {
    return new Feat(
        Text.literal(title + " feat"),
        Text.literal("d"),
        Text.literal(title),
        Side.ANYONE,
        new DeedGoal(
            DeedMatch.of(Identifier.of("nightborn", "fed")),
            1,
            DeedGoal.Measure.TOTAL,
            Optional.empty(),
            DeedGoal.Within.ANY_TIME,
            Optional.empty()),
        Optional.empty(),
        Identifier.of("nightborn", "ash_urn"),
        Optional.empty());
  }

  @ParameterizedTest(name = "wearing {0}: clicking each picks {1}")
  @CsvSource({
    "'', nightborn:ash_feat;nightborn:bone_feat",
    "nightborn:ash_feat, ;nightborn:bone_feat"
  })
  void listsEarnedFeatsInOrderAndClickingTheWornOneTakesItOff(
      final String worn, final String choices) {
    FeatLog log = FeatLog.EMPTY.earning(B, false).earning(A, true);
    if (!worn.isEmpty()) {
      log = log.wearing(Optional.of(Identifier.of(worn)));
    }
    final List<FeatLines.Line> lines =
        FeatLines.of(log, Map.of(A, feat("the Ash"), B, feat("the Bone")));
    assertEquals(List.of(A, B), lines.stream().map(FeatLines.Line::feat).toList());
    final String[] expected = choices.split(";", -1);
    for (int i = 0; i < lines.size(); i++) {
      assertEquals(
          expected[i].isEmpty() ? Optional.empty() : Optional.of(Identifier.of(expected[i])),
          lines.get(i).choiceOnClick());
    }
  }
}
