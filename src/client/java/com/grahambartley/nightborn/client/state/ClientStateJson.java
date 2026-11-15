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

  public static final ConfigFormat<ClientState> FORMAT =
      new ConfigFormat<>() {
        @Override
        public ClientState defaults() {
          return ClientState.DEFAULTS;
        }

        @Override
        public ConfigFormat.Read<ClientState> read(final JsonObject root) {
          return ClientStateJson.read(root);
        }

        @Override
        public JsonObject write(final ClientState state) {
          return ClientStateJson.write(state);
        }
      };

  private static final String KNOWN =
      Arrays.stream(GoreLevel.values()).map(GoreLevel::id).collect(Collectors.joining(", "));

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
      problems.add(GORE + " is not one of " + KNOWN + ", using " + fallback.id());
    }
    return level.orElse(fallback);
  }
}
