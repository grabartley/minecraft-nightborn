package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.levitation.Levitation;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobNavigation.class)
abstract class MobNavigationMixin extends EntityNavigation {

  MobNavigationMixin(final MobEntity entity, final World world) {
    super(entity, world);
  }

  @ModifyReturnValue(method = "isAtValidPosition", at = @At("RETURN"))
  private boolean nightborn$pathWhileLevitating(final boolean valid) {
    return valid || Levitation.isLevitating(entity);
  }
}
