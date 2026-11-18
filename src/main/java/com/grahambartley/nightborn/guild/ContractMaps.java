package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.raid.MapSpread;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class ContractMaps {

  private ContractMaps() {}

  public static void give(
      final ServerWorld world, final Entity master, final ServerPlayerEntity hunter) {
    final Text teller = master.getDisplayName();
    GuildRumour.nearestHouse(
        world,
        master.getBlockPos(),
        hunter,
        nearest ->
            nearest.ifPresentOrElse(
                house -> hand(hunter, map(world, house.pos())),
                () ->
                    hunter.sendMessage(
                        Text.translatable("nightborn.guild.rumour.none", teller), false)));
  }

  public static ItemStack map(final ServerWorld world, final BlockPos house) {
    final ItemStack map =
        FilledMapItem.createMap(
            world,
            house.getX(),
            house.getZ(),
            MapSpread.scale(ServerConfigService.current().raids().contractMapSpread()),
            true,
            true);
    FilledMapItem.fillExplorationMap(world, map);
    map.set(DataComponentTypes.ITEM_NAME, Text.translatable("nightborn.contract.map"));
    return map;
  }

  private static void hand(final ServerPlayerEntity hunter, final ItemStack map) {
    if (!hunter.giveItemStack(map)) {
      hunter.dropItem(map, false);
    }
  }
}
