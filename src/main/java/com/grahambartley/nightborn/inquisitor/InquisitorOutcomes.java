package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.hunt.Hunts;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillEarning;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class InquisitorOutcomes {

  static final String DRAINED = "nightborn.inquisitor.drained";
  static final String ENDED = "nightborn.inquisitor.ended";

  public enum Death {
    DRAINED,
    STRUCK_DOWN,
    OTHER
  }

  private InquisitorOutcomes() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(InquisitorOutcomes::afterDeath);
  }

  public static Death death(
      final boolean bitten, final boolean byVampire, final boolean byTurnedHuman) {
    if (bitten && byVampire) {
      return Death.DRAINED;
    }
    return byTurnedHuman ? Death.STRUCK_DOWN : Death.OTHER;
  }

  public static boolean pays(final Death death) {
    return death != Death.OTHER;
  }

  public static boolean clearsNotoriety(final Death death) {
    return death == Death.DRAINED;
  }

  public static boolean comesBack(final InquisitorEnding ending, final boolean bell) {
    return ending == InquisitorEnding.EMPTY && !bell;
  }

  public static void leave(
      final GrandInquisitorEntity inquisitor,
      final ServerWorld world,
      final InquisitorEnding ending) {
    inquisitor
        .quarry()
        .ifPresent(
            quarry -> {
              final ServerPlayerEntity hunted =
                  world.getServer().getPlayerManager().getPlayer(quarry.player());
              if (hunted != null) {
                if (comesBack(ending, quarry.bell())) {
                  Hunts.returnLater(hunted);
                } else {
                  Hunts.endVisit(hunted);
                }
              }
            });
    world.spawnParticles(
        ParticleTypes.END_ROD,
        inquisitor.getX(),
        inquisitor.getBodyY(0.5),
        inquisitor.getZ(),
        40,
        0.4,
        0.8,
        0.4,
        0.05);
    world.playSound(
        null,
        inquisitor.getBlockPos(),
        SoundEvents.BLOCK_BELL_RESONATE,
        SoundCategory.HOSTILE,
        3.0F,
        0.5F);
    inquisitor.depart();
  }

  private static void afterDeath(final LivingEntity dead, final DamageSource source) {
    final MinecraftServer server = dead.getServer();
    if (server == null || !(dead instanceof GrandInquisitorEntity inquisitor)) {
      return;
    }
    final Death death =
        death(
            source.isOf(NightbornDamage.BITE),
            Vampires.isVampire(source.getAttacker()),
            source.getAttacker() != null
                && inquisitor
                    .quarry()
                    .map(quarry -> quarry.turnedOn(source.getAttacker().getUuid()))
                    .orElse(false));
    inquisitor
        .quarry()
        .map(quarry -> server.getPlayerManager().getPlayer(quarry.player()))
        .ifPresent(Hunts::endVisit);
    if (!pays(death)) {
      return;
    }
    for (final UUID id : inquisitor.fighters()) {
      reward(server.getPlayerManager().getPlayer(id), inquisitor, death);
    }
  }

  private static void reward(
      @Nullable final ServerPlayerEntity vampire,
      final GrandInquisitorEntity inquisitor,
      final Death death) {
    if (vampire == null || !Vampires.isVampire(vampire)) {
      return;
    }
    if (clearsNotoriety(death)) {
      Hunts.set(vampire, 0.0);
      Hunts.endVisit(vampire);
    }
    SkillEarning.fromInquisitor(
        vampire,
        ServerConfigService.current().inquisitor().experience(),
        ServerConfigService.current().skills());
    vampire.sendMessage(
        Text.translatable(clearsNotoriety(death) ? DRAINED : ENDED, inquisitor.getDisplayName()),
        false);
    vampire
        .getServerWorld()
        .playSound(
            null,
            vampire.getBlockPos(),
            SoundEvents.BLOCK_BELL_RESONATE,
            SoundCategory.PLAYERS,
            1.0F,
            0.6F);
  }
}
