package com.galgame.search.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galgame.search.model.Game;
import com.galgame.search.model.Resource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class TouchGalService implements GameSource {

    private static final String BASE_URL = "https://www.touchgal.us/api";
    private final HttpClient client;
    private final ObjectMapper mapper;

    public TouchGalService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<Game> searchGame(String keyword) throws Exception {
        // Construct JSON payload manually or via object to match Python's:
        // {"queryString": "[{\"type\":\"keyword\",\"name\":\"KEYWORD\"}]", "limit": 15, "page": 1, ...}
        
        // Note: The Python code double-serializes queryString inside the JSON.
        String innerQuery = mapper.writeValueAsString(List.of(
            new SearchCriteria("keyword", keyword)
        ));

        var payloadNode = mapper.createObjectNode();
        payloadNode.put("queryString", innerQuery);
        payloadNode.put("limit", 15);
        payloadNode.put("page", 1);
        payloadNode.put("selectedType", "all");
        payloadNode.put("selectedLanguage", "all");
        payloadNode.put("selectedPlatform", "all");
        payloadNode.put("sortField", "resource_update_time");
        payloadNode.put("sortOrder", "desc");
        
        var searchOption = mapper.createObjectNode();
        searchOption.put("searchInIntroduction", true);
        searchOption.put("searchInAlias", true);
        searchOption.put("searchInTag", true);
        payloadNode.set("searchOption", searchOption);
        
        payloadNode.set("selectedYears", mapper.createArrayNode());
        payloadNode.set("selectedMonths", mapper.createArrayNode());

        String jsonPayload = mapper.writeValueAsString(payloadNode);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/search"))
                .header("Content-Type", "application/json")
                // Cookie: "kun-patch-setting-store|state|data|kunNsfwEnable": "all"
                // Browsers/Servers can be picky about cookie chars like '|'. 
                // However, standard HttpClient should handle it or we might need to encode.
                // The python request sends it raw. Let's try raw.
                .header("Cookie", "kun-patch-setting-store|state|data|kunNsfwEnable=all") 
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("API Error: HTTP " + response.statusCode());
        }

        JsonNode root = mapper.readTree(response.body());
        // Python: return data.get("galgames", [])
        JsonNode galgamesNode = root.get("galgames");
        
        List<Game> games = new ArrayList<>();
        if (galgamesNode.isArray()) {
            for (JsonNode node : galgamesNode) {
                // Parse Game from node
                // id, name, introduction, platform (array)
                String id = node.path("id").asText();
                String name = node.path("name").asText();
                String introduction = node.path("introduction").asText();
                
                List<String> platforms = new ArrayList<>();
                node.path("platform").forEach(p -> platforms.add(p.asText()));
                
                games.add(new Game(id, name, introduction, platforms, "TouchGal"));
            }
        }
        return games;
    }

    @Override
    public List<Resource> getDownloads(String patchId) throws Exception {
        // GET /patch/resource?patchId=...
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/patch/resource?patchId=" + patchId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("API Error: HTTP " + response.statusCode());
        }

        JsonNode root = mapper.readTree(response.body());
        List<Resource> resources = new ArrayList<>();
        
        if (root.isArray()) {
            for (JsonNode node : root) {
                resources.add(mapper.treeToValue(node, Resource.class));
            }
        }
        return resources;
    }

    @Override
    public String getSourceName() {
        return "TouchGal";
    }

    // Helper record for searching
    record SearchCriteria(String type, String name) {}
}
