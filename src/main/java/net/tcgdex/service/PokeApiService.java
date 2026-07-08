package net.tcgdex.service;

import com.google.gson.Gson;
import net.tcgdex.model.GenerationOption;
import net.tcgdex.model.PokemonAlternativeForm;
import net.tcgdex.model.PokemonIndexEntry;
import net.tcgdex.model.PokemonSpeciesInfo;
import net.tcgdex.model.RegionalForm;
import net.tcgdex.util.PokemonAlternativeForms;
import net.tcgdex.util.PokemonNameUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PokeApiService {
    private static final String BASE_URL = "https://pokeapi.co/api/v2";
    private static final Pattern ID_PATTERN = Pattern.compile("/(\\d+)/?$");

    private final HttpClient httpClient = HttpClients.createDefault();
    private final Gson gson = new Gson();
    private final Map<Integer, PokemonSpeciesInfo> speciesCache = new ConcurrentHashMap<>();
    private final ExecutorService speciesWarmupExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "pokeapi-species-warmup");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean speciesWarmupStarted = new AtomicBoolean(false);
    private volatile List<PokemonIndexEntry> nationalDexEntries;
    private volatile Map<Integer, Integer> generationByPokemonId;
    private volatile List<GenerationOption> generationOptions;

    public List<PokemonIndexEntry> getNationalDexEntries() throws IOException {
        List<PokemonIndexEntry> cachedEntries = nationalDexEntries;
        if (cachedEntries != null) {
            return cachedEntries;
        }

        synchronized (this) {
            if (nationalDexEntries != null) {
                return nationalDexEntries;
            }

            SpeciesListResponse response = get("/pokemon-species?limit=2000", SpeciesListResponse.class);
            nationalDexEntries = response.results == null
                    ? List.of()
                    : response.results.stream()
                            .map(entry -> {
                                int speciesId = extractId(entry.url);
                                PokemonAlternativeForm baseReplacement = PokemonAlternativeForms.getBaseReplacementForm(speciesId);
                                return new PokemonIndexEntry(
                                    speciesId,
                                    speciesId,
                                    entry.name,
                                    0,
                                    "",
                                    null,
                                    baseReplacement,
                                    baseReplacement != null ? baseReplacement.englishName() : PokemonNameUtils.slugToDisplayName(entry.name),
                                    baseReplacement != null ? baseReplacement.frenchName() : null,
                                    baseReplacement != null ? baseReplacement.formLabel() : null);
                            })
                            .filter(entry -> entry.speciesId() > 0)
                            .sorted(Comparator.comparingInt(PokemonIndexEntry::id))
                            .toList();
            startSpeciesWarmup();
            return nationalDexEntries;
        }
    }

    public List<PokemonIndexEntry> getPokedexEntries() throws IOException {
        ensureGenerationIndexLoaded();
        startSpeciesWarmup();

        List<PokemonIndexEntry> entries = new ArrayList<>();
        for (PokemonIndexEntry nationalEntry : getNationalDexEntries()) {
            int generationId = generationByPokemonId.getOrDefault(nationalEntry.speciesId(), 0);
            String generationLabel = getGenerationLabelFromCache(generationId);

            entries.add(new PokemonIndexEntry(
                    nationalEntry.id(),
                    nationalEntry.speciesId(),
                    nationalEntry.slug(),
                    generationId,
                    generationLabel,
                    null,
                    nationalEntry.alternativeForm(),
                    nationalEntry.englishName(),
                    nationalEntry.frenchName(),
                    nationalEntry.formLabel()));

            for (RegionalForm regionalForm : resolveRegionalFormsForSpecies(nationalEntry.speciesId())) {
                int regionalGenerationId = regionalForm.appearanceGenerationId();
                entries.add(new PokemonIndexEntry(
                        regionalForm.toEntryId(nationalEntry.speciesId()),
                        nationalEntry.speciesId(),
                        nationalEntry.slug(),
                        regionalGenerationId,
                        getGenerationLabelFromCache(regionalGenerationId),
                        regionalForm,
                        null,
                        capitalize(regionalForm.englishPrefix()) + " " + PokemonNameUtils.slugToDisplayName(nationalEntry.slug()),
                        nationalEntry.frenchName() == null || nationalEntry.frenchName().isBlank()
                                ? null
                                : nationalEntry.frenchName() + " " + regionalForm.frenchSuffix(),
                        regionalForm.label()));
            }

            for (PokemonAlternativeForm alternativeForm : PokemonAlternativeForms.getAdditionalForms(nationalEntry.speciesId())) {
                int alternativeGenerationId = resolveGenerationId(generationId, alternativeForm.regionalForm());
                entries.add(new PokemonIndexEntry(
                        alternativeForm.toEntryId(),
                        nationalEntry.speciesId(),
                        nationalEntry.slug(),
                        alternativeGenerationId,
                        getGenerationLabelFromCache(alternativeGenerationId),
                        alternativeForm.regionalForm(),
                        alternativeForm,
                        alternativeForm.englishName(),
                        alternativeForm.frenchName(),
                        alternativeForm.formLabel()));
            }
        }

        return entries;
    }

    public List<GenerationOption> getGenerationOptions() throws IOException {
        ensureGenerationIndexLoaded();
        return generationOptions;
    }

    public Integer getGenerationIdForPokemon(int pokemonId) throws IOException {
        ensureGenerationIndexLoaded();
        if (RegionalForm.isRegionalEntryId(pokemonId)) {
            RegionalForm regionalForm = RegionalForm.fromEntryId(pokemonId);
            return regionalForm != null ? regionalForm.appearanceGenerationId() : null;
        }
        if (PokemonAlternativeForm.isAlternativeEntryId(pokemonId)) {
            PokemonAlternativeForm alternativeForm = PokemonAlternativeForms.fromEntryId(pokemonId);
            if (alternativeForm != null && alternativeForm.regionalForm() != null) {
                return alternativeForm.regionalForm().appearanceGenerationId();
            }
            return generationByPokemonId.get(PokemonAlternativeForm.extractSpeciesId(pokemonId));
        }
        return generationByPokemonId.get(pokemonId);
    }

    public PokemonSpeciesInfo getPokemonSpecies(int pokemonId) throws IOException {
        if (PokemonAlternativeForm.isAlternativeEntryId(pokemonId)) {
            return getAlternativePokemonSpecies(pokemonId);
        }
        if (RegionalForm.isRegionalEntryId(pokemonId)) {
            return getRegionalPokemonSpecies(pokemonId);
        }

        PokemonAlternativeForm baseReplacement = PokemonAlternativeForms.getBaseReplacementForm(pokemonId);
        if (baseReplacement != null) {
            return getBaseReplacementSpecies(pokemonId, baseReplacement);
        }

        return getBasePokemonSpecies(pokemonId);
    }

    public PokemonSpeciesInfo getBasePokemonSpecies(int pokemonId) throws IOException {
        if (RegionalForm.isRegionalEntryId(pokemonId)) {
            return getBasePokemonSpecies(RegionalForm.extractSpeciesId(pokemonId));
        }
        if (PokemonAlternativeForm.isAlternativeEntryId(pokemonId)) {
            return getBasePokemonSpecies(PokemonAlternativeForm.extractSpeciesId(pokemonId));
        }

        PokemonSpeciesInfo cachedSpecies = speciesCache.get(pokemonId);
        if (cachedSpecies != null) {
            return cachedSpecies;
        }

        SpeciesResponse response = get("/pokemon-species/" + pokemonId, SpeciesResponse.class);
        Integer generationId = extractId(response.generation != null ? response.generation.url : null);
        String generationLabel = getGenerationLabel(generationId);
        String englishName = localizedName(response.names, "en");
        if (englishName == null || englishName.isBlank()) {
            englishName = PokemonNameUtils.slugToDisplayName(response.name);
        }
        String frenchName = localizedName(response.names, "fr");

        PokemonSpeciesInfo speciesInfo = new PokemonSpeciesInfo(
                response.id,
                response.id,
                response.name,
                englishName,
                frenchName,
                generationId == null ? 0 : generationId,
                generationLabel,
                null,
                null,
                englishName,
                frenchName);

        speciesCache.put(pokemonId, speciesInfo);
        return speciesInfo;
    }

    public PokemonSpeciesInfo findCachedPokemonSpecies(int pokemonId) {
        return speciesCache.get(pokemonId);
    }

    private void ensureGenerationIndexLoaded() throws IOException {
        if (generationByPokemonId != null && generationOptions != null) {
            return;
        }

        synchronized (this) {
            if (generationByPokemonId != null && generationOptions != null) {
                return;
            }

            Map<Integer, Integer> resolvedGenerationByPokemonId = new LinkedHashMap<>();
            List<GenerationOption> resolvedGenerationOptions = new ArrayList<>();

            for (int generationId = 1; generationId <= 9; generationId++) {
                GenerationResponse response = get("/generation/" + generationId, GenerationResponse.class);
                String label = localizedName(response.names, "fr");
                if (label == null || label.isBlank()) {
                    label = "Generation " + romanNumber(generationId);
                }

                resolvedGenerationOptions.add(new GenerationOption(generationId, label));

                if (response.pokemon_species != null) {
                    for (NamedResource species : response.pokemon_species) {
                        Integer pokemonId = extractId(species.url);
                        if (pokemonId != null) {
                            resolvedGenerationByPokemonId.put(pokemonId, generationId);
                        }
                    }
                }
            }

            generationByPokemonId = resolvedGenerationByPokemonId;
            generationOptions = resolvedGenerationOptions;
        }
    }

    private String getGenerationLabel(Integer generationId) throws IOException {
        if (generationId == null) {
            return "";
        }
        ensureGenerationIndexLoaded();
        return generationOptions.stream()
                .filter(option -> option.id() == generationId)
                .map(GenerationOption::label)
                .findFirst()
                .orElse("Generation " + romanNumber(generationId));
    }

    private String getGenerationLabelFromCache(Integer generationId) {
        if (generationId == null || generationId < 1 || generationOptions == null) {
            return "";
        }

        return generationOptions.stream()
                .filter(option -> option.id() == generationId)
                .map(GenerationOption::label)
                .findFirst()
                .orElse("Generation " + romanNumber(generationId));
    }

    private PokemonSpeciesInfo getRegionalPokemonSpecies(int entryId) throws IOException {
        RegionalForm regionalForm = RegionalForm.fromEntryId(entryId);
        int baseSpeciesId = RegionalForm.extractSpeciesId(entryId);
        PokemonSpeciesInfo baseSpecies = getBasePokemonSpecies(baseSpeciesId);
        String englishName = regionalForm == null
                ? baseSpecies.englishName()
                : capitalize(regionalForm.englishPrefix()) + " " + baseSpecies.englishName();
        String frenchName = baseSpecies.frenchName() == null || baseSpecies.frenchName().isBlank()
                ? baseSpecies.frenchName()
                : baseSpecies.frenchName() + " " + regionalForm.frenchSuffix();

        return new PokemonSpeciesInfo(
                entryId,
                baseSpecies.speciesId(),
                baseSpecies.slug(),
                englishName,
                frenchName,
                regionalForm.appearanceGenerationId(),
                getGenerationLabel(regionalForm.appearanceGenerationId()),
                regionalForm,
                null,
                baseSpecies.englishName(),
                baseSpecies.frenchName());
    }

    private PokemonSpeciesInfo getAlternativePokemonSpecies(int entryId) throws IOException {
        PokemonAlternativeForm alternativeForm = PokemonAlternativeForms.fromEntryId(entryId);
        if (alternativeForm == null) {
            throw new IOException("Forme alternative inconnue: " + entryId);
        }

        return buildAlternativeSpeciesInfo(entryId, alternativeForm);
    }

    private PokemonSpeciesInfo getBaseReplacementSpecies(int speciesId, PokemonAlternativeForm alternativeForm) throws IOException {
        return buildAlternativeSpeciesInfo(speciesId, alternativeForm);
    }

    private PokemonSpeciesInfo buildAlternativeSpeciesInfo(int entryId, PokemonAlternativeForm alternativeForm) throws IOException {
        PokemonSpeciesInfo baseSpecies = getBasePokemonSpecies(alternativeForm.speciesId());
        int generationId = resolveGenerationId(baseSpecies.generationId(), alternativeForm.regionalForm());
        return new PokemonSpeciesInfo(
                entryId,
                baseSpecies.speciesId(),
                baseSpecies.slug(),
                alternativeForm.englishName(),
                alternativeForm.frenchName(),
                generationId,
                getGenerationLabel(generationId),
                alternativeForm.regionalForm(),
                alternativeForm,
                baseSpecies.englishName(),
                baseSpecies.frenchName());
    }

    private int resolveGenerationId(int baseGenerationId, RegionalForm regionalForm) {
        if (regionalForm != null) {
            return regionalForm.appearanceGenerationId();
        }
        return baseGenerationId;
    }

    private void startSpeciesWarmup() {
        if (!speciesWarmupStarted.compareAndSet(false, true)) {
            return;
        }

        speciesWarmupExecutor.submit(() -> {
            try {
                for (PokemonIndexEntry entry : getNationalDexEntries()) {
                    try {
                        getBasePokemonSpecies(entry.speciesId());
                    } catch (IOException ignored) {
                    }
                }
            } catch (IOException ignored) {
            }
        });
    }

    public EnumSet<RegionalForm> getRegionalFormsForSpecies(int speciesId) {
        return EnumSet.copyOf(resolveRegionalFormsForSpecies(speciesId));
    }

    private EnumSet<RegionalForm> resolveRegionalFormsForSpecies(int speciesId) {
        return switch (speciesId) {
            case 19, 20, 26, 27, 28, 37, 38, 50, 51, 53, 74, 75, 76, 88, 89, 103, 105 ->
                EnumSet.of(RegionalForm.ALOLA);
            case 52 -> EnumSet.of(RegionalForm.ALOLA, RegionalForm.GALAR);
            case 58, 59, 100, 101, 157, 211, 215, 503, 549, 570, 571, 628, 705, 706, 713, 724 ->
                EnumSet.of(RegionalForm.HISUI);
            case 77, 78, 79, 80, 83, 110, 122, 144, 145, 146, 199, 222, 263, 264, 554, 555, 562, 618 ->
                EnumSet.of(RegionalForm.GALAR);
            case 128, 194 -> EnumSet.of(RegionalForm.PALDEA);
            default -> EnumSet.noneOf(RegionalForm.class);
        };
    }

    private <T> T get(String path, Class<T> responseType) throws IOException {
        HttpGet request = new HttpGet(BASE_URL + path);
        request.setHeader("Accept", "application/json");

        return httpClient.execute(request, response -> {
            if (response.getCode() != 200) {
                throw new IOException("Erreur HTTP " + response.getCode() + " sur " + path);
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("Reponse vide sur " + path);
            }

            StringBuilder json = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
            }

            return gson.fromJson(json.toString(), responseType);
        });
    }

    private Integer extractId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        Matcher matcher = ID_PATTERN.matcher(url);
        if (!matcher.find()) {
            return null;
        }

        return Integer.parseInt(matcher.group(1));
    }

    private String localizedName(List<LocalizedName> names, String languageCode) {
        if (names == null) {
            return null;
        }

        return names.stream()
                .filter(name -> name.language != null && languageCode.equalsIgnoreCase(name.language.name))
                .map(name -> name.name)
                .findFirst()
                .orElse(null);
    }

    private String romanNumber(int value) {
        return switch (value) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            default -> String.valueOf(value);
        };
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static class SpeciesListResponse {
        private List<NamedResource> results;
    }

    private static class SpeciesResponse {
        private int id;
        private String name;
        private NamedResource generation;
        private List<LocalizedName> names;
    }

    private static class GenerationResponse {
        private List<LocalizedName> names;
        private List<NamedResource> pokemon_species;
    }

    private static class LocalizedName {
        private String name;
        private NamedResource language;
    }

    private static class NamedResource {
        private String name;
        private String url;
    }
}
