package com.grahambartley.nightborn.lord;

import com.grahambartley.nightborn.servant.Bindable;
import com.grahambartley.nightborn.skill.Powers;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;

public final class Summons {

  private Summons() {}

  static void bind(final LivingEntity caster, final MobEntity mob, final SummonPower power) {
    power
        .health()
        .ifPresent(
            health -> {
              mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(health);
              mob.setHealth(health);
            });
    power
        .damage()
        .filter(damage -> mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE))
        .ifPresent(
            damage ->
                mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                    .setBaseValue(damage));
    if (mob instanceof Bindable bindable) {
      bindable.bind(caster.getUuid(), power.lifetimeTicks().orElse(Integer.MAX_VALUE));
    }
  }

  public static List<Entity> call(final LivingEntity caster, final SummonPower power) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return List.of();
    }
    final Random random = caster.getRandom();
    final LivingEntity foe = Powers.foe(caster);
    final List<Entity> called = new ArrayList<>();
    final int count = power.count().get(random);
    for (int i = 0; i < count; i++) {
      final Entity summoned = power.entity().value().create(world);
      if (summoned == null) {
        continue;
      }
      summoned.refreshPositionAndAngles(
          caster.getX() + power.offset(random.nextDouble()),
          caster.getY() + power.rise(),
          caster.getZ() + power.offset(random.nextDouble()),
          random.nextFloat() * 360.0F,
          0.0F);
      if (!world.isSpaceEmpty(summoned)) {
        summoned.discard();
        continue;
      }
      if (summoned instanceof MobEntity mob) {
        mob.initialize(
            world, world.getLocalDifficulty(mob.getBlockPos()), SpawnReason.MOB_SUMMONED, null);
        bind(caster, mob, power);
        if (foe != null) {
          mob.setTarget(foe);
        }
      }
      world.spawnEntityAndPassengers(summoned);
      called.add(summoned);
    }
    return called;
  }
}
