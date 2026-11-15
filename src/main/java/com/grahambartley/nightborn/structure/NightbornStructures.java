package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;
import net.minecraft.world.gen.structure.StructureType;

public final class NightbornStructures {

  private static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "vampire_structure");

  public static final StructureType<VampireStructure> VAMPIRE_STRUCTURE =
      Registry.register(Registries.STRUCTURE_TYPE, ID, () -> VampireStructure.CODEC);
  public static final StructurePlacementType<VampireStructurePlacement>
      VAMPIRE_STRUCTURE_PLACEMENT =
          Registry.register(
              Registries.STRUCTURE_PLACEMENT, ID, () -> VampireStructurePlacement.CODEC);

  private NightbornStructures() {}

  public static void initialize() {
    StructureFogTracker.register();
  }
}
