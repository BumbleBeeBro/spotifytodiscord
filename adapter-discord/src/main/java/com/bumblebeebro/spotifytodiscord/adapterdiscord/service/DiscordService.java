package com.bumblebeebro.spotifytodiscord.adapterdiscord.service;

import com.bumblebeebro.spotifytodiscord.domain.model.Track;
import com.bumblebeebro.spotifytodiscord.domain.model.YoutubeResult;
import com.bumblebeebro.spotifytodiscord.domain.ports.DiscordPort;
import com.bumblebeebro.spotifytodiscord.domain.ports.SpotifyPort;
import com.bumblebeebro.spotifytodiscord.domain.ports.YoutubePort;
import com.google.common.collect.Lists;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
public class DiscordService implements DiscordPort {

  public static String COMMAND_PREFIX = ":playlist";
  public static String MUSICBOT_PREFIX = "!play";
  public static String MUSICBOT_MAKEPLAYLIST_COMMAND = "!playlist make";
  public static String MUSICBOT_APPENDPLAYLIST_COMMAND = "!playlist append";

  private final DiscordClient discordClient;

  private final SpotifyPort spotifyPort;

  private final YoutubePort youtubePort;

  @Autowired
  public DiscordService(
      SpotifyPort spotifyPort,
      YoutubePort youtubePort,
      @Value("${discordtoken}") String discordToken) {
    this.spotifyPort = spotifyPort;
    this.youtubePort = youtubePort;
    this.discordClient = DiscordClient.create(discordToken);
  }

  @Override
  public void startListeningToMessages() {
    val login =
        discordClient.withGateway(
            (GatewayDiscordClient gateway) ->
                gateway.on(
                    MessageCreateEvent.class,
                    event -> {
                      val message = event.getMessage();

                      if (!message.getContent().startsWith(COMMAND_PREFIX)) {
                        return Mono.empty();
                      }

                      log.info("starting lookup");

                      this.writeMessage(message, "starting lookup");

                      try {
                        val messageContent = message.getContent();
                        val playListid =
                            messageContent.substring(messageContent.indexOf(' ') + 1).trim();
                        val playlist = spotifyPort.getPlaylistByUrl(playListid);

                        val parsedPlaylistName = playlist.getName().replace(' ', '_');

                        // message to create new playlist
                        this.writeMessage(
                            message, MUSICBOT_MAKEPLAYLIST_COMMAND + " " + parsedPlaylistName);

                        this.writeAppendMessageBatch(
                            message, playlist.getTracks(), parsedPlaylistName);

                      } catch (Exception e) {
                        log.error("transformation failed", e);
                        this.writeMessage(message, "transformation failed: " + e.getMessage());
                      }
                      return Mono.empty();
                    }));

    login.block();
  }

  private List<String> getPlaylistAsYoutubeLinks(List<Track> tracks) {

    return tracks.stream()
        .map(track -> youtubePort.getFirstSearchResults(track.asSearchString()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(YoutubeResult::getVideoUrl)
        .collect(Collectors.toList());
  }

  private void writeMessage(Message message, String content) {
    message.getChannel().flatMap(messageChannel -> messageChannel.createMessage(content)).block();
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
