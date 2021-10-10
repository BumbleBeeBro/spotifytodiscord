package com.bumblebeebro.spotifytodiscord.adapterspotify;

import com.bumblebeebro.spotifytodiscord.adapterspotify.service.SpotifyService;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

public class AdapterTest {

  private static final String TEST_PLAYLIST = "07HI54DDjXCaNTSKIkdLQz";

  @Test
  void canRetrievePlaylist(
      @Value("${spotifyclientsecret}") String clientSecret,
      @Value("${spotifyclientid}") String clientId) {
    // given
    val spotifyService = new SpotifyService(clientId, clientSecret);

    val tracks = spotifyService.getPlaylist(TEST_PLAYLIST);

    Assertions.assertThat(tracks).isNotNull();
  }
}
