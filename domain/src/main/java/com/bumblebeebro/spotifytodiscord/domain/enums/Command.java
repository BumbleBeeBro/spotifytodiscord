package com.bumblebeebro.spotifytodiscord.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum Command {
  COMMAND_PLAYLIST_PREFIX(":playlist"),
  COMMAND_PLAYLISTID_PREFIX(":playlistid");

  private static final Map<String, Command> BY_CODE = new HashMap<>();

  static {
    for (Command e : values()) {
      BY_CODE.put(e.code, e);
    }
  }

  private final String code;

  public static Command of(String code) {
    return BY_CODE.get(code);
  }
}
