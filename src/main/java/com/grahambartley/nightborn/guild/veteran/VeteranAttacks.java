package com.grahambartley.nightborn.guild.veteran;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.boss.ZoneShape;
import com.grahambartley.nightborn.boss.ZoneStrikes;
import com.grahambartley.nightborn.guild.GuildData;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.guild.raiding.RaidOrders;
import com.grahambartley.nightborn.guild.raiding.RaidingParties;
import com.grahambartley.nightborn.guild.raiding.RaidingParty;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

final class VeteranAttacks {

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
  private int stillTicks;
  private boolean dodged;
  private ItemStack sheathed = ItemStack.EMPTY;

  VeteranAttacks(final VeteranHunterEntity veteran) {
    this.veteran = veteran;
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

  void clear() {
    marks.forEach(ZoneMarkEntity::discard);
    marks.clear();
  }

  private double track(@Nullable final LivingEntity target) {
    if (target == null) {
      lastTargetPos = null;
      stillTicks = 0;
      return 0.0;
    }
    final Vec3d now = target.getPos();
    final double moved = lastTargetPos == null ? 0.0 : now.distanceTo(lastTargetPos);
    stillTicks = VeteranChoice.stillTicks(stillTicks, moved);
    lastTargetPos = now;
    return moved;
  }

  private VeteranChoice.Situation situation(
      final LivingEntity target, final Map<Identifier, VeteranAttack> attacks) {
    final double crowdReach = most(attacks, VeteranAttack.Trigger::crowdReach);
    final List<HunterEntity> allies =
        VeteranStrikes.allies(
            veteran,
            attacks.values().stream()
                .filter(a -> a.kind() == VeteranAttack.Kind.RALLYING_CRY)
                .mapToDouble(VeteranAttack::radius)
                .max()
                .orElse(0.0));
    return new VeteranChoice.Situation(
        veteran.canSee(target),
        veteran.distanceTo(target),
        !target.isOnGround(),
        BatForm.isBat(target),
        veteran
            .getWorld()
            .getEntitiesByClass(
                LivingEntity.class,
                new Box(target.getBlockPos()).expand(crowdReach),
                Vampires::isVampire)
            .size(),
        stillTicks,
        allies.stream().anyMatch(MindTies::mesmerised),
        partyHealth(allies));
  }

  private static double most(
      final Map<Identifier, VeteranAttack> attacks,
      final ToDoubleFunction<VeteranAttack.Trigger> field) {
    return attacks.values().stream()
        .map(VeteranAttack::trigger)
        .mapToDouble(field)
        .max()
        .orElse(0.0);
  }

  private double partyHealth(final List<HunterEntity> allies) {
    final double hunterMax =
        DefaultAttributeRegistry.get(GuildMobs.HUNTER)
            .getBaseValue(EntityAttributes.GENERIC_MAX_HEALTH);
    final Optional<RaidingParty> raid =
        veteran.getServer() == null
            ? Optional.empty()
            : veteran
                .raid()
                .map(RaidOrders::quarry)
                .flatMap(quarry -> RaidingParties.get(veteran.getServer()).against(quarry));
    if (raid.isEmpty()) {
      return VeteranChoice.partyHealth(
          allies.stream().mapToDouble(LivingEntity::getHealth).sum(),
          allies.stream().mapToDouble(LivingEntity::getMaxHealth).sum(),
          0,
          0,
          hunterMax);
    }
    final List<LivingEntity> standing =
        raid.get().members().stream()
            .map(id -> ((ServerWorld) veteran.getWorld()).getEntity(id))
            .filter(LivingEntity.class::isInstance)
            .map(LivingEntity.class::cast)
            .toList();
    return VeteranChoice.partyHealth(
        standing.stream().mapToDouble(LivingEntity::getHealth).sum(),
        standing.stream().mapToDouble(LivingEntity::getMaxHealth).sum(),
        raid.get().members().size() - standing.size(),
        raid.get().size() - raid.get().members().size(),
        hunterMax);
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
    if (VeteranChoice.dodged(moved)) {
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
    } else {
      clear();
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
}
