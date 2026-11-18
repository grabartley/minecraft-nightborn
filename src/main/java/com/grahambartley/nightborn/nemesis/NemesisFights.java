package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.nemesis.NemesisLines.Moment;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class NemesisFights {

  record Key(UUID mob, UUID player) {}

  record Fight(MobEntity mob, UUID player, boolean quarryVampire, boolean struck, long lastBlow) {

    Fight blow(final boolean quarryVampire, final boolean struckNow, final long now) {
      return new Fight(mob, player, quarryVampire, struck || struckNow, now);
    }
  }

  private static final Map<Key, Fight> FIGHTS = new HashMap<>();
  private static final Set<Key> TOLD_DOWNED = new HashSet<>();

  private NemesisFights() {}

  static void afterDamage(final LivingEntity victim, final DamageSource source) {
    if (victim instanceof MobEntity mob
        && source.getAttacker() instanceof ServerPlayerEntity player
        && NemesisKinds.of(mob).isPresent()) {
      note(mob, player, true);
    } else if (victim instanceof ServerPlayerEntity player
        && source.getAttacker() instanceof MobEntity mob
        && NemesisKinds.of(mob).isPresent()) {
      note(mob, player, false);
      if (Nemeses.isQuarry(mob, player)
          && Downed.helpless(player)
          && TOLD_DOWNED.add(new Key(mob.getUuid(), player.getUuid()))) {
        NemesisTaunts.say(mob, player, Moment.DOWNED);
      }
    }
  }

  static void tick(final MinecraftServer server) {
    TOLD_DOWNED.removeIf(
        told -> {
          final ServerPlayerEntity player = server.getPlayerManager().getPlayer(told.player());
          return player == null || !Downed.helpless(player);
        });
    final long now = server.getOverworld().getTime();
    final Iterator<Fight> fights = FIGHTS.values().iterator();
    while (fights.hasNext()) {
      final Fight fight = fights.next();
      final MobEntity mob = fight.mob();
      if (mob.isRemoved() || !mob.isAlive()) {
        fights.remove();
        continue;
      }
      final int endTicks =
          NemesisKinds.of(mob).map(found -> found.kind().fightEndTicks()).orElse(0);
      if (NemesisRules.fightOver(now, fight.lastBlow(), endTicks) && Downed.state(mob) == null) {
        fights.remove();
        survived(fight);
      }
    }
  }

  static void settled(final UUID mob, final UUID player) {
    FIGHTS.remove(new Key(mob, player));
  }

  static void clear() {
    FIGHTS.clear();
    TOLD_DOWNED.clear();
  }

  private static void note(
      final MobEntity mob, final ServerPlayerEntity player, final boolean struck) {
    final long now = player.server.getOverworld().getTime();
    final boolean vampire = Vampires.isVampire(player);
    FIGHTS.merge(
        new Key(mob.getUuid(), player.getUuid()),
        new Fight(mob, player.getUuid(), vampire, struck, now),
        (held, fresh) -> held.blow(vampire, struck, now));
  }

  private static void survived(final Fight fight) {
    final MobEntity mob = fight.mob();
    if (!(mob.getWorld() instanceof ServerWorld world)) {
      return;
    }
    NemesisAttachment.get(mob)
        .ifPresentOrElse(
            mark -> {
              if (mark.quarry().equals(fight.player())) {
                NemesisGrowth.grow(world, mob, mark);
              }
            },
            () -> {
              if (NemesisRules.outlasted(
                  mob instanceof VampireEntity,
                  npcHousehold(mob),
                  fight.quarryVampire(),
                  fight.struck())) {
                NemesisMaking.make(mob, fight.player());
              }
            });
  }

  private static boolean npcHousehold(final MobEntity mob) {
    return mob instanceof VampireEntity vampire
        && vampire.home().isPresent()
        && vampire.householdTie().lord().isEmpty();
  }
}
