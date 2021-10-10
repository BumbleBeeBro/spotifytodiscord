package com.bumblebeebro.spotifytodiscord.adapterspotify.service;

import com.bumblebeebro.spotifytodiscord.domain.model.Playlist;
import com.bumblebeebro.spotifytodiscord.domain.model.Track;
import com.bumblebeebro.spotifytodiscord.domain.ports.SpotifyPort;
import com.wrapper.spotify.SpotifyApi;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SpotifyService implements SpotifyPort {

  public static final String PLAYLIST_URL_PREFIX = "https://open.spotify.com/playlist/";
  public static final String PLAYLIST_URL_SUFFIX = "?si";

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
            .limit(50)
            .collect(Collectors.toList());

    return Playlist.builder()
        .owner(playlist.getOwner().getDisplayName())
        .name(playlist.getName())
        .tracks(tracks)
        .build();
  }

  public Playlist getPlaylistByUrl(String url) {
    return this.getPlaylist(this.extractPlaylistId(url));
  }

  private String extractPlaylistId(String url) {
    if (!url.startsWith(PLAYLIST_URL_PREFIX)) {
      throw new IllegalArgumentException("Provided URL is not valid");
    }

    return StringUtils.substringBetween(url, PLAYLIST_URL_PREFIX, PLAYLIST_URL_SUFFIX);
  }
}
