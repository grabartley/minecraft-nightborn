package com.grahambartley.nightborn.guild.veteran;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.guild.GuildData;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

final class VeteranAttacks {

  static final int MARK_EVERY_TICKS = 4;
  static final int STILL_TICKS = 20;
  static final double STILL_MOVE = 0.05;
  static final double CROWD_REACH = 4.0;
  static final double MARK_LIFT = 0.1;

  private final VeteranHunterEntity veteran;
  private final Map<Identifier, Long> readyAt = new HashMap<>();
  @Nullable private Identifier current;
  @Nullable private VeteranAttack attack;
  private int left;
  private Vec3d aim = Vec3d.ZERO;
  private List<Vec3d> marks = List.of();
  @Nullable private Vec3d lastTargetPos;
  private int stillTicks;
  private boolean dodged;
  private ItemStack sheathed = ItemStack.EMPTY;

  VeteranAttacks(final VeteranHunterEntity veteran) {
    this.veteran = veteran;
  }

  boolean busy() {
    return current != null;
  }

  void tick(final Map<Identifier, VeteranAttack> attacks) {
    final LivingEntity target = veteran.getTarget();
    final boolean hunting = target != null && target.isAlive();
    final double moved = track(hunting ? target : null);
    if (current != null) {
      windup(hunting ? target : null, moved);
      return;
    }
    if (!hunting || attacks.isEmpty()) {
      return;
    }
    VeteranChoice.pick(attacks, readyAt, veteran.getWorld().getTime(), situation(target, attacks))
        .ifPresent(id -> begin(id, attacks.get(id), target));
  }

  private double track(@Nullable final LivingEntity target) {
    if (target == null) {
      lastTargetPos = null;
      stillTicks = 0;
      return 0.0;
    }
    final Vec3d now = target.getPos();
    final double moved = lastTargetPos == null ? 0.0 : now.distanceTo(lastTargetPos);
    stillTicks = moved < STILL_MOVE ? stillTicks + 1 : 0;
    lastTargetPos = now;
    return moved;
  }

  private VeteranChoice.Situation situation(
      final LivingEntity target, final Map<Identifier, VeteranAttack> attacks) {
    final double rallyReach =
        attacks.values().stream()
            .filter(a -> a.kind() == VeteranAttack.Kind.RALLYING_CRY)
            .mapToDouble(VeteranAttack::radius)
            .max()
            .orElse(0.0);
    final List<HunterEntity> allies = VeteranStrikes.allies(veteran, rallyReach);
    return new VeteranChoice.Situation(
        veteran.canSee(target),
        veteran.distanceTo(target),
        !target.isOnGround(),
        BatForm.isBat(target),
        veteran
            .getWorld()
            .getEntitiesByClass(
                LivingEntity.class,
                new Box(target.getBlockPos()).expand(CROWD_REACH),
                Vampires::isVampire)
            .size(),
        stillTicks >= STILL_TICKS,
        allies.stream().anyMatch(MindTies::mesmerised),
        VeteranChoice.partyHealth(
            allies.stream().mapToDouble(LivingEntity::getHealth).sum(),
            allies.stream().mapToDouble(LivingEntity::getMaxHealth).sum()));
  }

  private void begin(final Identifier id, final VeteranAttack chosen, final LivingEntity target) {
    current = id;
    attack = chosen;
    left = chosen.windupTicks();
    dodged = false;
    aim = target.getPos();
    marks =
        switch (chosen.kind()) {
          case BOLT_VOLLEY -> VeteranAim.line(veteran.getPos(), aim, VeteranAim.MARK_STEP);
          case HOLY_WATER_ARC ->
              VeteranAim.arc(veteran.getPos(), aim, chosen.count(), VeteranAim.ARC_SPACING);
          case SILVER_CHAIN, RALLYING_CRY -> List.of();
        };
    chosen
        .held()
        .flatMap(GuildData::item)
        .ifPresent(
            item -> {
              sheathed = veteran.getMainHandStack().copy();
              veteran.equipStack(EquipmentSlot.MAINHAND, new ItemStack(item));
            });
    final SoundEvent sound = Registries.SOUND_EVENT.get(chosen.sound());
    if (sound != null) {
      veteran.playSound(sound, 1.0F, 1.0F);
    }
    if (chosen.shout()) {
      veteran.shout();
    }
    veteran.showAttack(chosen.clip());
  }

  private void windup(@Nullable final LivingEntity target, final double moved) {
    final VeteranAttack winding = attack;
    veteran.getNavigation().stop();
    veteran.getLookControl().lookAt(aim.x, aim.y + 1.0, aim.z);
    if (VeteranChoice.dodged(moved)) {
      dodged = true;
    }
    if (left % MARK_EVERY_TICKS == 0) {
      mark(winding.mark());
    }
    if (--left > 0) {
      return;
    }
    switch (winding.kind()) {
      case SILVER_CHAIN -> {
        if (target != null) {
          VeteranStrikes.chain(veteran, target, winding, dodged);
        }
      }
      case BOLT_VOLLEY -> VeteranStrikes.volley(veteran, aim, winding);
      case HOLY_WATER_ARC -> VeteranStrikes.arc(veteran, marks, winding);
      case RALLYING_CRY -> VeteranStrikes.rally(veteran, winding);
    }
    readyAt.put(current, veteran.getWorld().getTime() + winding.cooldownTicks());
    if (winding.held().isPresent()) {
      veteran.equipStack(EquipmentSlot.MAINHAND, sheathed);
      sheathed = ItemStack.EMPTY;
    }
    current = null;
    attack = null;
    veteran.showAttack("");
  }

  private void mark(final Identifier particle) {
    if (marks.isEmpty()
        || !(veteran.getWorld() instanceof ServerWorld world)
        || !(Registries.PARTICLE_TYPE.get(particle) instanceof SimpleParticleType simple)) {
      return;
    }
    final ParticleEffect effect = simple;
    for (final Vec3d spot : marks) {
      world.spawnParticles(effect, spot.x, spot.y + MARK_LIFT, spot.z, 1, 0.0, 0.0, 0.0, 0.0);
    }
  }
}
