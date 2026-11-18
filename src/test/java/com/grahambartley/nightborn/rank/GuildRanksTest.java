package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.faction.Oath;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildRanksTest {

  @ParameterizedTest(name = "sworn {0}, rank {1}, vampire {2} -> {3}")
  @CsvSource({
    "false, 0, false, 0",
    "true, 0, false, 0",
    "true, 2, false, 2",
    "true, 3, false, 3",
    "true, 3, true, 0",
    "false, 2, false, 0"
  })
  @DisplayName("a hunter holds their rank, and a vampire holds none while theirs is set aside")
  void of(final boolean sworn, final int rank, final boolean vampire, final int held) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    FactionAttachment.set(player, vampire ? Faction.VAMPIRE : Faction.HUMAN);
    OathAttachment.set(player, new Oath(true, sworn, rank, 0));
    assertEquals(held, GuildRanks.of(player));
    assertEquals(rank, OathAttachment.get(player).rank());
  }
}
