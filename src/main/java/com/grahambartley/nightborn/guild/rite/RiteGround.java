package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.Nightborn;
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

public final class RiteGround {

  public static final TagKey<Block> HAZARDS =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, "rite_hazards"));

  private RiteGround() {}

  static Optional<BlockPos> landing(
      final ServerWorld world, final BlockPos entrance, final GuildRite.Reach reach) {
    return RiteLanding.choose(
        entrance, reach.searchRadius(), reach.searchRise(), spot -> safe(world, spot, reach));
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
