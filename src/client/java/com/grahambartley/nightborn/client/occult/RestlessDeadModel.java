package com.grahambartley.nightborn.client.occult;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.occult.RestlessDead;
import com.grahambartley.nightborn.occult.RestlessDeadEntity;
import com.grahambartley.nightborn.occult.RestlessLook;
import java.util.Optional;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class RestlessDeadModel extends GeoModel<RestlessDeadEntity> {

  static final Identifier CLIPS =
      Identifier.of(Nightborn.MOD_ID, "animations/entity/restless_dead.animation.json");
  static final Identifier FALLBACK_TEXTURE =
      Identifier.of(Nightborn.MOD_ID, "textures/entity/restless_dead/male_cavalier.png");

  private static Optional<RestlessLook> look(final RestlessDeadEntity dead) {
    return RestlessLook.worn(RestlessDead.looks(dead.getRegistryManager()), dead.look());
  }

  @Override
  public Identifier getModelResource(final RestlessDeadEntity dead) {
    return look(dead).map(RestlessLook::rig).orElse(VampireRig.MALE).geo();
  }

  @Override
  public Identifier getTextureResource(final RestlessDeadEntity dead) {
    return look(dead).map(RestlessLook::texture).orElse(FALLBACK_TEXTURE);
  }

  @Override
  public Identifier getAnimationResource(final RestlessDeadEntity dead) {
    return CLIPS;
  }
}
