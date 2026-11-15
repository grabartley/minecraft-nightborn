package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.VampirePose;
import com.grahambartley.nightborn.mob.look.VampireLook;
import com.grahambartley.nightborn.mob.look.VampireRig;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class VampireModel extends GeoModel<VampireEntity> {

  static final Identifier FALLBACK_TEXTURE =
      Identifier.of(Nightborn.MOD_ID, "textures/entity/vampire/male_corvin.png");

  @Override
  public Identifier getModelResource(final VampireEntity vampire) {
    return vampire.rig().geo();
  }

  @Override
  public Identifier getTextureResource(final VampireEntity vampire) {
    return vampire.look().map(VampireLook::texture).orElse(FALLBACK_TEXTURE);
  }

  @Override
  public Identifier getAnimationResource(final VampireEntity vampire) {
    return VampireRig.animation();
  }

  @Override
  public void setCustomAnimations(
      final VampireEntity vampire,
      final long instanceId,
      final AnimationState<VampireEntity> animationState) {
    RigArmorLayer.hideAnchors(getAnimationProcessor());
    final GeoBone head = getAnimationProcessor().getBone("head");
    if (head == null || vampire.shownPose().holdsHead()) {
      return;
    }
    final EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
    head.setRotX(head.getRotX() + data.headPitch() * MathHelper.RADIANS_PER_DEGREE);
    head.setRotY(head.getRotY() + data.netHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
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
