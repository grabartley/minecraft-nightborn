package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.servant.Stance;
import com.grahambartley.nightborn.servant.Stanced;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class MemberStance implements Stanced {

  private final VampireEntity member;

  public MemberStance(final VampireEntity member) {
    this.member = member;
  }

  @Override
  public Optional<Stance> stance() {
    return member.householdTie().stance();
  }

  @Override
  public BlockPos post() {
    return member.householdTie().post().orElseGet(member::getBlockPos);
  }

  @Override
  @Nullable
  public LivingEntity leaderHere() {
    return member
        .householdTie()
        .lord()
        .map(lord -> member.getWorld().getPlayerByUuid(lord))
        .filter(PlayerEntity::isAlive)
        .orElse(null);
  }

  @Override
  public boolean guardsAgainst(final LivingEntity intruder) {
    return HouseRules.intruder(
        intruder instanceof PlayerEntity,
        Vampires.isVampire(intruder),
        intruder instanceof PlayerEntity player && (player.isCreative() || player.isSpectator()),
        intruder instanceof HunterEntity);
  }
}
