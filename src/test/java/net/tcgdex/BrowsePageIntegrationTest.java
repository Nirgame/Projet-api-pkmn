package net.tcgdex;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserCard;
import net.tcgdex.model.CardBrief;
import net.tcgdex.repository.UserCardRepository;
import net.tcgdex.repository.UserRepository;
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
class BrowsePageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCardRepository userCardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private TCGdexService tcgdexService;

    @Test
    void browsePageShouldShowOwnedBadgeQuantityForAuthenticatedUser() throws Exception {
        User user = userRepository.findByUsername("ash")
                .orElseGet(() -> userRepository.save(new User("ash", passwordEncoder.encode("pokemon123"))));

        UserCard userCard = userCardRepository.findByUserAndCardId(user, "base1-4")
                .orElseGet(() -> {
                    UserCard createdCard = new UserCard(user, "base1-4", "Charmander");
                    createdCard.setFrenchName("Salameche");
                    createdCard.setQuantity(3);
                    createdCard.setImage("https://assets.tcgdex.net/en/base/base1/4");
                    return userCardRepository.save(createdCard);
                });

        userCard.setQuantity(3);
        userCardRepository.save(userCard);

        CardBrief card = new CardBrief("base1-4", "4", "Charmander", "https://assets.tcgdex.net/en/base/base1/4");
        card.setFrenchName("Salameche");

        when(tcgdexService.getCards()).thenReturn(List.of(card));
        doNothing().when(tcgdexService).enrichFormLabels(anyList());

        mockMvc.perform(get("/browse")
                        .with(user("ash").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("owned-badge-base1-4")))
                .andExpect(content().string(containsString(">3<")));
    }
}
