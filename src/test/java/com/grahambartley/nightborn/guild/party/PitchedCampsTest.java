package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
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
}
