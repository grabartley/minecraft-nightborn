package com.grahambartley.nightborn.guild.party;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public final class CampClaims extends PersistentState {

  static final String ID = "nightborn_camp_blocks";
  private static final PersistentState.Type<CampClaims> TYPE =
      new PersistentState.Type<>(CampClaims::new, CampClaims::fromNbt, null);

  private final Map<Long, String> claims = new HashMap<>();

  public static CampClaims of(final ServerWorld world) {
    return world.getPersistentStateManager().getOrCreate(TYPE, ID);
  }

  static String id(final BlockState state) {
    return Registries.BLOCK.getId(state.getBlock()).toString();
  }

  public void claim(final BlockPos pos, final BlockState state) {
    claims.put(pos.asLong(), id(state));
    markDirty();
  }

  public boolean holds(final BlockPos pos, final BlockState state) {
    return id(state).equals(claims.get(pos.asLong()));
  }

  public boolean release(final BlockPos pos, final BlockState state) {
    final String claimed = claims.remove(pos.asLong());
    if (claimed == null) {
      return false;
    }
    markDirty();
    return claimed.equals(id(state));
  }

  public int size() {
    return claims.size();
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    claims.forEach((pos, block) -> nbt.putString(Long.toString(pos), block));
    return nbt;
  }

  static CampClaims fromNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final CampClaims claims = new CampClaims();
    for (final String key : nbt.getKeys()) {
      claims.claims.put(Long.parseLong(key), nbt.getString(key));
    }
    return claims;
  }
}
