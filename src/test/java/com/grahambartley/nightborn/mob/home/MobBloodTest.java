package com.grahambartley.nightborn.mob.home;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.blood.BloodValue;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MobBloodTest {

  @ParameterizedTest(name = "pending {0}, took {1} health at {2}%: gains {3}, keeps {4}")
  @CsvSource({
    "0, 4, 100, 4, 0",
    "0, 4, 50, 2, 0",
    "0, 3, 50, 1, 0.5",
    "0.5, 3, 50, 2, 0",
    "0, 2, 150, 3, 0",
    "0, 4, 0, 0, 0",
    "0.25, 0, 100, 0, 0.25",
    "0, -2, 100, 0, 0"
  })
  @DisplayName("a bite fills a vampire mob by the victim's blood quality and carries the fraction")
  void gain(
      final float pending,
      final float taken,
      final int quality,
      final int whole,
      final float remainder) {
    final BloodValue.Split split = MobBlood.gain(pending, taken, quality);
    assertEquals(whole, split.whole());
    assertEquals(remainder, split.remainder(), 1.0E-5F);
  }

  @ParameterizedTest(name = "took {0} health at {1}%")
  @CsvSource({"3, 50", "1.5, 100", "0.2, 150"})
  @DisplayName("the unspent fraction of a bite survives a save and load")
  void savesPending(final float taken, final int quality) {
    final MobBlood blood = new MobBlood();
    final NbtCompound nbt = new NbtCompound();
    nbt.putFloat(MobBlood.PENDING_KEY, MobBlood.gain(0.0F, taken, quality).remainder());
    blood.read(nbt);
    final NbtCompound saved = new NbtCompound();
    blood.write(saved);
    assertEquals(nbt.getFloat(MobBlood.PENDING_KEY), saved.getFloat(MobBlood.PENDING_KEY));
  }
}
