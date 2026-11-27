package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.dismember.BodyPart;
import com.grahambartley.nightborn.dismember.BodyState;
import com.grahambartley.nightborn.dismember.Dismemberment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import software.bernie.geckolib.cache.object.GeoBone;

public final class LostLimbs {

  private LostLimbs() {}

  public static boolean hidden(final LivingEntity entity, final GeoBone bone) {
    return entity != null && hidden(Dismemberment.state(entity), bone.getName());
  }

  static boolean hidden(final BodyState body, final String bone) {
    return !body.whole() && BodyPart.named(bone).filter(body::missing).isPresent();
  }

  public static void hide(final BipedEntityModel<?> model, final BodyState body) {
    if (body.missing(BodyPart.HEAD)) {
      model.head.visible = false;
      model.hat.visible = false;
    }
    if (body.missing(BodyPart.RIGHT_ARM)) {
      model.rightArm.visible = false;
    }
    if (body.missing(BodyPart.LEFT_ARM)) {
      model.leftArm.visible = false;
    }
    if (body.missing(BodyPart.RIGHT_LEG)) {
      model.rightLeg.visible = false;
    }
    if (body.missing(BodyPart.LEFT_LEG)) {
      model.leftLeg.visible = false;
    }
    if (model instanceof PlayerEntityModel<?> player) {
      player.rightSleeve.visible &= player.rightArm.visible;
      player.leftSleeve.visible &= player.leftArm.visible;
      player.rightPants.visible &= player.rightLeg.visible;
      player.leftPants.visible &= player.leftLeg.visible;
    }
  }
}
