package com.bumblebeebro.spotifytodiscord.adapterdiscord.service;

import com.bumblebeebro.spotifytodiscord.domain.enums.Command;
import com.bumblebeebro.spotifytodiscord.domain.ports.DiscordPort;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DiscordCommandLineServie implements DiscordPort {

  private final DiscordClient discordClient;

  private final DiscordService discordService;

  @Autowired
  public DiscordCommandLineServie(
      @Value("${discordtoken}") String discordToken, DiscordService discordService) {
    this.discordClient = DiscordClient.create(discordToken);
    this.discordService = discordService;
  }

  @Override
  public void startListeningToMessages() {
    val login =
        discordClient.withGateway(
            (GatewayDiscordClient gateway) ->
                gateway.on(
                    MessageCreateEvent.class,
                    event -> {
                      this.handleMessage(event.getMessage());
                      return Mono.empty();
                    }));
    login.block();
  }

  Command command;

  String input, option;

  public void handleMessage(Message message) {
    val content = message.getContent().split(" ");

    command = Command.of(content[0]);

    if (command == null) {
      return;
    }

    if (content.length < 2) {
      discordService.writeMessage(message, "Not a valid message");
      return;
    }

    input = content[1];

    if (content.length > 2) {
      option = content[2];
    }

    discordService.handlePlaylistRequest(message, command, input, option);
  }
}
