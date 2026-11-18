package com.grahambartley.nightborn.house;

import static com.grahambartley.nightborn.house.HouseFixtures.LORD;
import static com.grahambartley.nightborn.house.HouseFixtures.NPC;
import static com.grahambartley.nightborn.house.HouseFixtures.PLAYER;
import static com.grahambartley.nightborn.house.HouseFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HousesTest {

  @ParameterizedTest(name = "{0} houses")
  @ValueSource(ints = {0, 1, 3})
  @DisplayName("every house survives a save and load, with its members")
  void roundTrip(final int count) {
    final Houses houses = new Houses();
    for (int i = 0; i < count; i++) {
      houses.put(House.founded(new UUID(0, i + 10), at(i, 64, i)).withPlayer(PLAYER).withNpc(NPC));
    }
    final Houses loaded = Houses.fromNbt(houses.writeNbt(new NbtCompound(), null), null);
    assertEquals(houses.all(), loaded.all());
  }

  @ParameterizedTest(name = "lord \"{0}\"")
  @ValueSource(strings = {"castle", "", "{}"})
  @DisplayName("a damaged house is skipped and the rest still load")
  void skipsDamaged(final String damage) {
    final Houses houses = new Houses();
    houses.put(House.founded(LORD, at(1, 64, 1)));
    final NbtCompound nbt = houses.writeNbt(new NbtCompound(), null);
    final NbtList list = nbt.getList(Houses.HOUSES_KEY, NbtCompound.COMPOUND_TYPE);
    final NbtCompound broken = new NbtCompound();
    broken.put("lord", NbtString.of(damage));
    list.add(broken);
    assertEquals(
        List.of(House.founded(LORD, at(1, 64, 1))), List.copyOf(Houses.fromNbt(nbt, null).all()));
  }

  @ParameterizedTest(name = "{index}")
  @ValueSource(booleans = {true, false})
  @DisplayName("saving the same house again leaves the state clean")
  void dirty(final boolean changed) {
    final Houses houses = new Houses();
    final House house = House.founded(LORD, at(1, 64, 1));
    houses.put(house);
    houses.setDirty(false);
    houses.put(changed ? house.withNpc(NPC) : house);
    assertEquals(changed, houses.isDirty());
    assertEquals(Optional.of(changed ? house.withNpc(NPC) : house), houses.ledBy(LORD));
  }
}
