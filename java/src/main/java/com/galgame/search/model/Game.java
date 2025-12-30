package com.galgame.search.model;

import java.util.List;

public record Game(
    String id,
    String name,
    String introduction,
    List<String> platform,
    String source // New field to identify origin (e.g. "TouchGal")
) {}
