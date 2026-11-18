package com.grahambartley.nightborn.client.sun;

import com.grahambartley.nightborn.sun.ParasolItems;
import com.grahambartley.nightborn.sun.Parasols;
import net.minecraft.client.item.ModelPredicateProviderRegistry;

public final class ParasolModel {

  private ParasolModel() {}

  public static void register() {
    ModelPredicateProviderRegistry.register(
        ParasolItems.PARASOL,
        Parasols.OPEN_PREDICATE,
        (stack, world, entity, seed) -> Parasols.openness(stack, entity));
  }
}
