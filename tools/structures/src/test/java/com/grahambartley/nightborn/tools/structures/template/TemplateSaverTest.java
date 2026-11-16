package com.grahambartley.nightborn.tools.structures.template;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.tools.structures.files.OutputTree;
import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtFiles;
import com.grahambartley.nightborn.tools.structures.nbt.NbtList;
import com.grahambartley.nightborn.tools.structures.nbt.NbtReader;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TemplateSaverTest {

  @TempDir Path root;

  @ParameterizedTest(name = "{0} cells of {1} states")
  @CsvSource({"stone stone stone, 1", "stone dirt stone, 2", "stone dirt grass_block, 3"})
  void sharesOnePaletteEntryPerStateInSaveOrder(final String states, final int palette) {
    final String[] cells = states.split(" ");
    final Template t = new Template("p", 1, cells.length, 1);
    for (int y = 0; y < cells.length; y++) {
      t.set(0, y, 0, cells[y]);
    }
    final Map<String, Object> tag = TemplateSaver.rootTag(t);
    assertEquals(palette, ((NbtList) tag.get("palette")).items().size());
    final NbtList blocks = (NbtList) tag.get("blocks");
    for (int y = 0; y < cells.length; y++) {
      assertEquals(NbtList.ints(0, y, 0), ((Map<?, ?>) blocks.items().get(y)).get("pos"));
    }
    assertEquals(Map.of("Name", "minecraft:stone"), ((NbtList) tag.get("palette")).items().get(0));
  }

  @ParameterizedTest(name = "{0}[{1}={2}]")
  @CsvSource({"chain, axis, y", "lantern, hanging, true", "campfire, lit, false"})
  void writesPropertiesAndTheDataVersion(
      final String block, final String property, final String value) {
    final Template t = new Template("p", 1, 1, 1);
    t.set(0, 0, 0, BlockState.of(block, property, value));
    final Map<String, Object> tag = TemplateSaver.rootTag(t);
    assertEquals(TemplateSaver.DATA_VERSION, tag.get("DataVersion"));
    assertEquals(
        Map.of("Name", "minecraft:" + block, "Properties", Map.of(property, value)),
        ((NbtList) tag.get("palette")).items().get(0));
  }

  @Test
  void keepsBlockDataAndEntities() {
    final Template t = new Template("p", 1, 2, 1);
    t.set(0, 0, 0, "chest", Nbt.compound("id", "minecraft:chest"));
    t.entity(0, 1, 0, Nbt.compound("id", "minecraft:pig"));
    final Map<String, Object> tag = TemplateSaver.rootTag(t);
    assertEquals(
        Nbt.compound("id", "minecraft:chest"),
        ((Map<?, ?>) ((NbtList) tag.get("blocks")).items().get(0)).get("nbt"));
    assertEquals(1, ((NbtList) tag.get("entities")).items().size());
  }

  @ParameterizedTest(name = "a fence beside {0} connects east: {1}")
  @CsvSource({"stone, true", "air, false", "oak_fence, true"})
  void savesConnectedStatesUnderTheStructureFolder(final String neighbour, final String east) {
    final Template t = new Template("hunter_camp/x/piece", 2, 1, 1);
    t.set(0, 0, 0, "oak_fence");
    t.set(1, 0, 0, neighbour);
    TemplateSaver.save(t, new OutputTree(root));
    final Map<String, Object> read =
        NbtReader.read(NbtFiles.payload(root.resolve("structure/hunter_camp/x/piece.nbt")));
    final Map<?, ?> fence = (Map<?, ?>) ((NbtList) read.get("palette")).items().get(0);
    assertEquals(east, ((Map<?, ?>) fence.get("Properties")).get("east"));
  }
}
