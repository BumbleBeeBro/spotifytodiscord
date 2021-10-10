package com.bumblebeebro.spotifytodiscord.application.runner;

import com.bumblebeebro.spotifytodiscord.domain.ports.DiscordPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DiscordRunner implements ApplicationRunner {

  private final DiscordPort discordPort;

  @Autowired
  public DiscordRunner(DiscordPort discordPort) {
    this.discordPort = discordPort;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    discordPort.startListeningToMessages();
  }
}
