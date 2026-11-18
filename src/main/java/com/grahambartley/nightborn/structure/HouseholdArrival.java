package com.grahambartley.nightborn.structure;

import java.util.Map;
import net.minecraft.entity.EntityData;
import net.minecraft.util.Identifier;

public record HouseholdArrival(Map<Identifier, Integer> stages, boolean head)
    implements EntityData {

  public HouseholdArrival {
    stages = Map.copyOf(stages);
  }
}
