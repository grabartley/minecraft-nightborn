package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedFacts;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
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

  public static List<InquisitorLedger.Owed> owedOnLeaving(
      final InquisitorEnding ending, final boolean bell) {
    return List.of(
        comesBack(ending, bell)
            ? InquisitorLedger.Owed.COME_BACK
            : InquisitorLedger.Owed.END_VISIT);
  }

  public static List<InquisitorLedger.Owed> owedToFighter(final Death death) {
    return switch (death) {
      case DRAINED ->
          List.of(
              InquisitorLedger.Owed.CLEAR_NOTORIETY,
              InquisitorLedger.Owed.END_VISIT,
              InquisitorLedger.Owed.PAY,
              InquisitorLedger.Owed.ENDED);
      case STRUCK_DOWN -> List.of(InquisitorLedger.Owed.PAY);
      case OTHER -> List.of();
    };
  }

  public static void leave(
      final GrandInquisitorEntity inquisitor,
      final ServerWorld world,
      final InquisitorEnding ending) {
    inquisitor
        .quarry()
        .ifPresent(
            quarry ->
                InquisitorDues.owe(
                    world.getServer(), quarry.player(), owedOnLeaving(ending, quarry.bell())));
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
    if (death == Death.DRAINED && source.getAttacker() instanceof ServerPlayerEntity drainer) {
      Deeds.record(
          drainer,
          Deed.of(DeedKinds.INQUISITOR_DRAINED)
              .with(
                  DeedKinds.RANG_BELL,
                  inquisitor
                      .quarry()
                      .filter(q -> q.bell() && q.player().equals(drainer.getUuid()))
                      .isPresent())
              .with(DeedKinds.ARMOURED, DeedFacts.armoured(drainer)));
    }
    inquisitor
        .quarry()
        .ifPresent(
            quarry ->
                InquisitorDues.owe(
                    server, quarry.player(), List.of(InquisitorLedger.Owed.END_VISIT)));
    if (!pays(death)) {
      return;
    }
    for (final UUID id : inquisitor.fighters()) {
      InquisitorDues.owe(server, id, owedToFighter(death));
      tell(server.getPlayerManager().getPlayer(id), inquisitor, death);
    }
  }

  private static void tell(
      @Nullable final ServerPlayerEntity vampire,
      final GrandInquisitorEntity inquisitor,
      final Death death) {
    if (vampire == null || !Vampires.isVampire(vampire)) {
      return;
    }
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
