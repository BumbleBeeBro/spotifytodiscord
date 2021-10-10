package com.bumblebeebro.spotifytodiscord.domain.model;

import com.google.api.services.youtube.model.SearchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class YoutubeResult {

  public static final String BASE_URL = "https://www.youtube.com/watch?v=";

  String videoId;

  String kind;

  String title;

  public static YoutubeResult of(SearchResult searchResult) {
    return YoutubeResult.builder()
        .videoId(searchResult.getId().getVideoId())
        .kind(searchResult.getId().getKind())
        .title(searchResult.getSnippet().getTitle())
        .build();
  }

  public String getVideoUrl() {
    if (kind != null && videoId != null) {
      return BASE_URL + videoId;
    } else {
      return null;
    }
  }
}
