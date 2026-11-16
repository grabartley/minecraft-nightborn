package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillAttributes;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

final class OccultSkills {

  static final Identifier OCCULT = Identifier.of("nightborn", "occult");
  static final Identifier OCCULT_BLOOD = Identifier.of("nightborn", "occult/occult_blood");
  static final Identifier SUMMON_FAMILIAR = Identifier.of("nightborn", "occult/summon_familiar");
  static final Identifier SPIRIT_PACT = Identifier.of("nightborn", "occult/spirit_pact");
  static final Identifier GRAVE_FOG = Identifier.of("nightborn", "occult/grave_fog");
  static final Identifier SHADOW_STEP = Identifier.of("nightborn", "occult/shadow_step");

  private OccultSkills() {}

  static void learn(final LivingEntity vampire, final Identifier... skills) {
    TreeProgress progress = TreeProgress.EMPTY.withEarned(20).buy(OCCULT_BLOOD, 1, OCCULT);
    for (final Identifier skill : skills) {
      progress = progress.buy(skill, 1, null);
    }
    SkillAttachment.set(vampire, SkillTree.VAMPIRE, progress);
    SkillAttributes.apply(vampire);
    BloodAttachment.set(vampire, 20);
  }
}
