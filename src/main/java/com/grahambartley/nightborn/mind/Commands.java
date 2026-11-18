package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.skill.PowerSight;
import java.util.Comparator;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public final class Commands {

  public static final AttachmentType<Commanded> TYPE =
      AttachmentRegistry.createPersistent(
          Identifier.of(Nightborn.MOD_ID, "commanded"), Commanded.CODEC);

  static final AttachmentType<Long> LAST_STRIKE =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "commanded_strike"));

  private Commands() {}

  public static void register() {}

  public static Optional<CommandAim> aim(final LivingEntity caster, final CommandPower power) {
    return subject(caster, power.range())
        .map(
            subject -> {
              if (caster instanceof MobEntity mob) {
                final Optional<LivingEntity> foe = Optional.ofNullable(mob.getTarget());
                return new CommandAim(
                    subject, CommandOrder.pick(false, foe.isPresent()), foe, Optional.empty());
              }
              final Optional<LivingEntity> foe =
                  Optional.ofNullable(PowerSight.looked(caster, power.range()))
                      .filter(looked -> looked != subject && PowerReach.touches(caster, looked));
              final Optional<BlockPos> door = door(caster, power.range());
              return new CommandAim(
                  subject, CommandOrder.pick(door.isPresent(), foe.isPresent()), foe, door);
            });
  }

  public static void give(
      final LivingEntity caster, final CommandAim aim, final CommandPower power) {
    final LivingEntity subject = aim.subject();
    final long now = subject.getWorld().getTime();
    subject.removeStatusEffect(MindEffect.MESMERISED);
    subject.setAttached(
        TYPE,
        new Commanded(
            caster.getUuid(),
            aim.order(),
            aim.foe().map(Entity::getUuid),
            aim.door(),
            now + power.durationTicks()));
    subject.addStatusEffect(
        new StatusEffectInstance(MindEffect.COMMANDED, power.durationTicks()), caster);
    subject
        .getWorld()
        .playSound(
            null,
            subject.getBlockPos(),
            SoundEvents.ENTITY_ILLUSIONER_PREPARE_MIRROR,
            SoundCategory.HOSTILE,
            0.8F,
            0.7F);
    if (caster instanceof PlayerEntity player) {
      player.sendMessage(
          Text.translatable(aim.order().translationKey(), subject.getDisplayName()), true);
    }
  }

  public static boolean commanded(final Entity entity) {
    return entity.getAttached(TYPE) != null;
  }

  public static boolean drive(final MobEntity mob) {
    final Commanded order = mob.getAttached(TYPE);
    if (order == null) {
      return false;
    }
    final ServerWorld world = (ServerWorld) mob.getWorld();
    final long now = world.getTime();
    final Entity master = world.getEntity(order.master());
    if (order.over(now) || !(master instanceof LivingEntity leader) || !leader.isAlive()) {
      release(mob);
      return false;
    }
    if (Holds.still(mob)) {
      return true;
    }
    switch (order.order()) {
      case ATTACK -> attack(mob, world, order, leader, now);
      case OPEN_DOOR -> openDoor(mob, world, order);
      case FOLLOW ->
          walkTo(mob, leader.getX(), leader.getY(), leader.getZ(), CommandRules.FOLLOW_GAP);
    }
    mob.getNavigation().tick();
    mob.getMoveControl().tick();
    mob.getLookControl().tick();
    mob.getJumpControl().tick();
    return true;
  }

  public static void release(final LivingEntity entity) {
    entity.removeAttached(TYPE);
    entity.removeAttached(LAST_STRIKE);
    entity.removeStatusEffect(MindEffect.COMMANDED);
  }

  private static void attack(
      final MobEntity mob,
      final ServerWorld world,
      final Commanded order,
      final LivingEntity leader,
      final long now) {
    final Entity foe = order.foe().map(world::getEntity).orElse(null);
    if (!(foe instanceof LivingEntity target) || !target.isAlive()) {
      mob.setAttached(TYPE, order.following());
      return;
    }
    mob.getLookControl().lookAt(target, 30.0F, 30.0F);
    final double distance = mob.distanceTo(target);
    walkTo(mob, target.getX(), target.getY(), target.getZ(), 1.0);
    if (CommandRules.strikes(
        distance, now, mob.getAttachedOrElse(LAST_STRIKE, Long.MIN_VALUE / 2))) {
      mob.setAttached(LAST_STRIKE, now);
      mob.swingHand(Hand.MAIN_HAND);
      if (mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
        mob.tryAttack(target);
      } else {
        target.damage(world.getDamageSources().mobAttack(mob), CommandRules.BARE_HAND_DAMAGE);
      }
    }
  }

  private static void openDoor(
      final MobEntity mob, final ServerWorld world, final Commanded order) {
    final BlockPos door = order.door().orElse(null);
    if (door == null) {
      mob.setAttached(TYPE, order.following());
      return;
    }
    walkTo(mob, door.getX() + 0.5, door.getY(), door.getZ() + 0.5, 0.5);
    if (mob.getBlockPos().isWithinDistance(door, CommandRules.DOOR_REACH)) {
      final BlockState state = world.getBlockState(door);
      if (state.getBlock() instanceof DoorBlock block && !block.isOpen(state)) {
        block.setOpen(mob, world, state, door, true);
      }
      mob.setAttached(TYPE, order.following());
    }
  }

  private static void walkTo(
      final MobEntity mob, final double x, final double y, final double z, final double gap) {
    if (CommandRules.closesIn(Math.sqrt(mob.squaredDistanceTo(x, y, z)), gap)) {
      mob.getNavigation().startMovingTo(x, y, z, 1.0);
    } else {
      mob.getNavigation().stop();
    }
  }

  private static Optional<LivingEntity> subject(final LivingEntity caster, final float range) {
    return caster
        .getWorld()
        .getEntitiesByClass(
            LivingEntity.class,
            new Box(caster.getBlockPos()).expand(range),
            candidate ->
                candidate instanceof MobEntity
                    && CommandRules.commandable(
                        candidate instanceof PlayerEntity,
                        MindTies.mesmerisedBy(candidate, caster),
                        candidate.getType().isIn(Holds.UNHOLDABLE)))
        .stream()
        .min(Comparator.comparingDouble(caster::squaredDistanceTo));
  }

  private static Optional<BlockPos> door(final LivingEntity caster, final float range) {
    final HitResult hit = caster.raycast(range, 1.0F, false);
    if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) {
      return Optional.empty();
    }
    final BlockState state = caster.getWorld().getBlockState(block.getBlockPos());
    return state.getBlock() instanceof DoorBlock door && door.getBlockSetType().canOpenByHand()
        ? Optional.of(block.getBlockPos())
        : Optional.empty();
  }
}
