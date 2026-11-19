package com.grahambartley.nightborn.seeker;

public record Marker(boolean byHunter, boolean byAlly, double own, double allied) {

  public double share() {
    return MarkShare.of(byHunter, byAlly, own, allied);
  }
}
