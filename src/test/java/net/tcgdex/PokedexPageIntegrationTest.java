package net.tcgdex;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserCard;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.GenerationOption;
import net.tcgdex.model.PokedexDetailView;
import net.tcgdex.model.PokedexListItem;
import net.tcgdex.model.PokedexPageResult;
import net.tcgdex.model.PokemonSpeciesInfo;
import net.tcgdex.model.RegionalDisplayMode;
import net.tcgdex.model.RegionalForm;
import net.tcgdex.repository.UserRepository;
import net.tcgdex.service.PokedexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PokedexPageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private PokedexService pokedexService;

    @Test
    void pokedexPageShouldRenderAssignedCardPreview() throws Exception {
        User user = userRepository.findByUsername("gary")
                .orElseGet(() -> userRepository.save(new User("gary", passwordEncoder.encode("pokemon123"))));

        UserCard assignedCard = new UserCard(user, "base1-44", "Bulbasaur");
        assignedCard.setFrenchName("Bulbizarre");
        assignedCard.setQuantity(2);
        assignedCard.setImage("https://assets.tcgdex.net/en/base/base1/44");

        PokemonSpeciesInfo species = new PokemonSpeciesInfo(1, 1, "bulbasaur", "Bulbasaur", "Bulbizarre", 1, "Generation I", null);
        PokedexPageResult pageResult = new PokedexPageResult(
                List.of(new PokedexListItem(species, assignedCard, 2, false)),
                List.of(new GenerationOption(1, "Generation I")),
                List.of(RegionalForm.values()),
                1,
                1,
                1);

        when(pokedexService.getPokedexPage(any(User.class), eq(null), eq(null), eq(false), eq(false), eq(RegionalDisplayMode.INCLUDE), eq(null), eq(1), eq(24))).thenReturn(pageResult);

        mockMvc.perform(get("/pokedex")
                        .with(user("gary").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bulbizarre")))
                .andExpect(content().string(containsString("Carte assignee")))
                .andExpect(content().string(containsString("Generation I")))
                .andExpect(content().string(containsString("Non assignes seulement")))
                .andExpect(content().string(containsString("Formes alternatives")))
                .andExpect(content().string(containsString("Inclure")));
    }

    @Test
    void pokedexPageShouldRenderRegionalBadgeWithoutTemplateError() throws Exception {
        User user = userRepository.findByUsername("ethan")
                .orElseGet(() -> userRepository.save(new User("ethan", passwordEncoder.encode("pokemon123"))));

        PokemonSpeciesInfo regionalSpecies = new PokemonSpeciesInfo(
                1_010_019,
                19,
                "rattata",
                "Alolan Rattata",
                "Rattata d'Alola",
                7,
                "Generation VII",
                RegionalForm.ALOLA);

        PokedexPageResult pageResult = new PokedexPageResult(
                List.of(new PokedexListItem(regionalSpecies, null, 0, false)),
                List.of(new GenerationOption(7, "Generation VII")),
                List.of(RegionalForm.values()),
                1,
                1,
                1);

        when(pokedexService.getPokedexPage(any(User.class), eq(null), eq(null), eq(false), eq(false), eq(RegionalDisplayMode.ONLY), eq(null), eq(1), eq(24)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/pokedex")
                        .param("regionalMode", "ONLY")
                        .with(user("ethan").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Rattata d&#39;Alola")))
                .andExpect(content().string(containsString("Alolan Rattata")))
                .andExpect(content().string(containsString("Alola")))
                .andExpect(content().string(containsString("Generation VII")));
    }

    @Test
    void pokedexDetailPageShouldRenderOwnedAndAvailableCards() throws Exception {
        User user = userRepository.findByUsername("blue")
                .orElseGet(() -> userRepository.save(new User("blue", passwordEncoder.encode("pokemon123"))));

        UserCard ownedCard = new UserCard(user, "base1-44", "Bulbasaur");
        ownedCard.setFrenchName("Bulbizarre");
        ownedCard.setQuantity(1);
        ownedCard.setImage("https://assets.tcgdex.net/en/base/base1/44");

        CardBrief availableCard = new CardBrief("base1-44", "44", "Bulbasaur", "https://assets.tcgdex.net/en/base/base1/44");
        availableCard.setFrenchName("Bulbizarre");

        PokemonSpeciesInfo species = new PokemonSpeciesInfo(1, 1, "bulbasaur", "Bulbasaur", "Bulbizarre", 1, "Generation I", null);
        PokedexDetailView detailView = new PokedexDetailView(
                species,
                ownedCard,
                List.of(ownedCard),
                List.of(availableCard),
                Map.of("base1-44", 1),
                false);

        when(pokedexService.getPokemonDetail(any(User.class), eq(1))).thenReturn(detailView);

        mockMvc.perform(get("/pokedex/1")
                        .with(user("blue").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cartes deja dans ma collection")))
                .andExpect(content().string(containsString("Cartes TCG disponibles pour ce Pokemon")))
                .andExpect(content().string(containsString("Assigner")));
    }
}
