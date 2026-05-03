package uk.co.dajohnston.portal.spicetracker.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import uk.co.dajohnston.portal.spicetracker.mapper.SpiceMapper;
import uk.co.dajohnston.portal.spicetracker.model.SpiceDto;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceEntity;
import uk.co.dajohnston.portal.spicetracker.service.SpicesService;

@WebMvcTest(SpicesController.class)
class SpicesControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private SpicesService spicesService;
  @MockitoBean private SpiceMapper spiceMapper;

  @Test
  void listSpices_returnsOk() throws Exception {
    when(spicesService.listSpices()).thenReturn(List.of());
    mockMvc
        .perform(
            get("/api/spices")
                .header("X-Household-Id", "0161bbeb-a639-dc15-61c7-4cd55c7537b8")
                .with(jwt()))
        .andExpect(status().isOk());
  }

  @Test
  void createSpice_returnsCreated() throws Exception {
    UUID spiceId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");
    when(spicesService.createSpice(any()))
        .thenReturn(SpiceEntity.builder().id(spiceId).name("Cumin").build());
    when(spiceMapper.toDto(any())).thenReturn(SpiceDto.builder().id(spiceId).name("Cumin").build());
    mockMvc
        .perform(
            post("/api/spices")
                .header("X-Household-Id", "0161bbeb-a639-dc15-61c7-4cd55c7537b8")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Cumin\"}"))
        .andExpect(status().isCreated());
  }

  @Test
  void removeSpice_returnsNoContent() throws Exception {
    mockMvc
        .perform(
            delete("/api/spices/6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e")
                .header("X-Household-Id", "0161bbeb-a639-dc15-61c7-4cd55c7537b8")
                .with(jwt()))
        .andExpect(status().isNoContent());
  }
}
