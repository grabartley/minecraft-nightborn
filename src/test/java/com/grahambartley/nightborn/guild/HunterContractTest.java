package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterContractTest {

  private static final RegistryKey<LootTable> REWARD =
      RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("nightborn", "contract/test"));

  @ParameterizedTest(name = "{0} needing stage {1}: rank {2}, cleared {3} counts {4}")
  @CsvSource({
    "STAKE_VAMPIRES, 0, 0, false, true",
    "STAKE_VAMPIRES, 2, 1, false, false",
    "STAKE_VAMPIRES, 2, 2, false, true",
    "STAKE_VAMPIRES, 2, 3, true, true",
    "CLEAR_HOUSEHOLD, 0, 0, false, false",
    "CLEAR_HOUSEHOLD, 0, 0, true, true",
    "CLEAR_HOUSEHOLD, 3, 2, true, false"
  })
  void countsAStakeThatMeetsItsGoalAndStage(
      final ContractGoal goal,
      final int minimumStage,
      final int rank,
      final boolean cleared,
      final boolean counts) {
    final HunterContract contract =
        new HunterContract(Text.literal("c"), goal, 1, minimumStage, REWARD);
    assertEquals(counts, contract.countedBy(new HunterContract.Stake(rank, cleared)));
  }

  @ParameterizedTest(name = "{1} of {0} done finishes: {2}")
  @CsvSource({"3, 0, false", "3, 2, false", "3, 3, true", "1, 1, true", "1, 5, true"})
  void isFinishedOnceEnoughAreDone(final int count, final int done, final boolean finished) {
    final HunterContract contract =
        new HunterContract(Text.literal("c"), ContractGoal.STAKE_VAMPIRES, count, 0, REWARD);
    assertEquals(finished, contract.finishedAt(done));
  }

  @Test
  void everyShippedContractDecodesAndHasItsRewardAndEnglishText() throws IOException {
    final Map<String, HunterContract> contracts =
        GuildDataFiles.load("hunter_contract", HunterContract.CODEC);
    assertEquals(3, contracts.size());
    final String english =
        Files.readString(
            GuildDataFiles.DATA.resolve("../../assets/nightborn/lang/en_us.json").normalize());
    contracts.forEach(
        (name, contract) -> {
          assertTrue(
              Files.exists(
                  GuildDataFiles.DATA.resolve(
                      "loot_table/" + contract.reward().getValue().getPath() + ".json")),
              name);
          assertTrue(english.contains("\"nightborn.contract." + name + "\""), name);
        });
  }
}
