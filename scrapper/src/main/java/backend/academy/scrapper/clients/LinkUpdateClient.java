package backend.academy.scrapper.clients;

import backend.academy.scrapper.controller.dto.UpdateInfo;

public interface LinkUpdateClient {
    UpdateInfo getUpdateInfo(String url);
}
