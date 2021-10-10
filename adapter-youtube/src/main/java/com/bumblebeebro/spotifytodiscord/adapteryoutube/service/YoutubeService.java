package com.bumblebeebro.spotifytodiscord.adapteryoutube.service;

import com.bumblebeebro.spotifytodiscord.domain.model.YoutubeResult;
import com.bumblebeebro.spotifytodiscord.domain.ports.YoutubePort;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class YoutubeService implements YoutubePort {

  private final List<String> developerKeys;

  private int keyIndex;

  private final int maxIndex;

  private Calendar quotaDate;

  private static final String APPLICATION_NAME = "spotifytodiscord";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private final YouTube youTube;

  @SneakyThrows
  public YoutubeService(@Value("${googletokens}") List<String> developerKeys) {

    this.developerKeys = developerKeys;

    maxIndex = developerKeys.size();

    this.youTube = this.getService();
  }

  @SneakyThrows
  @Override
  public Optional<YoutubeResult> getFirstSearchResults(String searchString) {
    val request = this.youTube.search().list(List.of("snippet"));

    SearchListResponse response = null;

    this.resetKeyIndexIfQuotaIsRenewed();

    try {
      response =
          request
              .setKey(developerKeys.get(keyIndex))
              .setMaxResults(1L)
              .setQ(searchString)
              .setType(List.of("video"))
              .execute();
    } catch (GoogleJsonResponseException e) {
      if (e.getDetails().getErrors().get(0).getDomain().equals("youtube.quota")) {
        log.warn("Using next key");
      }

      if (this.useNextKeyAndSetQuotaDate()) {
        response =
            request
                .setKey(developerKeys.get(keyIndex))
                .setMaxResults(1L)
                .setQ(searchString)
                .setType(List.of("video"))
                .execute();
      } else throw e;
    }

    return response.getItems().stream().findFirst().map(YoutubeResult::of);
  }

  private YouTube getService() throws GeneralSecurityException, IOException {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  private void resetKeyIndexIfQuotaIsRenewed() {
    if (quotaDate != null) {
      quotaDate.add(Calendar.HOUR_OF_DAY, 24);

      val currentDate = Calendar.getInstance();
      currentDate.setTime(new Date());
      if (quotaDate.compareTo(currentDate) < 0) {
        keyIndex = 0;
      }
    }
  }

  private boolean useNextKeyAndSetQuotaDate() {
    if (keyIndex == 0) {
      val date = Calendar.getInstance();
      date.setTime(new Date());
      quotaDate = date;
    }

    if (keyIndex < maxIndex) {
      keyIndex++;
      return true;
    } else {
      return false;
    }
  }
}
