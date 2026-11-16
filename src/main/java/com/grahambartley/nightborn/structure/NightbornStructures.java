package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;
import net.minecraft.world.gen.structure.StructureType;

public final class NightbornStructures {

  public static final StructureType<VampireStructure> VAMPIRE_STRUCTURE =
      Registry.register(
          Registries.STRUCTURE_TYPE,
          Identifier.of(Nightborn.MOD_ID, "vampire_structure"),
          () -> VampireStructure.CODEC);
  public static final StructurePlacementType<RareSpreadPlacement> RARE_SPREAD =
      Registry.register(
          Registries.STRUCTURE_PLACEMENT,
          Identifier.of(Nightborn.MOD_ID, "rare_spread"),
          () -> RareSpreadPlacement.CODEC);

  private NightbornStructures() {}

  public static void initialize() {
    StructureFogTracker.register();
  }
}
