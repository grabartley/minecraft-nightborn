package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.structure.CaveMouth;
import com.grahambartley.nightborn.structure.Threshold;
import com.grahambartley.nightborn.structure.VampireStructure;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

public final class UndergroundLairGameTest implements FabricGameTest {

  private static final Identifier ENTRANCE = Identifier.of(Nightborn.MOD_ID, "lair_entrance");

  @GameTest(templateName = VampireStructureGameTest.ARENA, tickLimit = 20)
  public void theLairOpensOntoACave(final TestContext context) {
    final Structure lair =
        context
            .getWorld()
            .getRegistryManager()
            .get(RegistryKeys.STRUCTURE)
            .get(Identifier.of(Nightborn.MOD_ID, "underground_lair"));
    context.assertTrue(
        lair instanceof VampireStructure vampire
            && vampire.siting().cave().map(CaveMouth::entrance).equals(Optional.of(ENTRANCE))
            && vampire.footing().isEmpty(),
        "the lair looks for a cave to open its entrance onto, got " + lair);
    context.complete();
  }

  @GameTest(templateName = VampireStructureGameTest.ARENA, tickLimit = 20)
  public void everyLairPieceCarriesItsEntranceOnEveryTurn(final TestContext context) {
    for (final String piece : List.of("den", "grotto")) {
      final StructureTemplate template =
          context
              .getWorld()
              .getStructureTemplateManager()
              .getTemplateOrBlank(Identifier.of(Nightborn.MOD_ID, "vampire/lair/" + piece));
      for (final BlockRotation rotation : BlockRotation.values()) {
        final Optional<Threshold> threshold =
            Threshold.named(
                ENTRANCE,
                template.getInfosForBlock(
                    BlockPos.ORIGIN,
                    new StructurePlacementData().setRotation(rotation),
                    Blocks.JIGSAW,
                    true));
        context.assertTrue(
            threshold.isPresent() && threshold.get().out().getAxis().isHorizontal(),
            piece + " turned " + rotation + " has an entrance facing out, got " + threshold);
      }
    }
    context.complete();
  }
}
