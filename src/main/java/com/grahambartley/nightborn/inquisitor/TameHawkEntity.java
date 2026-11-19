package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.eldest.PetBatEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;

public class TameHawkEntity extends PetBatEntity {

  static final int BLEND_TICKS = 5;

  public TameHawkEntity(final EntityType<? extends BatEntity> type, final World world) {
    super(type, world);
  }

  @Override
  protected Item homeItem() {
    return InquisitorSpoils.TAME_HAWK;
  }

  @Nullable
  @Override
  public SoundEvent getAmbientSound() {
    return null;
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    return SoundEvents.ENTITY_PARROT_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.ENTITY_PARROT_DEATH;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "flight",
            BLEND_TICKS,
            state -> state.setAndContinue(HawkFlight.of(getVelocity().y).clip())));
  }
}
