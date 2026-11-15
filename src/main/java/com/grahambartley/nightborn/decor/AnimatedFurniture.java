package com.grahambartley.nightborn.decor;

import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;

public interface AnimatedFurniture {

  BooleanProperty toggleProperty();

  String artModel();

  default boolean glows(final BlockState state) {
    return false;
  }

  default Set<String> hiddenBones(final BlockState state) {
    return Set.of();
  }
}
