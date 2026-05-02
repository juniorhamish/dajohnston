package uk.co.dajohnston.portal.spicetracker.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.dajohnston.portal.spicetracker.mapper.PantryJarMapper;
import uk.co.dajohnston.portal.spicetracker.model.PantryJarDto;
import uk.co.dajohnston.portal.spicetracker.repository.PantryJarEntity;
import uk.co.dajohnston.portal.spicetracker.service.PantryService;

@WebMvcTest(PantryController.class)
class PantryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private PantryService pantryService;
  @MockitoBean private PantryJarMapper pantryJarMapper;

  @Test
  void listPantryJars_returnsOk() throws Exception {
    when(pantryService.listPantryJars()).thenReturn(List.of());
    mockMvc.perform(get("/api/pantry").with(jwt())).andExpect(status().isOk());
  }

  @Test
  void addPantryJar_returnsCreated() throws Exception {
    UUID spiceId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UUID jarId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    when(pantryService.addPantryJar(eq(spiceId), any()))
        .thenReturn(PantryJarEntity.builder().id(jarId).build());
    when(pantryJarMapper.toDto(any()))
        .thenReturn(
            PantryJarDto.builder()
                .id(jarId)
                .spiceId(spiceId)
                .spiceName("Cinnamon")
                .quantity(100)
                .build());
    mockMvc
        .perform(
            post("/api/pantry")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"spiceId\": \"" + spiceId + "\"}"))
        .andExpect(status().isCreated());
  }

  @Test
  void updatePantryJar_returnsOk() throws Exception {
    UUID jarId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    UUID spiceId = UUID.fromString("00000000-0000-0000-0000-000000000003");
    when(pantryService.updatePantryJar(jarId, 50))
        .thenReturn(PantryJarEntity.builder().id(jarId).quantity(50).build());
    when(pantryJarMapper.toDto(any()))
        .thenReturn(
            PantryJarDto.builder()
                .id(jarId)
                .spiceId(spiceId)
                .spiceName("Cinnamon")
                .quantity(50)
                .build());
    mockMvc
        .perform(
            patch("/api/pantry/" + jarId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\": 50}"))
        .andExpect(status().isOk());
  }

  @Test
  void removePantryJar_returnsNoContent() throws Exception {
    UUID jarId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");
    mockMvc.perform(delete("/api/pantry/" + jarId).with(jwt())).andExpect(status().isNoContent());
  }
}
