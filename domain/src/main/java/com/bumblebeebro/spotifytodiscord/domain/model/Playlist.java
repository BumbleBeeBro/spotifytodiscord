package com.bumblebeebro.spotifytodiscord.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Playlist {
  String owner;
  String name;
  List<Track> tracks;
}
