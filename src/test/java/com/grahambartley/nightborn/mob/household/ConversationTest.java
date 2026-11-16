package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.mob.ShippedClips;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConversationTest {

  @ParameterizedTest(name = "length roll {0}, quarrel roll {1}, tempers {2} and {3}: {4}")
  @CsvSource({
    "0, 99, 10, 10, OT TT OT",
    "3, 99, 10, 10, OT TT OT TT OT TT",
    "4, 99, 10, 10, OT TT OT",
    "0, 50, 70, 10, OT TA OA",
    "0, 50, 10, 70, OT TA OA",
    "3, 69, 70, 0, OT TT OT TA OA TA",
    "3, 70, 70, 0, OT TT OT TT OT TT"
  })
  @DisplayName("housemates take turns, and a hot temper on either side turns talk into an argument")
  void lines(
      final int lengthRoll,
      final int quarrelRoll,
      final int temper,
      final int otherTemper,
      final String expected) {
    final List<Conversation.Line> lines =
        Conversation.lines(lengthRoll, quarrelRoll, temper, otherTemper);
    assertEquals(
        expected,
        lines.stream()
            .map(line -> (line.opener() ? "O" : "T") + line.tone().name().charAt(0))
            .collect(Collectors.joining(" ")));
  }

  @ParameterizedTest(name = "{0} plays {1}")
  @CsvSource({"TALK, talk", "ARGUE, argue"})
  @DisplayName("each tone plays its own rig clip, shipped in the animation file")
  void clip(final Conversation.Tone tone, final String clip) {
    assertEquals(clip, tone.clip());
    assertTrue(ShippedClips.vampire().contains(clip));
  }
}
