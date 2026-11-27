package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PitchedCampsTest {

  private static final HunterParty.Camp CAMP =
      new HunterParty.Camp(
          new BlockPos(5, 64, 5), List.of(new BlockPos(6, 64, 5)), List.of(new BlockPos(6, 64, 6)));
  private static final CampStay STAY = new CampStay(2, 3L, 84_000L);

  @ParameterizedTest(name = "{0} camps pitched, the first struck {1}, survive a save and load")
  @CsvSource({"1, false", "1, true", "3, false", "3, true"})
  void remembersEachLeadersCampUntilItIsStruck(final int count, final boolean strikeFirst) {
    final PitchedCamps camps = new PitchedCamps();
    final List<UUID> leaders = Stream.generate(UUID::randomUUID).limit(count).toList();
    leaders.forEach(leader -> camps.pitched(leader, CAMP, STAY));
    if (strikeFirst) {
      camps.struck(leaders.get(0));
    }
    final Map<UUID, PitchedCamps.Pitched> loaded =
        PitchedCamps.fromNbt(camps.writeNbt(new NbtCompound(), null), null).all();
    assertEquals(strikeFirst ? count - 1 : count, loaded.size());
    leaders.stream()
        .skip(strikeFirst ? 1 : 0)
        .forEach(leader -> assertEquals(new PitchedCamps.Pitched(CAMP, STAY), loaded.get(leader)));
  }

  @ParameterizedTest(name = "a leader saved as \"{0}\" reads back: {1}")
  @CsvSource({"9b3c2f1e-0d4a-4c8e-9f21-6a7b8c9d0e1f, true", "not-a-leader, false", "'', false"})
  void skipsACampWhoseLeaderIsDamaged(final String key, final boolean read) {
    assertEquals(read, PitchedCamps.leader(key).isPresent());
  }

  @ParameterizedTest(name = "a guard camp held {0}, its stay released {1}, is saved with that stay")
  @CsvSource({"true, true", "true, false", "false, true"})
  void keepsALeadersLatestStayForACampItHolds(final boolean held, final boolean released) {
    final UUID leader = UUID.randomUUID();
    final CampStay guard = CampStay.guarding(new BlockPos(40, 64, -12), 3L, 84_000L);
    final CampStay latest = released ? guard.relieve(90_000L) : guard;
    final PitchedCamps camps = new PitchedCamps();
    if (held) {
      camps.pitched(leader, CAMP, guard);
    }
    camps.kept(leader, latest);
    final Map<UUID, PitchedCamps.Pitched> loaded =
        PitchedCamps.fromNbt(camps.writeNbt(new NbtCompound(), null), null).all();
    assertEquals(
        held ? Optional.of(new PitchedCamps.Pitched(CAMP, latest)) : Optional.empty(),
        Optional.ofNullable(loaded.get(leader)));
  }
}
