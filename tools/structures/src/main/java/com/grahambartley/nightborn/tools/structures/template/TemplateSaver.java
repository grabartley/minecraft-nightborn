package com.grahambartley.nightborn.tools.structures.template;

import com.grahambartley.nightborn.tools.structures.files.OutputTree;
import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TemplateSaver {

  public static final int DATA_VERSION = 3955;

  private TemplateSaver() {}

  public static void save(final Template template, final OutputTree out) {
    Connections.connect(template);
    out.nbt("structure/" + template.path() + ".nbt", rootTag(template));
  }

  static Map<String, Object> rootTag(final Template template) {
    final List<Map<String, Object>> palette = new ArrayList<>();
    final Map<String, Integer> ids = new HashMap<>();
    final List<Map<String, Object>> blocks = new ArrayList<>();
    for (final Pos pos : template.cells().keySet().stream().sorted(Pos.SAVE_ORDER).toList()) {
      final String state = template.cells().get(pos);
      if (!ids.containsKey(state)) {
        ids.put(state, palette.size());
        final BlockState.Parsed parsed = BlockState.parse(state);
        final Map<String, Object> entry = Nbt.compound("Name", parsed.block());
        if (!parsed.properties().isEmpty()) {
          entry.put("Properties", new LinkedHashMap<String, Object>(parsed.properties()));
        }
        palette.add(entry);
      }
      final Map<String, Object> block =
          Nbt.compound("pos", NbtList.ints(pos.x(), pos.y(), pos.z()), "state", ids.get(state));
      final Map<String, Object> data = template.blockData().get(pos);
      if (data != null) {
        block.put("nbt", data);
      }
      blocks.add(block);
    }
    return Nbt.compound(
        "DataVersion", DATA_VERSION,
        "size", NbtList.ints(template.sizeX(), template.sizeY(), template.sizeZ()),
        "palette", NbtList.compounds(palette),
        "blocks", NbtList.compounds(blocks),
        "entities", NbtList.compounds(template.entities()));
  }
}
