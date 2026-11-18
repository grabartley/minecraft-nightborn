package com.grahambartley.nightborn.house;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OfflineStageTest {

  private static NbtCompound saved(final int rank) {
    final NbtCompound age = new NbtCompound();
    age.putInt("rank", rank);
    final NbtCompound attachments = new NbtCompound();
    attachments.put("nightborn:age", age);
    final NbtCompound player = new NbtCompound();
    player.put("fabric:attachments", attachments);
    return player;
  }

  @ParameterizedTest(name = "rank {0}")
  @ValueSource(ints = {0, 1, 2, 3})
  @DisplayName("an offline player's stage is read from their saved data")
  void reads(final int rank) {
    assertEquals(Optional.of(rank), OfflineStage.ageRank(saved(rank)));
  }

  @Test
  @DisplayName("a player saved with no age has none yet")
  void unaged() {
    assertEquals(Optional.of(0), OfflineStage.ageRank(new NbtCompound()));
  }

  @Test
  @DisplayName("a damaged age is not guessed at")
  void damaged() {
    final NbtCompound player = new NbtCompound();
    final NbtCompound attachments = new NbtCompound();
    attachments.put("nightborn:age", NbtString.of("ancient"));
    player.put("fabric:attachments", attachments);
    assertEquals(Optional.empty(), OfflineStage.ageRank(player));
  }
}
