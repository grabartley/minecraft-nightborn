package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.VampireLook;
import com.grahambartley.nightborn.mob.VampirePose;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class VampireModel extends GeoModel<VampireEntity> {

  @Override
  public Identifier getModelResource(final VampireEntity vampire) {
    return vampire.look().geo();
  }

  @Override
  public Identifier getTextureResource(final VampireEntity vampire) {
    return vampire.look().texture();
  }

  @Override
  public Identifier getAnimationResource(final VampireEntity vampire) {
    return VampireLook.animation();
  }

  @Override
  public void setCustomAnimations(
      final VampireEntity vampire,
      final long instanceId,
      final AnimationState<VampireEntity> animationState) {
    final GeoBone head = getAnimationProcessor().getBone("head");
    if (head == null || VisibleStateAttachment.get(vampire).downed()) {
      return;
    }
    final EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
    head.setRotX(data.headPitch() * MathHelper.RADIANS_PER_DEGREE);
    head.setRotY(data.netHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
  }

  @Override
  public void applyMolangQueries(
      final AnimationState<VampireEntity> animationState, final double animTime) {
    final VampireEntity vampire = animationState.getAnimatable();
    final double spread =
        VampirePose.spread(
            vampire.isSprinting(),
            Math.hypot(vampire.getX() - vampire.prevX, vampire.getZ() - vampire.prevZ));
    MathParser.setVariable(VampirePose.SPREAD_VARIABLE, () -> spread);
  }
}
