package com.grahambartley.nightborn.guild;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class HunterRigClips {

  static final List<String> RIGS = List.of("hunter", "grand_inquisitor");

  private static final Path ANIMATIONS =
      Path.of("src/client/resources/assets/nightborn/animations/entity");

  private HunterRigClips() {}

  static JsonObject of(final String rig) throws IOException {
    return JsonParser.parseString(Files.readString(ANIMATIONS.resolve(rig + ".animation.json")))
        .getAsJsonObject()
        .getAsJsonObject("animations");
  }
}
