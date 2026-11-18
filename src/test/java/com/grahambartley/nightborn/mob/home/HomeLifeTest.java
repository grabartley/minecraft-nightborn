package com.grahambartley.nightborn.mob.home;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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

  @ParameterizedTest(name = "coffin {0}, out on night {1}, decided night {2}")
  @CsvSource({"12, 4, 4", "-900, -1, 7", "0, -9223372036854775808, -9223372036854775808"})
  @DisplayName("a vampire's coffin and its night's plans survive a save and load")
  void keepsCoffinAndNight(final long coffin, final long outing, final long decided) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putLong(HomeLife.HOME_KEY, new BlockPos(3, 70, 3).asLong());
    nbt.putLong(HomeLife.COFFIN_KEY, new BlockPos((int) coffin, 70, 5).asLong());
    nbt.putLong(HomeLife.OUTING_KEY, outing);
    nbt.putLong(HomeLife.DECIDED_KEY, decided);
    final HomeLife life = new HomeLife();
    life.read(nbt);
    assertEquals(Optional.of(new BlockPos((int) coffin, 70, 5)), life.coffin());
    final NbtCompound saved = new NbtCompound();
    life.write(saved);
    assertEquals(nbt, saved);
  }

  @ParameterizedTest(name = "room at y {0} with radius {1}")
  @CsvSource({"69, 6", "-20, 3", "300, 12"})
  @DisplayName("the room a vampire keeps to survives a save and load beside its home")
  void keepsItsRoom(final int y, final int radius) {
    final HomeLife life = new HomeLife();
    final BlockPos home = new BlockPos(10, 48, 10);
    final BlockPos room = new BlockPos(10, y, 10);
    life.settle(home);
    life.keepTo(room, radius);
    final NbtCompound nbt = new NbtCompound();
    life.write(nbt);
    final HomeLife loaded = new HomeLife();
    loaded.read(nbt);
    assertEquals(Optional.of(home), loaded.home());
    assertEquals(Optional.of(room), loaded.room());
    assertEquals(radius, nbt.getInt(HomeLife.ROOM_RADIUS_KEY));
  }

  @ParameterizedTest(name = "home {0}")
  @MethodSource("homes")
  @DisplayName("a vampire that was never given a room has none")
  void noRoom(final Optional<BlockPos> home) {
    final HomeLife life = new HomeLife();
    home.ifPresent(life::settle);
    assertEquals(Optional.empty(), life.room());
  }

  @ParameterizedTest(name = "home {0}")
  @MethodSource("homes")
  @DisplayName("a vampire that wanders off forgets its home and its coffin")
  void wander(final Optional<BlockPos> home) {
    final HomeLife life = new HomeLife();
    home.ifPresent(life::settle);
    life.keepTo(new BlockPos(1, 2, 3), 4);
    life.wander();
    assertEquals(Optional.empty(), life.home());
    assertEquals(Optional.empty(), life.room());
    assertEquals(Optional.empty(), life.coffin());
  }

  @ParameterizedTest(name = "sent out on night {0}")
  @CsvSource({"0", "5", "-2", "1000"})
  @DisplayName("a vampire sent out keeps that night's outing through a save and load")
  void sendOut(final long night) {
    final HomeLife life = new HomeLife();
    life.sendOut(night);
    final NbtCompound nbt = new NbtCompound();
    life.write(nbt);
    assertEquals(night, nbt.getLong(HomeLife.OUTING_KEY));
    assertEquals(night, nbt.getLong(HomeLife.DECIDED_KEY));
  }
}
