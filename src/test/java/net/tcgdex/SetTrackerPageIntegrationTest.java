package net.tcgdex;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserCard;
import net.tcgdex.entity.UserTrackedSet;
import net.tcgdex.model.CardBrief;
import net.tcgdex.model.Set;
import net.tcgdex.repository.UserCardRepository;
import net.tcgdex.repository.UserRepository;
import net.tcgdex.repository.UserTrackedSetRepository;
import net.tcgdex.service.TCGdexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SetTrackerPageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCardRepository userCardRepository;

    @Autowired
    private UserTrackedSetRepository trackedSetRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private TCGdexService tcgdexService;

    @Test
    void setTrackerPageShouldRender() throws Exception {
        User user = userRepository.findByUsername("misty")
                .orElseGet(() -> userRepository.save(new User("misty", passwordEncoder.encode("pokemon123"))));

        if (!trackedSetRepository.existsByUserAndSetId(user, "base1")) {
            trackedSetRepository.save(new UserTrackedSet(user, "base1", "Set de Base"));
        }

        Set set = new Set();
        set.setId("base1");
        set.setEnglishName("Base Set");
        set.setFrenchName("Set de Base");

        CardBrief card = new CardBrief("base1-4", "4", "Charmander", "https://assets.tcgdex.net/en/base/base1/4");
        card.setFrenchName("Salameche");

        when(tcgdexService.getSets()).thenReturn(List.of(set));
        when(tcgdexService.getSet("base1")).thenReturn(set);
        when(tcgdexService.getCardsBySet("base1")).thenReturn(List.of(card));

        mockMvc.perform(get("/set-tracker")
                        .with(user("misty").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Suivi des sets")))
                .andExpect(content().string(containsString("Set de Base")))
                .andExpect(content().string(containsString("Ajouter au suivi")));
    }

    @Test
    void setTrackerDetailShouldRenderChecklist() throws Exception {
        User user = userRepository.findByUsername("tracey")
                .orElseGet(() -> userRepository.save(new User("tracey", passwordEncoder.encode("pokemon123"))));

        if (!trackedSetRepository.existsByUserAndSetId(user, "base1")) {
            trackedSetRepository.save(new UserTrackedSet(user, "base1", "Set de Base"));
        }

        userCardRepository.findByUserAndCardId(user, "base1-4")
                .orElseGet(() -> {
                    UserCard createdCard = new UserCard(user, "base1-4", "Charmander");
                    createdCard.setFrenchName("Salameche");
                    createdCard.setQuantity(2);
                    createdCard.setImage("https://assets.tcgdex.net/en/base/base1/4");
                    createdCard.setSetId("base1");
                    createdCard.setSetName("Set de Base");
                    return userCardRepository.save(createdCard);
                });

        Set set = new Set();
        set.setId("base1");
        set.setEnglishName("Base Set");
        set.setFrenchName("Set de Base");

        CardBrief card = new CardBrief("base1-4", "4", "Charmander", "https://assets.tcgdex.net/en/base/base1/4");
        card.setFrenchName("Salameche");
        card.setSetId("base1");
        card.setSetName("Set de Base");
        card.setEnglishRarity("Common");
        card.setFrenchRarity("Commune");

        when(tcgdexService.getSet("base1")).thenReturn(set);
        when(tcgdexService.getCardsBySet("base1")).thenReturn(List.of(card));
        doNothing().when(tcgdexService).enrichFormLabels(anyList());
        doNothing().when(tcgdexService).enrichRarities(anyList());
        doNothing().when(tcgdexService).enrichSetMetadata(anyList());

        mockMvc.perform(get("/set-tracker/base1")
                        .with(user("tracey").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Checklist")))
                .andExpect(content().string(containsString("Salameche")))
                .andExpect(content().string(containsString("Possedee")));
    }
}
