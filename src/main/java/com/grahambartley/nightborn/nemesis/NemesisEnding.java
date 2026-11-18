package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.nemesis.NemesisLines.Moment;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillEarning;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class NemesisEnding {

  static final String ENDED = "nightborn.nemesis.ended";

  private NemesisEnding() {}

  static void afterDeath(final LivingEntity entity, final DamageSource source) {
    if (entity instanceof ServerPlayerEntity player) {
      fellTo(player, source);
      return;
    }
    if (!(entity instanceof MobEntity mob) || !(mob.getWorld() instanceof ServerWorld world)) {
      return;
    }
    NemesisAttachment.get(mob).ifPresent(mark -> end(world, mob, mark, source));
  }

  private static void end(
      final ServerWorld world,
      final MobEntity mob,
      final NemesisMark mark,
      final DamageSource source) {
    final NemesisLedger ledger = NemesisLedger.of(world.getServer());
    final Optional<NemesisEntry> entry =
        ledger.entry(mark.quarry(), mob.getUuid()).filter(held -> held.current(mark));
    if (entry.isEmpty()) {
      return;
    }
    ledger.remove(mark.quarry(), mob.getUuid());
    final Optional<UUID> ender =
        NemesisRules.ender(
            Optional.ofNullable(source.getAttacker())
                .filter(PlayerEntity.class::isInstance)
                .map(Entity::getUuid),
            source.isOf(NightbornDamage.SUN),
            Optional.ofNullable(Downed.state(mob)).flatMap(DownedState::downedBy));
    if (!NemesisRules.endedByQuarry(ender, mark.quarry())) {
      return;
    }
    NemesisKinds.byId(world.getRegistryManager(), entry.get().kind())
        .ifPresent(
            kind -> {
              mob.dropStack(NemesisItems.keepsake(kind.keepsake(), mob.getDisplayName()));
              final ServerPlayerEntity quarry =
                  world.getServer().getPlayerManager().getPlayer(mark.quarry());
              if (quarry != null) {
                pay(quarry, mob, kind);
              }
            });
  }

  private static void pay(
      final ServerPlayerEntity quarry, final MobEntity mob, final NemesisKind kind) {
    final SkillSettings skills = ServerConfigService.current().skills();
    SkillEarning.fromNemesis(
        quarry,
        NemesisRules.reward(
            SkillEarning.huntExperience(skills, StageRanks.of(mob)), kind.experienceTimes()),
        skills);
    Deeds.record(quarry, Deed.of(DeedKinds.NEMESIS_ENDED));
    quarry.sendMessage(Text.translatable(ENDED, mob.getDisplayName()), false);
  }

  private static void fellTo(final ServerPlayerEntity player, final DamageSource source) {
    if (source.getAttacker() instanceof MobEntity mob && Nemeses.isQuarry(mob, player)) {
      NemesisTaunts.say(mob, player, Moment.DOWNED);
    }
  }
}
