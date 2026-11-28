package com.grahambartley.nightborn.guild.veteran;

import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.boss.ZoneShape;
import com.grahambartley.nightborn.boss.ZoneStrikes;
import com.grahambartley.nightborn.guild.GuildData;
import com.grahambartley.nightborn.mob.brain.ActionReadiness;
import com.grahambartley.nightborn.mob.combat.MobFighting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

final class VeteranAttacks implements MobFighting.Moves {

  static final double MARK_LIFT = 0.02;

  private final VeteranHunterEntity veteran;
  private final Map<Identifier, Long> readyAt = new HashMap<>();
  private final List<ZoneMarkEntity> marks = new ArrayList<>();
  @Nullable private Identifier current;
  @Nullable private VeteranAttack attack;
  private int left;
  private Vec3d aim = Vec3d.ZERO;
  private List<Vec3d> spots = List.of();
  @Nullable private Vec3d lastTargetPos;
  private boolean dodged;
  private ItemStack sheathed = ItemStack.EMPTY;

  VeteranAttacks(final VeteranHunterEntity veteran) {
    this.veteran = veteran;
  }

  @Override
  public void tick() {
    final LivingEntity target = hunted();
    final double moved = track(target);
    if (current != null) {
      windup(target, moved);
    }
  }

  @Override
  public List<ActionReadiness> actions() {
    return readiness(
        veteran.attacks(),
        readyAt,
        veteran.getWorld().getTime(),
        current == null,
        hunted() != null);
  }

  static List<ActionReadiness> readiness(
      final Map<Identifier, VeteranAttack> attacks,
      final Map<Identifier, Long> readyAt,
      final long now,
      final boolean free,
      final boolean hunting) {
    if (!free || !hunting) {
      return List.of();
    }
    final List<ActionReadiness> ready = new ArrayList<>(attacks.size());
    attacks.forEach(
        (id, attack) ->
            ready.add(
                ActionReadiness.of(
                    id, now >= readyAt.getOrDefault(id, Long.MIN_VALUE), attack.cooldownTicks())));
    return ready;
  }

  @Override
  public boolean act(final Identifier action) {
    final LivingEntity target = hunted();
    final VeteranAttack chosen = veteran.attacks().get(action);
    if (current != null || target == null || chosen == null) {
      return false;
    }
    begin(action, chosen, target);
    return true;
  }

  @Override
  public boolean busy() {
    return current != null;
  }

  @Nullable
  private LivingEntity hunted() {
    final LivingEntity target = veteran.getTarget();
    return target != null && target.isAlive() ? target : null;
  }

  void clear() {
    marks.forEach(ZoneMarkEntity::discard);
    marks.clear();
    sheathe();
  }

  private void sheathe() {
    if (!sheathed.isEmpty()) {
      veteran.equipStack(EquipmentSlot.MAINHAND, sheathed);
      sheathed = ItemStack.EMPTY;
    }
  }

  private double track(@Nullable final LivingEntity target) {
    if (target == null) {
      lastTargetPos = null;
      return 0.0;
    }
    final Vec3d now = target.getPos();
    final double moved = lastTargetPos == null ? 0.0 : now.distanceTo(lastTargetPos);
    lastTargetPos = now;
    return moved;
  }

  private void begin(final Identifier id, final VeteranAttack chosen, final LivingEntity target) {
    current = id;
    attack = chosen;
    left = chosen.windupTicks();
    dodged = false;
    aim = target.getPos();
    spots =
        chosen.kind() == VeteranAttack.Kind.HOLY_WATER_ARC
            ? VeteranAim.arc(veteran.getPos(), aim, chosen.count(), VeteranAim.ARC_SPACING)
            : List.of();
    mark(chosen, target);
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

  private void mark(final VeteranAttack chosen, final LivingEntity target) {
    if (chosen.mark().isEmpty() || !(veteran.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final double y = ZoneStrikes.floor(world, target) + MARK_LIFT;
    final List<ZoneShape> shapes =
        switch (chosen.kind()) {
          case BOLT_VOLLEY ->
              List.of(
                  ZoneShape.line(
                      veteran.getX(),
                      veteran.getZ(),
                      aim.x,
                      aim.z,
                      chosen.radius(),
                      Math.max(chosen.reach(), veteran.distanceTo(target))));
          case HOLY_WATER_ARC ->
              spots.stream()
                  .map(spot -> ZoneShape.circle(spot.x, spot.z, chosen.radius()))
                  .toList();
          case SILVER_CHAIN, RALLYING_CRY -> List.of();
        };
    shapes.forEach(shape -> marks.add(ZoneMarkEntity.place(world, shape, y, chosen.mark())));
  }

  private void windup(@Nullable final LivingEntity target, final double moved) {
    final VeteranAttack winding = attack;
    veteran.getNavigation().stop();
    veteran.getLookControl().lookAt(aim.x, aim.y + 1.0, aim.z);
    if (VeteranDodge.dodged(moved)) {
      dodged = true;
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
      case HOLY_WATER_ARC -> VeteranStrikes.arc(veteran, spots, winding);
      case RALLYING_CRY -> VeteranStrikes.rally(veteran, winding);
    }
    if (winding.kind() == VeteranAttack.Kind.HOLY_WATER_ARC) {
      marks.forEach(mark -> mark.linger(winding.effectTicks()));
      marks.clear();
    }
    clear();
    readyAt.put(current, veteran.getWorld().getTime() + winding.cooldownTicks());
    current = null;
    attack = null;
    veteran.showAttack("");
  }
}
