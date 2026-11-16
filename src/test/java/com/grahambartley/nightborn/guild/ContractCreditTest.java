package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ContractCreditTest {

  private static final RegistryKey<LootTable> REWARD =
      RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("nightborn", "contract/test"));
  private static final HunterContract STAKE_TWO =
      new HunterContract(Text.literal("two"), ContractGoal.STAKE_VAMPIRES, 2, 0, REWARD);
  private static final HunterContract ELDER =
      new HunterContract(Text.literal("elder"), ContractGoal.STAKE_VAMPIRES, 1, 2, REWARD);
  private static final HunterContract CLEAR =
      new HunterContract(Text.literal("clear"), ContractGoal.CLEAR_HOUSEHOLD, 1, 0, REWARD);
  private static final HunterContract.Stake FLEDGLING = new HunterContract.Stake(0, false);
  private static final HunterContract.Stake LAST_ELDER = new HunterContract.Stake(2, true);

  private static Optional<ContractCredit.Held> held(final HunterContract contract, final int done) {
    return Optional.of(new ContractCredit.Held(contract, done));
  }

  static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of("nothing held", List.of(), FLEDGLING, OptionalInt.empty()),
        Arguments.of(
            "the first unfinished contract",
            List.of(Optional.empty(), held(STAKE_TWO, 0), held(STAKE_TWO, 0)),
            FLEDGLING,
            OptionalInt.of(1)),
        Arguments.of(
            "skips a finished contract",
            List.of(held(STAKE_TWO, 2), held(STAKE_TWO, 1)),
            FLEDGLING,
            OptionalInt.of(1)),
        Arguments.of(
            "skips a contract the stake does not meet",
            List.of(held(ELDER, 0), held(CLEAR, 0), held(STAKE_TWO, 0)),
            FLEDGLING,
            OptionalInt.of(2)),
        Arguments.of(
            "an elder who was the last of a household meets every goal",
            List.of(held(ELDER, 0), held(CLEAR, 0)),
            LAST_ELDER,
            OptionalInt.of(0)),
        Arguments.of(
            "only finished contracts",
            List.of(held(ELDER, 1), held(STAKE_TWO, 2)),
            LAST_ELDER,
            OptionalInt.empty()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("cases")
  void oneStakeAdvancesAtMostOneContract(
      final String name,
      final List<Optional<ContractCredit.Held>> inventory,
      final HunterContract.Stake stake,
      final OptionalInt slot) {
    assertEquals(slot, ContractCredit.slotToAdvance(inventory, stake));
  }
}
