package com.bumblebeebro.spotifytodiscord.domain.ports;

import com.bumblebeebro.spotifytodiscord.domain.model.Playlist;

public interface SpotifyPort {

  Playlist getPlaylist(String playListId);

  Playlist getPlaylistByUrl(String url);
}
