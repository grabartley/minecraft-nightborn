package com.grahambartley.nightborn.tools.structures.vampire.lair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Coffins;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LairShrineTest {

  private static Template shrineFacing(final String front) {
    final Template t = new Template("vampire/test", 25, 12, 25);
    LairCave.carve(t);
    final var step = Facing.STEP.get(front);
    LairShrine.cut(t, 12 - step.x() * 11, 12 - step.z() * 11, front, LairShrine.DEEPSLATE_COFFIN);
    return t;
  }

  @ParameterizedTest(name = "a shrine facing {0} stands its coffin in an open arch")
  @ValueSource(strings = {"north", "south", "east", "west"})
  void shrinesHoldAStandingCoffinWithRoomToOpen(final String front) {
    final Template t = shrineFacing(front);
    final List<Coffins.Coffin> coffins = Coffins.in(t);
    assertEquals(1, coffins.size());
    assertTrue(coffins.get(0).standing());
    assertEquals(Facing.OPPOSITE.get(front), coffins.get(0).facing());
    Coffins.assertSound(t, List.of(LairShrine.DEEPSLATE_COFFIN));
    final var step = Facing.STEP.get(front);
    final var foot = coffins.get(0).foot();
    assertEquals(BlockState.AIR, t.get(foot.x() + step.x(), 1, foot.z() + step.z()));
  }

  @ParameterizedTest(name = "a shrine facing {0} leaves no dripstone buried in its rock")
  @ValueSource(strings = {"north", "south", "east", "west"})
  void shrinesLeaveNoBuriedDripstone(final String front) {
    Dripstone.assertHangingWhole(shrineFacing(front));
  }
}
