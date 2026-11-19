package com.grahambartley.nightborn.guild.raiding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RaidingPartiesTest {

  private static final UUID QUARRY = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID LEADER = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID MEMBER = UUID.fromString("00000000-0000-0000-0000-000000000003");

  private static RaidingParty raid(final boolean passing) {
    return new RaidingParty(
        QUARRY,
        List.of(MEMBER),
        1,
        37000L,
        Optional.empty(),
        passing ? Optional.of(LEADER) : Optional.empty());
  }

  private static RaidingParties reloaded(final RaidingParties parties) {
    return RaidingParties.fromNbt(parties.writeNbt(new NbtCompound(), null), null);
  }

  @ParameterizedTest(name = "passing {0}")
  @CsvSource({"true", "false"})
  void keepsARaidAndItsLeaderAcrossASave(final boolean passing) {
    final RaidingParties parties = new RaidingParties();
    parties.start(raid(passing));
    final RaidingParties loaded = reloaded(parties);
    assertEquals(Optional.of(raid(passing)), loaded.against(QUARRY));
    assertEquals(passing, loaded.ledBy(LEADER).isPresent());
  }

  @ParameterizedTest(name = "ended on nights {0} and {1}: keeps {2}")
  @CsvSource({"4, 2, 4", "2, 4, 4", "3, 3, 3"})
  void keepsTheLatestEndForAQuarryUntilItIsTaken(
      final long first, final long second, final long kept) {
    final RaidingParties parties = new RaidingParties();
    parties.endedFor(QUARRY, first);
    parties.endedFor(QUARRY, second);
    final RaidingParties loaded = reloaded(parties);
    assertTrue(loaded.hasEnded(QUARRY));
    assertEquals(Optional.of(kept), loaded.takeEnded(QUARRY));
    assertFalse(loaded.hasEnded(QUARRY));
    assertEquals(Optional.empty(), loaded.takeEnded(QUARRY));
  }

  @ParameterizedTest(name = "started {0}")
  @CsvSource({"true", "false"})
  void updatesOnlyARaidThatIsUnderway(final boolean started) {
    final RaidingParties parties = new RaidingParties();
    if (started) {
      parties.start(raid(false));
    }
    parties.put(raid(false).without(MEMBER));
    assertEquals(
        started ? Optional.of(raid(false).without(MEMBER)) : Optional.empty(),
        parties.against(QUARRY));
    assertEquals(started, parties.remove(QUARRY).isPresent());
    assertEquals(Optional.empty(), parties.against(QUARRY));
  }

  @ParameterizedTest(name = "ended {0}")
  @CsvSource({"true", "false"})
  void forgetsAnEndWhenTheQuarryIsMadeHuman(final boolean ended) {
    final RaidingParties parties = new RaidingParties();
    if (ended) {
      parties.endedFor(QUARRY, 5L);
    }
    parties.forget(QUARRY);
    assertFalse(parties.hasEnded(QUARRY));
  }
}
