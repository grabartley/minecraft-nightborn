package com.grahambartley.nightborn.mob.stage;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillAttributes;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public final class VampireStaging {

  public static final Identifier NONE = Identifier.of(Nightborn.MOD_ID, "none");

  private VampireStaging() {}

  public static Identifier begin(
      final LivingEntity vampire,
      final DynamicRegistryManager registries,
      final Map<Identifier, Integer> odds,
      final Random random) {
    return become(
        vampire,
        registries,
        StageOdds.pick(VampireStages.stages(registries), odds, random.nextDouble()).orElse(NONE),
        random);
  }

  public static Identifier become(
      final LivingEntity vampire,
      final DynamicRegistryManager registries,
      final Identifier id,
      final Random random) {
    final VampireStage stage = VampireStages.stage(registries, id).orElse(VampireStage.PLAIN);
    final Map<Identifier, Skill> skills = Skills.allSkills(registries);
    final Map<Identifier, SkillBranch> branches = Skills.allBranches(registries);
    final List<Identifier> picks =
        SkillPicks.pick(
            SkillPicks.ofTree(skills, branches, SkillTree.VAMPIRE),
            stage.skills(),
            stage.skills().count().get(random),
            random::nextDouble);
    SkillAttachment.set(vampire, SkillTree.VAMPIRE, learned(picks, skills, branches));
    StageStats.apply(vampire, stage.body());
    SkillAttributes.apply(vampire);
    vampire.setHealth(vampire.getMaxHealth());
    return id;
  }

  public static TreeProgress learned(
      final List<Identifier> picks,
      final Map<Identifier, Skill> skills,
      final Map<Identifier, SkillBranch> branches) {
    TreeProgress progress = TreeProgress.EMPTY;
    for (final Identifier id : picks) {
      final Skill skill = skills.get(id);
      progress =
          progress.buy(
              id, skill.cost(), SkillPurchase.bloodline(skill, branches.get(skill.branch())));
    }
    return progress.withEarned(progress.spent());
  }
}
