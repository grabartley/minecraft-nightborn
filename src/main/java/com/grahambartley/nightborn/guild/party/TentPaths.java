package com.grahambartley.nightborn.guild.party;

import java.util.EnumSet;
import java.util.Set;
import net.fabricmc.fabric.api.registry.LandPathNodeTypesRegistry;
import net.minecraft.entity.ai.pathing.PathNodeType;
import org.jetbrains.annotations.Nullable;

public final class TentPaths {

  static final Set<TentPart> MOUTH = EnumSet.of(TentPart.ROOF_FRONT, TentPart.ROOF_MIDDLE);

  private TentPaths() {}

  public static void register() {
    LandPathNodeTypesRegistry.register(
        TentBlocks.HUNTER_TENT,
        (state, neighbor) ->
            type(state.get(HunterTentBlock.PART), state.get(HunterTentBlock.STRUCK), neighbor));
  }

  @Nullable
  public static PathNodeType type(
      final TentPart part, final boolean struck, final boolean neighbor) {
    return neighbor || struck || MOUTH.contains(part) ? null : PathNodeType.FENCE;
  }
}
