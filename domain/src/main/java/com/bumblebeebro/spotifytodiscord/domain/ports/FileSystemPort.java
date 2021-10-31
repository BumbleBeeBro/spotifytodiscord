package com.bumblebeebro.spotifytodiscord.domain.ports;

import java.io.File;
import java.io.IOException;

public interface FileSystemPort {

  File saveToFile(String input, String name) throws IOException;

  File saveToDisocrdMusicBotDir(String input, String name) throws IOException;
}
