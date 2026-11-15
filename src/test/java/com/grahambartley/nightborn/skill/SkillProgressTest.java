package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SkillProgressTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillTree.class)
  @DisplayName("each tree keeps its own points, so spending one never touches the other")
  void treesAreSeparate(final SkillTree tree) {
    final SkillProgress progress = SkillProgress.EMPTY.with(tree, TreeProgress.EMPTY.withEarned(4));
    assertEquals(4, progress.get(tree).earned());
    for (final SkillTree other : SkillTree.values()) {
      if (other != tree) {
        assertTrue(progress.get(other).isEmpty());
      }
    }
  }

  @Test
  @DisplayName("progress crosses the network unchanged")
  void packetRoundTrip() {
    final SkillProgress progress =
        new SkillProgress(
            new TreeProgress(
                3,
                1.5F,
                List.of(new BoughtSkill(Identifier.of("nightborn", "core/thick_blood"), 1)),
                List.of(Identifier.of("nightborn", "beast"))),
            TreeProgress.EMPTY.withEarned(2));
    final var buf = Unpooled.buffer();
    SkillProgress.PACKET_CODEC.encode(buf, progress);
    assertEquals(progress, SkillProgress.PACKET_CODEC.decode(buf));
  }
}
