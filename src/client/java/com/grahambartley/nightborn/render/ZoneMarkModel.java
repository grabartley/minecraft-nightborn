package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import java.util.Optional;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class ZoneMarkModel extends GeoModel<ZoneMarkEntity> {

  static final Identifier GEO = Identifier.of(Nightborn.MOD_ID, "geo/entity/zone_mark.geo.json");
  static final Identifier ANIMATION =
      Identifier.of(Nightborn.MOD_ID, "animations/entity/zone_mark.animation.json");
  static final Identifier CIRCLE =
      Identifier.of(Nightborn.MOD_ID, "textures/entity/boss/zone_mark_circle.png");
  static final Identifier LINE =
      Identifier.of(Nightborn.MOD_ID, "textures/entity/boss/zone_mark_line.png");

  static final String MARKS = "textures/entity/boss/mark/";

  static Identifier texture(final boolean line, final Optional<Identifier> look) {
    return look.map(id -> Identifier.of(id.getNamespace(), MARKS + id.getPath() + ".png"))
        .orElse(line ? LINE : CIRCLE);
  }

  @Override
  public Identifier getModelResource(final ZoneMarkEntity mark) {
    return GEO;
  }

  @Override
  public Identifier getTextureResource(final ZoneMarkEntity mark) {
    return texture(mark.line(), mark.look());
  }

  @Override
  public Identifier getAnimationResource(final ZoneMarkEntity mark) {
    return ANIMATION;
  }
}
