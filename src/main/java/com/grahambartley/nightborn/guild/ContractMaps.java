package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.raid.MapSpread;
import com.grahambartley.nightborn.servant.job.ScoutChoices;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.NearestStructures;
import com.grahambartley.nightborn.structure.NearestStructures.Found;
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
    final BlockPos from = master.getBlockPos();
    final int range = ServerConfigService.current().structures().rumourRange();
    final Text teller = master.getDisplayName();
    NearestStructures.eachOf(world, GuildRumour.VAMPIRE_HOUSES, from, range)
        .thenAcceptAsync(
            found -> {
              if (hunter.isDisconnected()) {
                return;
              }
              ScoutChoices.nearest(from, found, range, 1).stream()
                  .findFirst()
                  .ifPresentOrElse(
                      house -> hand(world, hunter, house),
                      () ->
                          hunter.sendMessage(
                              Text.translatable("nightborn.guild.rumour.none", teller), false));
            },
            world.getServer());
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

  private static void hand(
      final ServerWorld world, final ServerPlayerEntity hunter, final Found house) {
    final ItemStack map = map(world, house.pos());
    if (!hunter.giveItemStack(map)) {
      hunter.dropItem(map, false);
    }
  }
}
