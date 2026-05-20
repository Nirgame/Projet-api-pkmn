package net.tcgdex;

import net.tcgdex.entity.User;
import net.tcgdex.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CollectionPageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void collectionPageShouldRender() throws Exception {
        if (userRepository.findByUsername("brock").isEmpty()) {
            userRepository.save(new User("brock", passwordEncoder.encode("pokemon123")));
        }

        mockMvc.perform(get("/collection")
                        .with(user("brock").roles("USER")))
                .andExpect(status().isOk());
    }
}
