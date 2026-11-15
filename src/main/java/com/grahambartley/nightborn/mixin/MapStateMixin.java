package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.vampire.Vampires;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MapState.class)
abstract class MapStateMixin {

  @Shadow
  protected abstract void removeDecoration(String id);

  @WrapOperation(
      method = "update",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/item/map/MapState;addDecoration(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/world/WorldAccess;Ljava/lang/String;DDDLnet/minecraft/text/Text;)V",
              ordinal = 0))
  private void nightborn$hideVampires(
      final MapState state,
      final RegistryEntry<MapDecorationType> type,
      final WorldAccess world,
      final String key,
      final double x,
      final double z,
      final double rotation,
      final Text text,
      final Operation<Void> original,
      @Local final MapState.PlayerUpdateTracker tracker) {
    if (Vampires.isVampire(tracker.player)) {
      removeDecoration(key);
    } else {
      original.call(state, type, world, key, x, z, rotation, text);
    }
  }
}
