package com.bumblebeebro.spotifytodiscord.adapterspotify;

import com.bumblebeebro.spotifytodiscord.adapterspotify.service.SpotifyService;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AdapterTest {

  private static final String TEST_PLAYLIST = "07HI54DDjXCaNTSKIkdLQz";

  @Value("${spotifyclientsecret}")
  private String clientSecret;

  @Value("${spotifyclientid}")
  private String clientId;

  @Test
  @Ignore
  public void canRetrievePlaylist() {
    // given
    val spotifyService = new SpotifyService(clientId, clientSecret);

    val tracks = spotifyService.getPlaylist(TEST_PLAYLIST);

    Assertions.assertThat(tracks).isNotNull();
  }
}
