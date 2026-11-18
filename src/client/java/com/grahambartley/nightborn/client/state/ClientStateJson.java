package com.grahambartley.nightborn.client.state;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grahambartley.nightborn.config.ConfigFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ClientStateJson {

  private static final String GORE = "gore";
  private static final String SKIP_CEREMONY_CAMERA = "skipCeremonyCamera";

  public static final ConfigFormat<ClientState> FORMAT =
      ConfigFormat.of(ClientState.DEFAULTS, ClientStateJson::read, ClientStateJson::write);

  private static final String KNOWN =
      Arrays.stream(GoreLevel.values()).map(GoreLevel::id).collect(Collectors.joining(", "));

  private ClientStateJson() {}

  public static ConfigFormat.Read<ClientState> read(final JsonObject root) {
    final List<String> problems = new ArrayList<>();
    final GoreLevel gore = readGore(root.get(GORE), problems);
    final boolean skip = readSkip(root.get(SKIP_CEREMONY_CAMERA), problems);
    return new ConfigFormat.Read<>(new ClientState(gore, skip), problems);
  }

  public static JsonObject write(final ClientState state) {
    final JsonObject root = new JsonObject();
    root.addProperty(GORE, state.gore().id());
    root.addProperty(SKIP_CEREMONY_CAMERA, state.skipCeremonyCamera());
    return root;
  }

  private static GoreLevel readGore(final JsonElement element, final List<String> problems) {
    final GoreLevel fallback = ClientState.DEFAULTS.gore();
    if (element == null) {
      return fallback;
    }
    final Optional<GoreLevel> level =
        element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
            ? GoreLevel.byId(element.getAsString())
            : Optional.empty();
    if (level.isEmpty()) {
      problems.add(GORE + " is not one of " + KNOWN + ", using " + fallback.id());
    }
    return level.orElse(fallback);
  }

  private static boolean readSkip(final JsonElement element, final List<String> problems) {
    final boolean fallback = ClientState.DEFAULTS.skipCeremonyCamera();
    if (element == null) {
      return fallback;
    }
    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
      return element.getAsBoolean();
    }
    problems.add(SKIP_CEREMONY_CAMERA + " is not true or false, using " + fallback);
    return fallback;
  }
}
