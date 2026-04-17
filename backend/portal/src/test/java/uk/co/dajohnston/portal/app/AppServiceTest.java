package uk.co.dajohnston.portal.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.dajohnston.portal.app.entity.AppEntity;
import uk.co.dajohnston.portal.app.entity.AppRepository;

@ExtendWith(MockitoExtension.class)
class AppServiceTest {

  @Mock private AppRepository appRepository;
  @Mock private AppMapper appMapper;
  @InjectMocks private AppService appService;

  @Test
  void listActiveApps_returnsActiveApps() {
    AppEntity entity1 =
        AppEntity.builder()
            .id("spice-tracker")
            .name("Spice Tracker")
            .description("Track your spice inventory")
            .icon("pepper")
            .url("/apps/spice-tracker")
            .active(true)
            .build();
    AppEntity entity2 =
        AppEntity.builder()
            .id("meal-planner")
            .name("Meal Planner")
            .description("Plan your weekly meals")
            .icon("utensils")
            .url("/apps/meal-planner")
            .active(true)
            .build();

    App app1 =
        App.builder()
            .id("spice-tracker")
            .name("Spice Tracker")
            .description("Track your spice inventory")
            .icon("pepper")
            .url("/apps/spice-tracker")
            .build();
    App app2 =
        App.builder()
            .id("meal-planner")
            .name("Meal Planner")
            .description("Plan your weekly meals")
            .icon("utensils")
            .url("/apps/meal-planner")
            .build();

    when(appRepository.findByActiveTrue()).thenReturn(List.of(entity1, entity2));
    when(appMapper.toDomain(entity1)).thenReturn(app1);
    when(appMapper.toDomain(entity2)).thenReturn(app2);

    List<App> result = appService.listActiveApps();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).id()).isEqualTo("spice-tracker");
    assertThat(result.get(1).id()).isEqualTo("meal-planner");
  }

  @Test
  void listActiveApps_returnsEmptyList_whenNoActiveApps() {
    when(appRepository.findByActiveTrue()).thenReturn(List.of());

    List<App> result = appService.listActiveApps();

    assertThat(result).isEmpty();
  }
}
