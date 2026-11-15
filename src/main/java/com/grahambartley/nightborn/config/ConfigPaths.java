package com.grahambartley.nightborn.config;

import java.nio.file.Path;

public final class ConfigPaths {

  static final String DIRECTORY = "nightborn";
  static final String SERVER_FILE = "server-config.json";

  private ConfigPaths() {}

  public static Path serverConfig(final Path worldRoot) {
    return worldRoot.resolve(DIRECTORY).resolve(SERVER_FILE);
  }
}
