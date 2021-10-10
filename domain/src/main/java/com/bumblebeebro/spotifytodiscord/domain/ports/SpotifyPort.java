package com.bumblebeebro.spotifytodiscord.domain.ports;

import com.bumblebeebro.spotifytodiscord.domain.model.Playlist;

public interface SpotifyPort {

  public Playlist getPlaylist(String playListId);
}
