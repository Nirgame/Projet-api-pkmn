package net.tcgdex;

import net.tcgdex.entity.User;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.GenerationOption;
import net.tcgdex.model.PokedexDetailView;
import net.tcgdex.model.PokedexPageResult;
import net.tcgdex.model.PokemonAlternativeForm;
import net.tcgdex.model.PokemonIndexEntry;
import net.tcgdex.model.PokemonSpeciesInfo;
import net.tcgdex.model.RegionalDisplayMode;
import net.tcgdex.model.RegionalForm;
import net.tcgdex.repository.UserCardRepository;
import net.tcgdex.repository.UserPokemonCardAssignmentRepository;
import net.tcgdex.service.CollectionService;
import net.tcgdex.service.PokeApiService;
import net.tcgdex.service.PokedexService;
import net.tcgdex.service.TCGdexService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PokedexServiceTest {

    @Test
    void generationFilterShouldReturnBasePokemonWithoutRegionalNoise() throws IOException {
        PokeApiService pokeApiService = mock(PokeApiService.class);
        TCGdexService tcgdexService = mock(TCGdexService.class);
        CollectionService collectionService = mock(CollectionService.class);
        UserCardRepository userCardRepository = mock(UserCardRepository.class);
        UserPokemonCardAssignmentRepository assignmentRepository = mock(UserPokemonCardAssignmentRepository.class);
        User user = mock(User.class);

        when(assignmentRepository.findByUser(user)).thenReturn(List.of());
        when(collectionService.getUserCollection(user)).thenReturn(List.of());
        when(pokeApiService.getGenerationOptions()).thenReturn(List.of(
                new GenerationOption(1, "Generation I"),
                new GenerationOption(2, "Generation II")));
        when(pokeApiService.getPokedexEntries()).thenReturn(List.of(
                new PokemonIndexEntry(19, 19, "rattata", 1, "Generation I", null),
                new PokemonIndexEntry(152, 152, "chikorita", 2, "Generation II", null),
                new PokemonIndexEntry(155, 155, "cyndaquil", 2, "Generation II", null),
                new PokemonIndexEntry(1_010_019, 19, "rattata", 1, "Generation I", RegionalForm.ALOLA)));
        when(pokeApiService.getPokemonSpecies(152)).thenReturn(
                new PokemonSpeciesInfo(152, 152, "chikorita", "Chikorita", "Germignon", 2, "Generation II", null));
        when(pokeApiService.getPokemonSpecies(155)).thenReturn(
                new PokemonSpeciesInfo(155, 155, "cyndaquil", "Cyndaquil", "Héricendre", 2, "Generation II", null));

        PokedexService service = new PokedexService(
                pokeApiService,
                tcgdexService,
                collectionService,
                userCardRepository,
                assignmentRepository);

        PokedexPageResult result = service.getPokedexPage(
                user,
                null,
                2,
                false,
                false,
                false,
                RegionalDisplayMode.EXCLUDE,
                null,
                RegionalDisplayMode.INCLUDE,
                1,
                24);

        assertThat(result.totalResults()).isEqualTo(2);
        assertThat(result.pokemons())
                .extracting(item -> item.species().englishName())
                .containsExactly("Chikorita", "Cyndaquil");
    }

    @Test
    void pokemonDetailShouldFallbackToBaseSpeciesCardsForAlternativeForms() throws IOException {
        PokeApiService pokeApiService = mock(PokeApiService.class);
        TCGdexService tcgdexService = mock(TCGdexService.class);
        CollectionService collectionService = mock(CollectionService.class);
        UserCardRepository userCardRepository = mock(UserCardRepository.class);
        UserPokemonCardAssignmentRepository assignmentRepository = mock(UserPokemonCardAssignmentRepository.class);
        User user = mock(User.class);

        PokemonAlternativeForm landorusTotem = new PokemonAlternativeForm(
                645, 1, "Landorus Totem", "Demeteros Forme Totemique", "Totemique", false,
                List.of("landorus totem", "demeteros forme totemique"));
        PokemonSpeciesInfo landorusTotemSpecies = new PokemonSpeciesInfo(
                landorusTotem.toEntryId(), 645, "landorus", "Landorus Totem", "Demeteros Forme Totemique",
                5, "Generation V", null, landorusTotem, "Landorus", "Demeteros");
        PokemonSpeciesInfo baseLandorus = new PokemonSpeciesInfo(
                645, 645, "landorus", "Landorus", "Demeteros", 5, "Generation V", null);

        CardBrief plainLandorus = new CardBrief();
        plainLandorus.setId("bw-1");
        plainLandorus.setEnglishName("Landorus");
        plainLandorus.setFrenchName("Demeteros");

        when(collectionService.getUserCollection(user)).thenReturn(List.of());
        when(assignmentRepository.findByUserAndPokemonId(user, landorusTotem.toEntryId())).thenReturn(java.util.Optional.empty());
        when(pokeApiService.getPokemonSpecies(landorusTotem.toEntryId())).thenReturn(landorusTotemSpecies);
        when(pokeApiService.getBasePokemonSpecies(645)).thenReturn(baseLandorus);
        when(tcgdexService.searchCards("Landorus")).thenReturn(List.of(plainLandorus));
        when(tcgdexService.searchCards("Demeteros")).thenReturn(List.of(plainLandorus));
        when(tcgdexService.searchCards("landorus")).thenReturn(List.of(plainLandorus));

        PokedexService service = new PokedexService(
                pokeApiService,
                tcgdexService,
                collectionService,
                userCardRepository,
                assignmentRepository);

        PokedexDetailView detail = service.getPokemonDetail(user, landorusTotem.toEntryId());

        assertThat(detail.availableCards()).extracting(CardBrief::getId).containsExactly("bw-1");
    }

    @Test
    void generationFilterShouldPlaceAlolanFormsInGenerationSeven() throws IOException {
        PokeApiService pokeApiService = mock(PokeApiService.class);
        TCGdexService tcgdexService = mock(TCGdexService.class);
        CollectionService collectionService = mock(CollectionService.class);
        UserCardRepository userCardRepository = mock(UserCardRepository.class);
        UserPokemonCardAssignmentRepository assignmentRepository = mock(UserPokemonCardAssignmentRepository.class);
        User user = mock(User.class);

        when(assignmentRepository.findByUser(user)).thenReturn(List.of());
        when(collectionService.getUserCollection(user)).thenReturn(List.of());
        when(pokeApiService.getGenerationOptions()).thenReturn(List.of(
                new GenerationOption(1, "Generation I"),
                new GenerationOption(7, "Generation VII")));
        when(pokeApiService.getPokedexEntries()).thenReturn(List.of(
                new PokemonIndexEntry(19, 19, "rattata", 1, "Generation I", null),
                new PokemonIndexEntry(1_010_019, 19, "rattata", 7, "Generation VII", RegionalForm.ALOLA)));
        when(pokeApiService.getPokemonSpecies(1_010_019)).thenReturn(
                new PokemonSpeciesInfo(1_010_019, 19, "rattata", "Alolan Rattata", "Rattata d'Alola", 7, "Generation VII", RegionalForm.ALOLA));

        PokedexService service = new PokedexService(
                pokeApiService,
                tcgdexService,
                collectionService,
                userCardRepository,
                assignmentRepository);

        PokedexPageResult result = service.getPokedexPage(
                user,
                null,
                7,
                false,
                false,
                false,
                RegionalDisplayMode.ONLY,
                null,
                RegionalDisplayMode.INCLUDE,
                1,
                24);

        assertThat(result.totalResults()).isEqualTo(1);
        assertThat(result.pokemons().getFirst().species().generationLabel()).isEqualTo("Generation VII");
        assertThat(result.pokemons().getFirst().species().englishName()).isEqualTo("Alolan Rattata");
    }

    @Test
    void excludeAlternativeModeShouldKeepBaseReplacementEntriesVisible() throws IOException {
        PokeApiService pokeApiService = mock(PokeApiService.class);
        TCGdexService tcgdexService = mock(TCGdexService.class);
        CollectionService collectionService = mock(CollectionService.class);
        UserCardRepository userCardRepository = mock(UserCardRepository.class);
        UserPokemonCardAssignmentRepository assignmentRepository = mock(UserPokemonCardAssignmentRepository.class);
        User user = mock(User.class);

        PokemonAlternativeForm wormadamLeaf = new PokemonAlternativeForm(
                413, 1, "Wormadam Leaf", "Cheniselle Cape Plante", "Cape Plante", true,
                List.of("wormadam leaf", "cheniselle cape plante"));
        PokemonAlternativeForm oricorioPompom = new PokemonAlternativeForm(
                741, 1, "Oricorio Pompom", "Plumeline Style Pom-Pom", "Pom-Pom", true,
                List.of("oricorio pompom", "plumeline style pom-pom"));

        when(assignmentRepository.findByUser(user)).thenReturn(List.of());
        when(collectionService.getUserCollection(user)).thenReturn(List.of());
        when(pokeApiService.getGenerationOptions()).thenReturn(List.of(
                new GenerationOption(4, "Generation IV"),
                new GenerationOption(7, "Generation VII")));
        when(pokeApiService.getPokedexEntries()).thenReturn(List.of(
                new PokemonIndexEntry(413, 413, "wormadam", 4, "Generation IV", null, wormadamLeaf, "Wormadam Leaf", "Cheniselle Cape Plante", "Cape Plante"),
                new PokemonIndexEntry(741, 741, "oricorio", 7, "Generation VII", null, oricorioPompom, "Oricorio Pompom", "Plumeline Style Pom-Pom", "Pom-Pom"),
                new PokemonIndexEntry(2_007_452, 745, "lycanroc", 7, "Generation VII", null,
                        new PokemonAlternativeForm(745, 2, "Lycanroc Night", "Lougaroc Forme Nocturne", "Nocturne", false, List.of("lycanroc night")),
                        "Lycanroc Night", "Lougaroc Forme Nocturne", "Nocturne")));
        when(pokeApiService.getPokemonSpecies(413)).thenReturn(
                new PokemonSpeciesInfo(413, 413, "wormadam", "Wormadam Leaf", "Cheniselle Cape Plante", 4, "Generation IV", null, wormadamLeaf, "Wormadam", "Cheniselle"));
        when(pokeApiService.getPokemonSpecies(741)).thenReturn(
                new PokemonSpeciesInfo(741, 741, "oricorio", "Oricorio Pompom", "Plumeline Style Pom-Pom", 7, "Generation VII", null, oricorioPompom, "Oricorio", "Plumeline"));

        PokedexService service = new PokedexService(
                pokeApiService,
                tcgdexService,
                collectionService,
                userCardRepository,
                assignmentRepository);

        PokedexPageResult result = service.getPokedexPage(
                user,
                null,
                null,
                false,
                false,
                false,
                RegionalDisplayMode.EXCLUDE,
                null,
                RegionalDisplayMode.INCLUDE,
                1,
                24);

        assertThat(result.pokemons())
                .extracting(item -> item.species().frenchName())
                .contains("Cheniselle Cape Plante", "Plumeline Style Pom-Pom")
                .doesNotContain("Lougaroc Forme Nocturne");
    }

    @Test
    void hisuiNativeSpeciesShouldKeepCardsWithHisuiMarker() throws IOException {
        PokeApiService pokeApiService = mock(PokeApiService.class);
        TCGdexService tcgdexService = mock(TCGdexService.class);
        CollectionService collectionService = mock(CollectionService.class);
        UserCardRepository userCardRepository = mock(UserCardRepository.class);
        UserPokemonCardAssignmentRepository assignmentRepository = mock(UserPokemonCardAssignmentRepository.class);
        User user = mock(User.class);

        PokemonSpeciesInfo sneasler = new PokemonSpeciesInfo(
                903, 903, "sneasler", "Sneasler", "Farfurex", 8, "Generation VIII", null);
        CardBrief hisuianCard = new CardBrief();
        hisuianCard.setId("swsh10-95");
        hisuianCard.setEnglishName("Sneasler");
        hisuianCard.setFrenchName("Farfurex de Hisui");

        when(collectionService.getUserCollection(user)).thenReturn(List.of());
        when(assignmentRepository.findByUserAndPokemonId(user, 903)).thenReturn(Optional.empty());
        when(pokeApiService.getPokemonSpecies(903)).thenReturn(sneasler);
        when(pokeApiService.getBasePokemonSpecies(903)).thenReturn(sneasler);
        when(tcgdexService.searchCards("Sneasler")).thenReturn(List.of(hisuianCard));
        when(tcgdexService.searchCards("Farfurex")).thenReturn(List.of(hisuianCard));
        when(tcgdexService.searchCards("sneasler")).thenReturn(List.of(hisuianCard));

        PokedexService service = new PokedexService(
                pokeApiService,
                tcgdexService,
                collectionService,
                userCardRepository,
                assignmentRepository);

        PokedexDetailView detail = service.getPokemonDetail(user, 903);

        assertThat(detail.availableCards()).extracting(CardBrief::getId).containsExactly("swsh10-95");
    }
}
