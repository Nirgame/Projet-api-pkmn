package net.tcgdex;

import net.tcgdex.entity.User;
import net.tcgdex.entity.UserCard;
import net.tcgdex.entity.UserPokemonCardAssignment;
import net.tcgdex.repository.UserRepository;
import net.tcgdex.service.PokedexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PokedexApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private PokedexService pokedexService;

    @Test
    void assignEndpointShouldReturnSuccessPayload() throws Exception {
        User user = userRepository.findByUsername("leaf")
                .orElseGet(() -> userRepository.save(new User("leaf", passwordEncoder.encode("pokemon123"))));

        UserPokemonCardAssignment assignment = new UserPokemonCardAssignment();
        assignment.setUser(user);
        assignment.setPokemonId(1);
        assignment.setAssignedCardId("base1-44");
        assignment.setComment("Commentaire test");

        UserCard userCard = new UserCard(user, "base1-44", "Bulbasaur");

        when(pokedexService.assignCard(any(User.class), eq(1), eq("base1-44"), eq("Commentaire test"))).thenReturn(assignment);
        when(pokedexService.getAssignedCard(any(User.class), eq(1))).thenReturn(Optional.of(userCard));

        mockMvc.perform(post("/api/pokedex/pokemon/1/assign/base1-44")
                        .contentType("application/json")
                        .content("{\"comment\":\"Commentaire test\"}")
                        .with(csrf())
                        .with(user("leaf").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":true")))
                .andExpect(content().string(containsString("\"cardId\":\"base1-44\"")))
                .andExpect(content().string(containsString("\"comment\":\"Commentaire test\"")));
    }

    @Test
    void clearAssignmentEndpointShouldReturnSuccessPayload() throws Exception {
        User user = userRepository.findByUsername("silver")
                .orElseGet(() -> userRepository.save(new User("silver", passwordEncoder.encode("pokemon123"))));

        doNothing().when(pokedexService).clearAssignment(any(User.class), eq(1));

        mockMvc.perform(delete("/api/pokedex/pokemon/1/assignment")
                        .with(csrf())
                        .with(user("silver").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":true")));
    }

    @Test
    void markMissingCardEndpointShouldReturnSuccessPayload() throws Exception {
        User user = userRepository.findByUsername("crystal")
                .orElseGet(() -> userRepository.save(new User("crystal", passwordEncoder.encode("pokemon123"))));

        UserPokemonCardAssignment assignment = new UserPokemonCardAssignment();
        assignment.setUser(user);
        assignment.setPokemonId(25);
        assignment.setCardMissing(true);
        assignment.setComment("Aucune carte");

        when(pokedexService.markMissingCard(any(User.class), eq(25), eq("Aucune carte"))).thenReturn(assignment);

        mockMvc.perform(post("/api/pokedex/pokemon/25/mark-missing")
                        .contentType("application/json")
                        .content("{\"comment\":\"Aucune carte\"}")
                        .with(csrf())
                        .with(user("crystal").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":true")))
                .andExpect(content().string(containsString("\"missingCard\":true")))
                .andExpect(content().string(containsString("\"comment\":\"Aucune carte\"")));
    }
}
