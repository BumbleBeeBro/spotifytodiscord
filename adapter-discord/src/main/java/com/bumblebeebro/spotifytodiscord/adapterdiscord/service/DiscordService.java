package com.bumblebeebro.spotifytodiscord.adapterdiscord.service;

import com.bumblebeebro.spotifytodiscord.domain.enums.Command;
import com.bumblebeebro.spotifytodiscord.domain.model.Playlist;
import com.bumblebeebro.spotifytodiscord.domain.model.Track;
import com.bumblebeebro.spotifytodiscord.domain.model.YoutubeResult;
import com.bumblebeebro.spotifytodiscord.domain.ports.FileSystemPort;
import com.bumblebeebro.spotifytodiscord.domain.ports.SpotifyPort;
import com.bumblebeebro.spotifytodiscord.domain.ports.YoutubePort;
import com.google.common.collect.Lists;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateFields;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class DiscordService {

  public static String MUSICBOT_MAKEPLAYLIST_COMMAND = "!playlist make";
  public static String MUSICBOT_APPENDPLAYLIST_COMMAND = "!playlist append";

  private final SpotifyPort spotifyPort;

  private final YoutubePort youtubePort;

  private final FileSystemPort fileSystemPort;

  public void handlePlaylistRequest(
      Message message, Command command, String input, @Nullable String option) {

    this.writeMessage(message, "starting lookup");

    try {
      Playlist playlist;

      if (command == Command.COMMAND_PLAYLIST_PREFIX) {
        playlist = spotifyPort.getPlaylistByUrl(input);
      } else if (command == Command.COMMAND_PLAYLISTID_PREFIX) {
        playlist = spotifyPort.getPlaylist(input);
      } else {
        return;
      }

      val parsedPlaylistName = playlist.getName().replace(' ', '_');

      if (option != null && option.equals("download")) {
        this.returnAsFile(message, playlist, parsedPlaylistName);

      } else if (option != null && option.equals("bot")) {
        this.toDisocrdMusicBot(message, playlist, parsedPlaylistName);
      } else {
        this.returnAsMusicBotCommands(message, playlist, parsedPlaylistName);
      }

    } catch (Exception e) {
      log.error("transformation failed", e);
      this.writeMessage(message, "transformation failed: " + e.getMessage());
    }
  }

  private void returnAsFile(Message message, Playlist playlist, String parsedPlaylistName)
      throws IOException {
    val links = this.getPlaylistAsYoutubeLinks(playlist.getTracks());

    val fileInput = links.stream().reduce((s1, s2) -> s1 + "\r\n" + s2);

    if (fileInput.isEmpty()) {
      this.writeMessage(message, "No youtube links found");
      return;
    }

    val file = fileSystemPort.saveToFile(fileInput.get(), parsedPlaylistName);

    this.writeMessage(message, "File with YouTube links of Playlist " + playlist.getName(), file);
  }

  private void toDisocrdMusicBot(Message message, Playlist playlist, String parsedPlaylistName)
      throws IOException {
    val links = this.getPlaylistAsYoutubeLinks(playlist.getTracks());

    val fileInput = links.stream().reduce((s1, s2) -> s1 + "\r\n" + s2);

    if (fileInput.isEmpty()) {
      this.writeMessage(message, "No youtube links found");
      return;
    }

    val file = fileSystemPort.saveToDisocrdMusicBotDir(fileInput.get(), parsedPlaylistName);

    this.writeMessage(
        message,
        "File with YouTube links for playlist "
            + playlist.getName()
            + " added to MusicBot Playlist Directory as "
            + file.getName());
  }

  private void returnAsMusicBotCommands(
      Message message, Playlist playlist, String parsedPlaylistName) {
    // message to create new playlist
    this.writeMessage(message, MUSICBOT_MAKEPLAYLIST_COMMAND + " " + parsedPlaylistName);

    //       messages to append music
    this.writeAppendMessageBatch(message, playlist.getTracks(), parsedPlaylistName);
  }

  private List<String> getPlaylistAsYoutubeLinks(List<Track> tracks) {

    return tracks.stream()
        .map(track -> youtubePort.getFirstSearchResults(track.asSearchString()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(YoutubeResult::getVideoUrl)
        .collect(Collectors.toList());
  }

  public void writeMessage(Message message, String content) {
    message.getChannel().flatMap(messageChannel -> messageChannel.createMessage(content)).block();
  }

  @SneakyThrows
  public void writeMessage(Message message, String content, File file) {
    val discordFile = MessageCreateFields.File.of(file.getName(), new FileInputStream(file));
    message
        .getChannel()
        .flatMap(messageChannel -> messageChannel.createMessage(content).withFiles(discordFile))
        .block();
  }

  private void writeAppendMessageBatch(
      Message message, List<Track> tracksLinks, String parsedPlaylistName) {

    for (List<String> stringBatch :
        Lists.partition(this.getPlaylistAsYoutubeLinks(tracksLinks), 20)) {
      // message to append to playlist
      var appendMessage =
          stringBatch.stream()
              .map(this::youtubeLinkToCopyableString)
              .reduce((s1, s2) -> s1 + " | " + s2);

      if (appendMessage.isEmpty()) {
        throw new IllegalArgumentException("Playlist is empty");
      } else {
        this.writeMessage(
            message,
            MUSICBOT_APPENDPLAYLIST_COMMAND + " " + parsedPlaylistName + " " + appendMessage.get());
      }
    }
  }

  private String youtubeLinkToCopyableString(String link) {
    return "<" + link + ">";
  }
}
