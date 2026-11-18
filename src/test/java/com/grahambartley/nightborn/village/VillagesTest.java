package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class VillagesTest {

  private static final BlockPos BELL = new BlockPos(120, 68, -40);

  @ParameterizedTest(name = "suspicion {0} is kept: {1}")
  @CsvSource({"0, false", "1, true", "75, true"})
  @DisplayName("only a village with something to remember is stored")
  void storesOnlyWhatMatters(final int suspicion, final boolean kept) {
    final Villages villages = new Villages();
    villages.put(BELL, VillageRecord.fresh(3L).raised(suspicion, 3L, Optional.empty()));
    assertEquals(kept, villages.at(BELL).isPresent());
    assertEquals(kept ? 1 : 0, villages.all().size());
  }

  @ParameterizedTest(name = "suspicion {0} survives a save and load")
  @CsvSource({"10", "90"})
  void survivesASaveAndLoad(final int suspicion) {
    final Villages villages = new Villages();
    final VillageRecord record =
        VillageRecord.fresh(3L).raised(suspicion, 3L, Optional.of(UUID.randomUUID()));
    villages.put(BELL, record);
    final NbtCompound saved = villages.writeNbt(new NbtCompound(), null);
    assertEquals(Optional.of(record), Villages.fromNbt(saved, null).at(BELL));
  }

  @ParameterizedTest(name = "calming a village at {0} forgets it")
  @CsvSource({"10", "90"})
  void forgetsACalmVillage(final int suspicion) {
    final Villages villages = new Villages();
    final VillageRecord record = VillageRecord.fresh(3L).raised(suspicion, 3L, Optional.empty());
    villages.put(BELL, record);
    villages.put(BELL, record.charmed(4L, suspicion));
    assertEquals(Optional.empty(), villages.at(BELL));
  }
}
