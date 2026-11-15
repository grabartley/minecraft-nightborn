package com.grahambartley.nightborn.client.state;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grahambartley.nightborn.config.ConfigFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ClientStateJson {

  static final String GORE = "gore";

  public static final ConfigFormat<ClientState> FORMAT =
      new ConfigFormat<>(ClientState.DEFAULTS, ClientStateJson::read, ClientStateJson::write);

  private ClientStateJson() {}

  public static ConfigFormat.Read<ClientState> read(final JsonObject root) {
    final List<String> problems = new ArrayList<>();
    final GoreLevel gore = readGore(root.get(GORE), problems);
    return new ConfigFormat.Read<>(new ClientState(gore), problems);
  }

  public static JsonObject write(final ClientState state) {
    final JsonObject root = new JsonObject();
    root.addProperty(GORE, state.gore().id());
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
      problems.add(GORE + " is not full, reduced or off, using " + fallback.id());
    }
    return level.orElse(fallback);
  }
}
