package com.grahambartley.nightborn.client.state;

import com.grahambartley.nightborn.config.ConfigFile;
import com.grahambartley.nightborn.config.ConfigPaths;
import java.nio.file.Path;
import java.time.Clock;

public final class ClientStateService {

  private static volatile ClientState current = ClientState.DEFAULTS;
  private static volatile ConfigFile<ClientState> file;

  private ClientStateService() {}

  public static void load(final Path configDirectory) {
    file =
        new ConfigFile<>(
            ConfigPaths.clientState(configDirectory), ClientStateJson.FORMAT, Clock.systemUTC());
    current = file.loadOrCreate();
  }

  public static ClientState current() {
    return current;
  }

  public static boolean update(final ClientState state) {
    if (state.equals(current)) {
      return false;
    }
    current = state;
    return file != null && file.save(state);
  }
}
