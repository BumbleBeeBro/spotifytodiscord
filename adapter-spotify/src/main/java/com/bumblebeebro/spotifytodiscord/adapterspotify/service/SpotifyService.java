package com.bumblebeebro.spotifytodiscord.adapterspotify.service;

import com.bumblebeebro.spotifytodiscord.domain.model.Playlist;
import com.bumblebeebro.spotifytodiscord.domain.model.Track;
import com.bumblebeebro.spotifytodiscord.domain.ports.SpotifyPort;
import com.wrapper.spotify.SpotifyApi;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SpotifyService implements SpotifyPort {

  private final SpotifyApi spotifyApi;

  @SneakyThrows
  public SpotifyService(
      @Value("${spotifyclientsecret}") String clientSecret,
      @Value("${spotifyclientid}") String clientId) {

    spotifyApi = SpotifyApi.builder().setClientId(clientId).setClientSecret(clientSecret).build();

    val clientCredentials = spotifyApi.clientCredentials().build().execute();

    spotifyApi.setAccessToken(clientCredentials.getAccessToken());

    log.info("Expires in: {}", clientCredentials.getExpiresIn());
  }

  @SneakyThrows
  @Override
  public Playlist getPlaylist(String playListId) {
    val playlist = spotifyApi.getPlaylist(playListId).build().execute();

    log.info("Name: {}", playlist.getName());

    val tracks =
        Arrays.stream(playlist.getTracks().getItems())
            .map(Track::of)
            .limit(20)
            .collect(Collectors.toList());

    return Playlist.builder()
        .owner(playlist.getOwner().getDisplayName())
        .name(playlist.getName())
        .tracks(tracks)
        .build();
  }
}
