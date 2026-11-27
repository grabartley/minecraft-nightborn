package com.grahambartley.nightborn.guild;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class ExplorationMaps {

  private ExplorationMaps() {}

  public static ItemStack around(
      final ServerWorld world, final BlockPos centre, final byte scale, final String nameKey) {
    final ItemStack map =
        FilledMapItem.createMap(world, centre.getX(), centre.getZ(), scale, true, true);
    FilledMapItem.fillExplorationMap(world, map);
    map.set(DataComponentTypes.ITEM_NAME, Text.translatable(nameKey));
    return map;
  }

  public static ItemStack marking(
      final ServerWorld world, final BlockPos target, final byte scale, final String nameKey) {
    final ItemStack map = around(world, target, scale, nameKey);
    MapState.addDecorationsNbt(map, target, "+", MapDecorationTypes.RED_X);
    return map;
  }
}
