package com.grahambartley.nightborn.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AdvancementCatchUpTest {

  private static final List<Identifier> LADDER =
      List.of(
          Identifier.of("nightborn", "fledgling"),
          Identifier.of("nightborn", "adult"),
          Identifier.of("nightborn", "elder"));

  @ParameterizedTest(name = "rank {0} -> {1} rungs")
  @CsvSource({"-1, 0", "0, 1", "1, 2", "2, 3", "9, 3"})
  void everyRungUpToTheCurrentOneIsReached(final int rank, final int reached) {
    assertEquals(LADDER.subList(0, reached), AdvancementCatchUp.reached(rank, LADDER));
  }

  @ParameterizedTest(name = "share {0}, household {1} -> {2} deeds")
  @CsvSource({"0, 0, 2", "50, 0, 3", "100, 6, 5"})
  void reportsWhatThePlayerAlreadyHas(final int share, final int household, final int count) {
    final List<Deed> deeds =
        AdvancementCatchUp.deeds(
            LADDER.subList(0, 1),
            List.of(Identifier.of("nightborn", "hunter")),
            List.of(
                new AdvancementCatchUp.LineShareDone(
                    SkillTree.VAMPIRE, Identifier.of("nightborn", "noble"), share)),
            household);
    assertEquals(count, deeds.size());
    assertEquals(DeedKinds.STAGE_REACHED, deeds.get(0).kind());
    assertEquals(Optional.of("nightborn:fledgling"), deeds.get(0).fact(DeedKinds.STAGE));
    assertEquals(Optional.of("nightborn:hunter"), deeds.get(1).fact(DeedKinds.RANK));
    assertEquals(
        household > 0, deeds.stream().anyMatch(d -> d.kind().equals(DeedKinds.HOUSE_FOUNDED)));
    deeds.stream()
        .filter(d -> d.kind().equals(DeedKinds.HOUSE_GUEST))
        .forEach(d -> assertEquals(household, d.number(DeedKinds.HOUSEHOLD).getAsInt()));
  }
}
