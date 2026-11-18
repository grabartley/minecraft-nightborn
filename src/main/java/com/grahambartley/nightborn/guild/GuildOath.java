package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.moment.Moment;
import com.grahambartley.nightborn.moment.MomentAttachment;
import com.grahambartley.nightborn.moment.MomentState;
import com.grahambartley.nightborn.moment.Moments;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

public final class GuildOath {

  static final String SWORN = "nightborn.oath.sworn";
  static final float LOOK_TURN = 30.0F;

  private GuildOath() {}

  public static boolean swear(
      final ServerPlayerEntity player, final Optional<GuildMasterEntity> master) {
    if (!GuildMembership.swear(player)) {
      return false;
    }
    MilestoneCriterion.reach(player, Milestone.SWORN_IN);
    Moments.begin(player, Moment.OATH);
    player.sendMessage(Text.translatable(SWORN), false);
    final MomentState state = MomentAttachment.get(player);
    final int ticks = state == null ? 0 : state.cameraTicks();
    master.ifPresent(giver -> giver.swearIn(player, ticks));
    salute(player);
    return true;
  }

  private static void salute(final ServerPlayerEntity player) {
    final int range = ServerConfigService.current().factions().ceremonyRange();
    for (final HunterEntity hunter :
        player
            .getServerWorld()
            .getEntitiesByClass(
                HunterEntity.class,
                Box.of(player.getPos(), range * 2.0, range * 2.0, range * 2.0),
                hunter -> hunter.isAlive() && hunter.squaredDistanceTo(player) <= range * range)) {
      hunter.getLookControl().lookAt(player, LOOK_TURN, LOOK_TURN);
      hunter.speak(HunterVoice.CHEER);
    }
  }
}
