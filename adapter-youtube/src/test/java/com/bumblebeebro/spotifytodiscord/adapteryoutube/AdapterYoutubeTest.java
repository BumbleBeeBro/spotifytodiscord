package com.bumblebeebro.spotifytodiscord.adapteryoutube;

import com.bumblebeebro.spotifytodiscord.adapteryoutube.service.YoutubeService;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class AdapterYoutubeTest {

  private static final String TEST_QUERY = "weiß der geier oder weiß er nicht";

  @Test
  public void canRetrieveResult(@Value("${googletokens}") List<String> developerKeys) {
    // given
    val youtubeService = new YoutubeService(developerKeys);

    val result = youtubeService.getFirstSearchResults(TEST_QUERY);

    Assertions.assertThat(result).isPresent();

    Assertions.assertThat(result.get().getVideoId()).isEqualTo("gQlkbGh1WwA");
  }
}
