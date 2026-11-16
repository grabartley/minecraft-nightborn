package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.util.Identifier;

public enum HunterLook {
  MALE("hunter_male", "hunter/male"),
  FEMALE("hunter_female", "hunter/female"),
  GUILD_MASTER("guild_master", "hunter/guild_master");

  public static final Identifier ANIMATION =
      Identifier.of(Nightborn.MOD_ID, "animations/entity/hunter.animation.json");

  private final Identifier geo;
  private final Identifier texture;

  HunterLook(final String geo, final String texture) {
    this.geo = Identifier.of(Nightborn.MOD_ID, "geo/entity/" + geo + ".geo.json");
    this.texture = Identifier.of(Nightborn.MOD_ID, "textures/entity/" + texture + ".png");
  }

  public Identifier geo() {
    return geo;
  }

  public Identifier texture() {
    return texture;
  }

  public static HunterLook hunter(final boolean female) {
    return female ? FEMALE : MALE;
  }
}
