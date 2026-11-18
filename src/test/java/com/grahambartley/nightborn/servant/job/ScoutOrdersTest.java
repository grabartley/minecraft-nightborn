package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.structure.NearestStructures.Found;
import com.grahambartley.nightborn.thrall.ThrallEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScoutOrdersTest {

  @ParameterizedTest(name = "({0}, {1}) -> {2} blocks {3}")
  @CsvSource({"0, -240, 240, NORTH", "300, 400, 500, SOUTH_EAST", "-30, 40, 50, SOUTH_WEST"})
  @DisplayName("each place on the wheel shows its name, how far it lies and which way")
  void label(final int x, final int z, final int blocks, final Bearing bearing) {
    final Found place = new Found(Identifier.of("nightborn", "crypt"), new BlockPos(x, 0, z));
    final TranslatableTextContent label =
        (TranslatableTextContent) ScoutOrders.label(BlockPos.ORIGIN, place).getContent();
    assertEquals("nightborn.scout.place", label.getKey());
    assertEquals(ScoutTrip.placeName(place.structure()), label.getArgs()[0]);
    assertEquals(blocks, label.getArgs()[1]);
    assertEquals(Text.translatable(bearing.translationKey()), label.getArgs()[2]);
  }

  @Test
  @DisplayName("scouting answers to its own action and reads its places from a structure tag")
  void names() {
    assertEquals(
        "nightborn:scout", new ScoutOrders<ThrallEntity>(SkillTree.VAMPIRE).id().toString());
    assertEquals("nightborn:scout_targets", ScoutOrders.SCOUT_TARGETS.id().toString());
  }

  @ParameterizedTest(name = "disconnected {0}, gone {1}, screen {2}, in reach {3} -> {4}")
  @CsvSource({
    "false, false, false, true, true",
    "true, false, false, true, false",
    "false, true, false, true, false",
    "false, false, true, true, false",
    "false, false, false, false, false"
  })
  @DisplayName("the list of places opens only if the player is still there to choose")
  void stillAsking(
      final boolean disconnected,
      final boolean gone,
      final boolean screen,
      final boolean reach,
      final boolean opens) {
    assertEquals(opens, ScoutOrders.stillAsking(disconnected, gone, screen, reach));
  }
}
