package com.bumblebeebro.spotifytodiscord.domain.model;

import com.wrapper.spotify.enums.ModelObjectType;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Track {

  String name;

  List<String> artists;

  public static Track of(PlaylistTrack playlistTrack) {

    val name = playlistTrack.getTrack().getName();

    val playlistItem = playlistTrack.getTrack();

    List<String> artists;

    if (playlistItem.getType().equals(ModelObjectType.TRACK)) {
      artists =
          Arrays.stream(
                  ((com.wrapper.spotify.model_objects.specification.Track) playlistItem)
                      .getArtists())
              .map(ArtistSimplified::getName)
              .collect(Collectors.toList());
    } else {
      throw new IllegalArgumentException("non Track playlist Items are not allowed");
    }

    return Track.builder().name(name).artists(artists).build();
  }

  public String asSearchString() {
    val artistsAsString = artists.stream().reduce((s1, s2) -> s1 + ", " + s2);
    return artistsAsString.isPresent() ? name + " " + artistsAsString.get() : name;
  }
}
