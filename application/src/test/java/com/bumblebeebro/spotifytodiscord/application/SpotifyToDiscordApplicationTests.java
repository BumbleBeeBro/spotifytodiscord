package com.bumblebeebro.spotifytodiscord.application;

import com.bumblebeebro.spotifytodiscord.domain.ports.SpotifyPort;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

@SpringBootTest
class SpotifyToDiscordApplicationTests {

  private static final String TEST_PLAYLIST = "07HI54DDjXCaNTSKIkdLQz";

  @Autowired SpotifyPort spotifyPort;

  @Test
  void contextLoads() {}

  @Test
  void canRetrievePlaylist() {
    val tracks = spotifyPort.getPlaylist(TEST_PLAYLIST);

    Assertions.assertThat(tracks).isNotNull();
  }
}
