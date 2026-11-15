package com.grahambartley.nightborn.mob.stage;

import com.grahambartley.nightborn.skill.Skill;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.util.Identifier;

public final class StageDataCheck {

  private StageDataCheck() {}

  public static List<String> problems(
      final Map<Identifier, VampireStage> stages, final Map<Identifier, Skill> vampireSkills) {
    final List<String> problems = new ArrayList<>();
    if (stages.isEmpty()) {
      problems.add("there are no vampire stages");
    } else if (stages.values().stream().allMatch(stage -> stage.weight() == 0)) {
      problems.add("no vampire stage has any weight");
    }
    new TreeMap<>(stages)
        .forEach(
            (id, stage) -> {
              new TreeMap<>(stage.skills().weights())
                  .keySet()
                  .forEach(
                      skill -> {
                        if (!vampireSkills.containsKey(skill)) {
                          problems.add(id + " weighs " + skill + ", which is not a vampire skill");
                        }
                      });
              if (stage.skills().count().getMax() >= vampireSkills.size()
                  && !vampireSkills.isEmpty()) {
                problems.add(id + " may want every vampire skill, which only the Eldest has");
              }
            });
    return problems;
  }
}
