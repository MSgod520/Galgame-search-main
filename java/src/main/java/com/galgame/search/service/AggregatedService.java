package com.galgame.search.service;

import com.galgame.search.model.Game;
import com.galgame.search.model.Resource;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AggregatedService {
    private final List<GameSource> sources;
    private final ExecutorService executor;

    public AggregatedService() {
        this.sources = new ArrayList<>();
        this.executor = Executors.newCachedThreadPool();
        
        // Register available sources
        this.sources.add(new TouchGalService());
        this.sources.add(new NyaaSource());
    }

    public void addSource(GameSource source) {
        this.sources.add(source);
    }

    /**
     * Searches all sources in parallel.
     * returns a Future that completes when all sources have returned (or timed out).
     */
    public List<Game> searchAll(String keyword) {
        List<CompletableFuture<List<Game>>> futures = sources.stream()
            .map(source -> CompletableFuture.supplyAsync(() -> {
                try {
                    return source.searchGame(keyword);
                } catch (Exception e) {
                    System.err.println("Error searching " + source.getSourceName() + ": " + e.getMessage());
                    return Collections.<Game>emptyList();
                }
            }, executor))
            .collect(Collectors.toList());

        // Wait for all
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Collect results
        List<Game> allGames = new ArrayList<>();
        for (CompletableFuture<List<Game>> f : futures) {
            try {
                allGames.addAll(f.get());
            } catch (Exception e) { 
                // Should not happen as we catch inside supplyAsync
            }
        }
        return allGames;
    }

    public List<Resource> getDownloads(Game game) throws Exception {
        // Use the source identifier from the game object to find which service to call
        // But our interface searchGame returns Game. 
        // We need to know which source produced which game to call getDownloads on proper source.
        // We added 'source' field to Game.
        
        for (GameSource s : sources) {
            if (s.getSourceName().equals(game.source())) {
                return s.getDownloads(game.id());
            }
        }
        throw new Exception("Source not found: " + game.source());
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
