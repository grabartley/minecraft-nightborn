package com.grahambartley.nightborn.mob.stage;

import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;

final class StageFixtures {

  static final Identifier CORE = Identifier.of("nightborn", "core");
  static final Identifier NOBLE = Identifier.of("nightborn", "noble");
  static final Identifier HUNTER = Identifier.of("nightborn", "hunter");
  static final Identifier LORD = Identifier.of("nightborn", "lord");
  static final Identifier THICK = Identifier.of("nightborn", "core/thick_blood");
  static final Identifier RUSH = Identifier.of("nightborn", "core/blood_rush");
  static final Identifier STRENGTH = Identifier.of("nightborn", "core/grave_strength");
  static final Identifier NOBLE_BLOOD = Identifier.of("nightborn", "noble/noble_blood");
  static final Identifier FAITH = Identifier.of("nightborn", "hunter/faith");
  static final Identifier RALLY = Identifier.of("nightborn", "lord/rally");

  static final Map<Identifier, SkillBranch> BRANCHES =
      Map.of(
          CORE, new SkillBranch(SkillTree.VAMPIRE, false, 0),
          NOBLE, new SkillBranch(SkillTree.VAMPIRE, true, 1),
          HUNTER, new SkillBranch(SkillTree.HUNTER, false, 0),
          LORD, new SkillBranch(SkillTree.VAMPIRE, false, 15, false));

  static final Map<Identifier, Skill> SKILLS =
      Map.of(
          THICK, skill(CORE, 1),
          RUSH, skill(CORE, 2, THICK),
          STRENGTH, skill(CORE, 2, THICK),
          NOBLE_BLOOD, skill(NOBLE, 1),
          FAITH, skill(HUNTER, 1),
          RALLY, skill(LORD, 1));

  private StageFixtures() {}

  static Skill skill(final Identifier branch, final int cost, final Identifier... requires) {
    return new Skill(
        branch,
        cost,
        List.of(requires),
        Identifier.of("nightborn", "icon"),
        0,
        0,
        List.of(),
        0.0,
        Optional.empty());
  }

  static StageSkills weights(final Map<Identifier, Integer> weights, final int other) {
    return new StageSkills(ConstantIntProvider.create(0), weights, other);
  }

  static VampireStage stage(final int rank, final int weight) {
    return new VampireStage(
        rank,
        weight,
        StageBody.SAME,
        StageSkills.NONE,
        StageCombat.PLAIN,
        StageLook.PLAIN,
        Optional.empty());
  }
}
