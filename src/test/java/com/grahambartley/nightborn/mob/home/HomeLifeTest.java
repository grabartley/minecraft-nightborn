package com.grahambartley.nightborn.mob.home;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HomeLifeTest {

  static Stream<Arguments> homes() {
    return Stream.of(
        Arguments.of(Optional.of(new BlockPos(120, 40, -900))),
        Arguments.of(Optional.of(new BlockPos(-3, -58, 7))),
        Arguments.of(Optional.empty()));
  }

  @ParameterizedTest(name = "home {0}")
  @MethodSource("homes")
  @DisplayName("a vampire's home survives a save and load, and a homeless vampire stays homeless")
  void roundTrip(final Optional<BlockPos> home) {
    final HomeLife life = new HomeLife();
    home.ifPresent(life::settle);
    final NbtCompound nbt = new NbtCompound();
    life.write(nbt);
    final HomeLife loaded = new HomeLife();
    loaded.read(nbt);
    assertEquals(home, loaded.home());
    assertEquals(Optional.empty(), loaded.coffin());
  }

  static Stream<Arguments> oldSaves() {
    final NbtCompound wrongType = new NbtCompound();
    wrongType.putString(HomeLife.HOME_KEY, "castle");
    wrongType.putString(HomeLife.COFFIN_KEY, "coffin");
    return Stream.of(
        Arguments.of("nothing saved", new NbtCompound()), Arguments.of("text", wrongType));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("oldSaves")
  @DisplayName("a save from before homes loads as a homeless vampire")
  void oldSave(final String name, final NbtCompound nbt) {
    final HomeLife loaded = new HomeLife();
    loaded.read(nbt);
    assertEquals(Optional.empty(), loaded.home());
    assertEquals(Optional.empty(), loaded.coffin());
  }
}
