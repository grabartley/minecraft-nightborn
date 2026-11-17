package com.grahambartley.nightborn.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.age.AgeLadder;
import com.grahambartley.nightborn.age.AgeProgress;
import com.grahambartley.nightborn.mob.stage.StageAge;
import com.grahambartley.nightborn.mob.stage.StageBody;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.mob.stage.StageDeed;
import com.grahambartley.nightborn.mob.stage.StageLook;
import com.grahambartley.nightborn.mob.stage.StageSkills;
import com.grahambartley.nightborn.mob.stage.VampireStage;
import java.util.Map;
import java.util.Optional;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StageCommandTest {

  static VampireStage stage(final int rank, final boolean deed) {
    final Identifier id = Identifier.of("nightborn", "deed/" + rank);
    return new VampireStage(
        rank,
        1,
        StageBody.SAME,
        StageSkills.NONE,
        StageCombat.PLAIN,
        StageLook.PLAIN,
        Optional.empty(),
        Optional.of(
            new StageAge(
                Text.literal("stage " + rank),
                deed
                    ? Optional.of(
                        new StageDeed(
                            StageDeed.Act.DRAIN,
                            TagKey.of(RegistryKeys.ENTITY_TYPE, id),
                            Text.literal("deed " + rank)))
                    : Optional.empty())));
  }

  static final AgeLadder LADDER =
      AgeLadder.of(
          Map.of(
              Identifier.of("nightborn", "a"), stage(0, false),
              Identifier.of("nightborn", "b"), stage(1, true),
              Identifier.of("nightborn", "c"), stage(2, true),
              Identifier.of("nightborn", "d"), stage(3, true)));

  @ParameterizedTest(name = "deeds {0} -> {1}")
  @CsvSource({"0, 0/3", "2, 1/3", "6, 2/3", "14, 3/3", "1, 0/3", "8, 1/3"})
  @DisplayName("the stage line counts deeds done out of the deeds the ladder asks for")
  void countsDeeds(final int deeds, final String shown) {
    assertEquals(shown, StageCommand.deeds(LADDER, new AgeProgress(0, 0, 0, deeds)));
  }
}
