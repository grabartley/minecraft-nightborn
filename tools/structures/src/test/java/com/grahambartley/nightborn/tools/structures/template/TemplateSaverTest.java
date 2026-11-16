package com.grahambartley.nightborn.tools.structures.template;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.tools.structures.files.OutputTree;
import com.grahambartley.nightborn.tools.structures.nbt.NbtFiles;
import com.grahambartley.nightborn.tools.structures.nbt.NbtList;
import com.grahambartley.nightborn.tools.structures.nbt.NbtReader;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemplateSaverTest {

  @TempDir Path root;

  @Test
  void sharesOnePaletteEntryPerStateInSaveOrder() {
    final Template t = new Template("p", 2, 2, 1);
    t.set(0, 1, 0, "stone");
    t.set(1, 0, 0, "dirt");
    t.set(0, 0, 0, "stone");
    final Map<String, Object> tag = TemplateSaver.rootTag(t);
    final NbtList palette = (NbtList) tag.get("palette");
    assertEquals(2, palette.items().size());
    assertEquals(Map.of("Name", "minecraft:stone"), palette.items().get(0));
    final NbtList blocks = (NbtList) tag.get("blocks");
    assertEquals(NbtList.ints(0, 0, 0), ((Map<?, ?>) blocks.items().get(0)).get("pos"));
    assertEquals(NbtList.ints(0, 1, 0), ((Map<?, ?>) blocks.items().get(2)).get("pos"));
  }

  @Test
  void writesPropertiesAndTheDataVersion() {
    final Template t = new Template("p", 1, 1, 1);
    t.set(0, 0, 0, BlockState.of("chain", "axis", "y"));
    final Map<String, Object> tag = TemplateSaver.rootTag(t);
    assertEquals(TemplateSaver.DATA_VERSION, tag.get("DataVersion"));
    assertEquals(
        Map.of("Name", "minecraft:chain", "Properties", Map.of("axis", "y")),
        ((NbtList) tag.get("palette")).items().get(0));
  }

  @Test
  void savesConnectedStatesUnderTheStructureFolder() {
    final Template t = new Template("hunter_camp/x/piece", 2, 1, 1);
    t.set(0, 0, 0, "oak_fence");
    t.set(1, 0, 0, "stone");
    TemplateSaver.save(t, new OutputTree(root));
    final Map<String, Object> read =
        NbtReader.read(NbtFiles.payload(root.resolve("structure/hunter_camp/x/piece.nbt")));
    final Map<?, ?> fence = (Map<?, ?>) ((NbtList) read.get("palette")).items().get(0);
    assertEquals("true", ((Map<?, ?>) fence.get("Properties")).get("east"));
  }
}
