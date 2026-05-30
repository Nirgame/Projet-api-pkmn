package net.tcgdex.model;

import java.util.List;

public record PokedexPageResult(
        List<PokedexListItem> pokemons,
        List<GenerationOption> generationOptions,
        List<RegionalForm> availableRegionalForms,
        int currentPage,
        int totalPages,
        int totalResults,
        int unassignedCount) {
}
