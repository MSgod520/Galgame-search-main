package com.galgame.search.service;

import com.galgame.search.model.Game;
import com.galgame.search.model.Resource;
import java.io.IOException;
import java.util.List;

public interface GameSource {
    /**
     * Search for games by keyword.
     */
    List<Game> searchGame(String keyword) throws Exception;

    /**
     * Get download resources for a specific game ID.
     */
    List<Resource> getDownloads(String id) throws Exception;

    /**
     * Get the name of this source.
     */
    String getSourceName();
}
