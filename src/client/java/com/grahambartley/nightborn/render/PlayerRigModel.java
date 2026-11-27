package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.client.anim.PlayerClips;
import com.grahambartley.nightborn.mob.VampirePose;
import com.grahambartley.nightborn.mob.look.VampireRig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.model.GeoModel;

public final class PlayerRigModel extends GeoModel<PlayerRigAnimatable> {

  static final Identifier STEVE = Identifier.ofVanilla("textures/entity/player/wide/steve.png");

  private final Identifier geo;

  public PlayerRigModel(final boolean slim) {
    this.geo =
        Identifier.of(
            Nightborn.MOD_ID, "geo/entity/player_" + (slim ? "slim" : "wide") + ".geo.json");
  }

  @Override
  public Identifier getModelResource(final PlayerRigAnimatable animatable) {
    return geo;
  }

  @Override
  public Identifier getTextureResource(final PlayerRigAnimatable animatable) {
    return STEVE;
  }

  @Override
  public Identifier getAnimationResource(final PlayerRigAnimatable animatable) {
    return VampireRig.sharedClips();
  }

  @Override
  public void setCustomAnimations(
      final PlayerRigAnimatable animatable,
      final long instanceId,
      final AnimationState<PlayerRigAnimatable> animationState) {
    RigArmorLayer.hideAnchors(getAnimationProcessor());
    final GeoBone mouth = getAnimationProcessor().getBone(PlayerRigRenderer.MOUTH);
    if (mouth != null) {
      mouth.setTrackingMatrices(true);
    }
    final GeoBone head = getAnimationProcessor().getBone("head");
    if (head == null
        || !(animationState.getData(DataTickets.ENTITY) instanceof PlayerEntity player)
        || PlayerClips.clip(player).map(VampirePose::holdsHead).orElse(false)) {
      return;
    }
    HeadLook.apply(head, animationState.getData(DataTickets.ENTITY_MODEL_DATA));
  }

  @Override
  public void applyMolangQueries(
      final AnimationState<PlayerRigAnimatable> animationState, final double animTime) {
    final double spread =
        animationState.getData(DataTickets.ENTITY) instanceof PlayerEntity player
            ? PlayerClips.levitationSpread(player)
            : 0.0;
    MathParser.setVariable(VampirePose.SPREAD_VARIABLE, () -> spread);
  }
}
