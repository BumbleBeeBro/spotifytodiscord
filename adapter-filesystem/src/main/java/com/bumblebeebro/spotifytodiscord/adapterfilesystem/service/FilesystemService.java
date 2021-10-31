package com.bumblebeebro.spotifytodiscord.adapterfilesystem.service;

import com.bumblebeebro.spotifytodiscord.domain.ports.FileSystemPort;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FilesystemService implements FileSystemPort {

  private static final String FILE_TYPE = ".txt";

  private final Path rootLocation;

  @SneakyThrows
  public FilesystemService() {
    rootLocation = Files.createTempDirectory("SpotifyToDiscord");
  }

  @Override
  public File saveToFile(String input, String name) throws IOException {
    val path = Files.createFile(rootLocation.resolve(name + FILE_TYPE));
    val writer = new BufferedWriter(new FileWriter(path.toFile()));

    writer.write(input);
    writer.close();

    return path.toFile();
  }
}
