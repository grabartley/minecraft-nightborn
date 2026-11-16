package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.skill.MesmerisePower;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillAttributes;
import com.grahambartley.nightborn.skill.SkillPower;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

final class NobleSkills {

  static final Identifier NOBLE = Identifier.of("nightborn", "noble");
  static final Identifier NOBLE_BLOOD = Identifier.of("nightborn", "noble/noble_blood");
  static final Identifier MESMERISE = Identifier.of("nightborn", "noble/mesmerise");
  static final Identifier SILVER_TONGUE = Identifier.of("nightborn", "noble/silver_tongue");
  static final Identifier BLOOD_BOND = Identifier.of("nightborn", "noble/blood_bond");
  static final int STARE_TICKS = 20;

  private NobleSkills() {}

  static MesmerisePower power(final TestContext context) {
    return Skills.skills(context.getWorld().getRegistryManager())
        .map(registry -> registry.get(MESMERISE))
        .flatMap(Skill::power)
        .flatMap(SkillPower::mesmerise)
        .orElseThrow();
  }

  static void learn(final LivingEntity vampire, final Identifier... skills) {
    TreeProgress progress = TreeProgress.EMPTY.withEarned(20).buy(NOBLE_BLOOD, 1, NOBLE);
    for (final Identifier skill : skills) {
      progress = progress.buy(skill, 1, null);
    }
    SkillAttachment.set(vampire, SkillTree.VAMPIRE, progress);
    SkillAttributes.apply(vampire);
    BloodAttachment.set(vampire, 20);
  }
}
