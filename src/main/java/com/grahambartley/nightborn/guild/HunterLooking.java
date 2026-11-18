package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.mob.look.Figure;
import net.minecraft.util.Identifier;

public interface HunterLooking extends Figure {

  HunterRig rig();

  Identifier texture();

  default Identifier geo() {
    return rig().geo();
  }

  default Identifier animation() {
    return HunterRig.ANIMATION;
  }
}
