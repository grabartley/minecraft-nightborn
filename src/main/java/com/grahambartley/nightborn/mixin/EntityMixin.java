package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.name.NameDisplay;
import com.grahambartley.nightborn.name.NameStyle;
import com.grahambartley.nightborn.name.NameStyles;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
abstract class EntityMixin {

  @ModifyExpressionValue(
      method = "getDisplayName",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/entity/Entity;getName()Lnet/minecraft/text/Text;"))
  private Text nightborn$showName(final Text name) {
    return (Object) this instanceof NameBearer bearer
        ? NameDisplay.shown(
            name, bearer, NameStyles.of((Entity) (Object) this).flatMap(NameStyle::color))
        : name;
  }
}
