package com.bumblebeebro.spotifytodiscord.domain.ports;

import com.bumblebeebro.spotifytodiscord.domain.model.YoutubeResult;

import java.util.Optional;

public interface YoutubePort {

  Optional<YoutubeResult> getFirstSearchResults(String searchString);
}
