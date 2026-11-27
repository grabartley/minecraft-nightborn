package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.structure.EntrancePlan;
import com.grahambartley.nightborn.structure.NearestStructures.Found;
import com.grahambartley.nightborn.structure.Threshold;
import com.grahambartley.nightborn.structure.VampireStructure;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.structure.Structure;

public final class RiteGround {

  public static final TagKey<Block> HAZARDS =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, "rite_hazards"));

  private RiteGround() {}

  static Optional<Threshold> door(
      final ServerWorld world, final Found house, final GuildRite.Reach reach) {
    final Structure structure =
        world.getRegistryManager().get(RegistryKeys.STRUCTURE).get(house.structure());
    if (!(structure instanceof VampireStructure built)) {
      return Optional.empty();
    }
    return RiteTicket.span(house.pos(), reach.searchRadius()).stream()
        .flatMap(
            chunk ->
                world.getStructureAccessor().getStructureStarts(chunk, structure::equals).stream())
        .map(
            start ->
                EntrancePlan.threshold(
                    built,
                    start.getChildren(),
                    world.getStructureTemplateManager(),
                    Random.create(world.getSeed())))
        .flatMap(Optional::stream)
        .findFirst();
  }

  static Optional<BlockPos> landing(
      final ServerWorld world, final Threshold door, final GuildRite.Reach reach) {
    return RiteLanding.choose(
        door, reach.searchRadius(), reach.searchRise(), spot -> safe(world, spot, reach));
  }

  static boolean safe(final ServerWorld world, final BlockPos spot, final GuildRite.Reach reach) {
    return RiteLanding.stands(
            world.getBlockState(spot.down()),
            world.getBlockState(spot),
            world.getBlockState(spot.up()),
            state -> state.isIn(HAZARDS))
        && world
            .getEntitiesByClass(
                LivingEntity.class,
                new Box(spot).expand(reach.householdReach()),
                near -> !(near instanceof PlayerEntity) && Vampires.isVampire(near))
            .isEmpty();
  }
}
