package at.ac.ase.inso.group02.recommendation;

import at.ac.ase.inso.group02.authentication.UserService;
import at.ac.ase.inso.group02.authentication.dto.UserDetailDTO;
import at.ac.ase.inso.group02.authentication.dto.UserLocationDTO;
import at.ac.ase.inso.group02.skills.SkillOfferService;
import at.ac.ase.inso.group02.skills.dto.*;
import at.ac.ase.inso.group02.util.pagination.PaginatedQueryDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class RecommendationServiceTest {

    @Inject
    RecommendationService recommendationService;

    @InjectMock
    SkillOfferService skillOfferService;

    @InjectMock
    UserService userService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // Basic Functionality Tests

    @Test
    void testGetRecommendations_shouldReturnEmptyWhenNoSkillsFound() {
        // Arrange
        SkillDemandDTO demandDTO = createTestDemand("Java Programming Help");
        UserDetailDTO userDTO = createTestUser(16.3738, 48.2082);
        
        when(userService.getCurrentUser()).thenReturn(userDTO);
        when(skillOfferService.getSkillsFiltered(any())).thenReturn(createEmptyPaginatedResponse());

        // Act
        PaginatedQueryDTO<SkillOfferDTO> result = recommendationService.getRecommendations(demandDTO);

        // Assert
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotal());
        verify(skillOfferService, times(1)).getSkillsFiltered(any());
    }

    @Test
    void testGetRecommendations_shouldHandleNullUser() {
        // Arrange
        SkillDemandDTO demandDTO = createTestDemand("Java Programming Help");
        when(userService.getCurrentUser()).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, 
            () -> recommendationService.getRecommendations(demandDTO));
    }

    // Rating Filter Tests

    @ParameterizedTest
    @ValueSource(doubles = {7.5, 8.0, 9.0, 10.0})
    void testGetRecommendations_shouldIncludeHighRatedOffers(double rating) {
        // Arrange
        SkillDemandDTO demandDTO = createTestDemand("Java Programming Help");
        UserDetailDTO userDTO = createTestUser(16.3738, 48.2082);
        List<SkillOfferDTO> offers = List.of(
            createTestOffer("Java Programming Tutor", rating)
        );

        when(userService.getCurrentUser()).thenReturn(userDTO);
        when(skillOfferService.getSkillsFiltered(any())).thenReturn(
            createPaginatedResponse(offers)
        );

        // Act
        PaginatedQueryDTO<SkillOfferDTO> result = recommendationService.getRecommendations(demandDTO);

        // Assert
        assertFalse(result.getItems().isEmpty());
        assertEquals(1, result.getItems().size());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 5.0, 7.0, 7.4})
    void testGetRecommendations_shouldFilterLowRatedOffers(double rating) {
        // Arrange
        SkillDemandDTO demandDTO = createTestDemand("Java Programming Help");
        UserDetailDTO userDTO = createTestUser(16.3738, 48.2082);
        List<SkillOfferDTO> offers = List.of(
            createTestOffer("Java Programming Tutor", rating)
        );

        when(userService.getCurrentUser()).thenReturn(userDTO);
        when(skillOfferService.getSkillsFiltered(any())).thenReturn(
            createPaginatedResponse(offers)
        );

        // Act
        PaginatedQueryDTO<SkillOfferDTO> result = recommendationService.getRecommendations(demandDTO);

        // Assert
        if (rating == 0.0) {
            assertFalse(result.getItems().isEmpty()); // New offers should be included
        } else {
            assertTrue(result.getItems().isEmpty()); // Low rated offers should be filtered out
        }
    }

    // Title Matching Tests

    @Test
    void testGetRecommendations_shouldMatchPartialTitleKeywords() {
        // Arrange
        SkillDemandDTO demandDTO = createTestDemand("Java Programming Help");
        UserDetailDTO userDTO = createTestUser(16.3738, 48.2082);
        List<SkillOfferDTO> offers = List.of(
            createTestOffer("Java Expert", 8.0),
            createTestOffer("Programming Tutor", 8.0),
            createTestOffer("Python Developer", 8.0)
        );

        when(userService.getCurrentUser()).thenReturn(userDTO);
        when(skillOfferService.getSkillsFiltered(any())).thenReturn(
            createPaginatedResponse(offers)
        );

        // Act
        PaginatedQueryDTO<SkillOfferDTO> result = recommendationService.getRecommendations(demandDTO);

        // Assert
        assertEquals(2, result.getItems().size());
        assertTrue(result.getItems().stream()
            .allMatch(offer -> 
                offer.getTitle().toLowerCase().contains("java") ||
                offer.getTitle().toLowerCase().contains("programming")
            ));
    }

    @Test
    void testGetRecommendations_shouldMatchCaseInsensitive() {
        // Arrange
        SkillDemandDTO demandDTO = createTestDemand("JAVA programming HELP");
        UserDetailDTO userDTO = createTestUser(16.3738, 48.2082);
        List<SkillOfferDTO> offers = List.of(
            createTestOffer("java Expert", 8.0),
            createTestOffer("PROGRAMMING Tutor", 8.0)
        );

        when(userService.getCurrentUser()).thenReturn(userDTO);
        when(skillOfferService.getSkillsFiltered(any())).thenReturn(
            createPaginatedResponse(offers)
        );

        // Act
        PaginatedQueryDTO<SkillOfferDTO> result = recommendationService.getRecommendations(demandDTO);

        // Assert
        assertEquals(2, result.getItems().size());
    }

    // Category Tests

    @Test
    void testGetRecommendations_shouldFilterByCategory() {
        // Arrange
        SkillDemandDTO demandDTO = createTestDemand("Java Help");
        UserDetailDTO userDTO = createTestUser(16.3738, 48.2082);
        
        ArgumentCaptor<SkillQueryParamsDTO> paramsCaptor = ArgumentCaptor.forClass(SkillQueryParamsDTO.class);
        when(userService.getCurrentUser()).thenReturn(userDTO);
        when(skillOfferService.getSkillsFiltered(any())).thenReturn(createEmptyPaginatedResponse());

        // Act
        recommendationService.getRecommendations(demandDTO);

        // Assert
        verify(skillOfferService).getSkillsFiltered(paramsCaptor.capture());
        SkillQueryParamsDTO capturedParams = paramsCaptor.getValue();
        assertEquals(Set.of(1L), capturedParams.getCategory());
    }

    // Geographic Location Tests

    @Test
    void testGetRecommendations_shouldUseUserLocation() {
        // Arrange
        double testLat = 48.2082;
        double testLon = 16.3738;
        SkillDemandDTO demandDTO = createTestDemand("Java Help");
        UserDetailDTO userDTO = createTestUser(testLon, testLat);
        
        ArgumentCaptor<SkillQueryParamsDTO> paramsCaptor = ArgumentCaptor.forClass(SkillQueryParamsDTO.class);
        when(userService.getCurrentUser()).thenReturn(userDTO);
        when(skillOfferService.getSkillsFiltered(any())).thenReturn(createEmptyPaginatedResponse());

        // Act
        recommendationService.getRecommendations(demandDTO);

        // Assert
        verify(skillOfferService).getSkillsFiltered(paramsCaptor.capture());
        SkillQueryParamsDTO capturedParams = paramsCaptor.getValue();
        assertEquals(testLon, capturedParams.getLat());
        assertEquals(testLat, capturedParams.getLon());
        assertEquals(2500.0, capturedParams.getRadius());
    }

    @Test
    void testGetRecommendations_shouldHandleNullLocation() {
        // Arrange
        SkillDemandDTO demandDTO = createTestDemand("Java Help");
        UserDetailDTO userDTO = UserDetailDTO.builder()
            .username("testUser")
            .location(null)
            .build();

        when(userService.getCurrentUser()).thenReturn(userDTO);

        // Act & Assert
        assertThrows(IllegalStateException.class, 
            () -> recommendationService.getRecommendations(demandDTO));
    }

    // Pagination Tests

    @Test
    void testGetRecommendations_shouldHandlePagination() {
        // Arrange
        SkillDemandDTO demandDTO = createTestDemand("Java Help");
        UserDetailDTO userDTO = createTestUser(16.3738, 48.2082);
        List<SkillOfferDTO> offers = createTestOffersWithDifferentRatings(15);
        
        when(userService.getCurrentUser()).thenReturn(userDTO);
        when(skillOfferService.getSkillsFiltered(any())).thenReturn(
            PaginatedQueryDTO.<SkillOfferDTO>builder()
                .items(offers)
                .total(offers.size())
                .page(0)
                .pageSize(10)
                .hasMore(true)
                .build()
        );

        // Act
        PaginatedQueryDTO<SkillOfferDTO> result = recommendationService.getRecommendations(demandDTO);

        // Assert
        assertTrue(result.isHasMore());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getPageSize());
    }

    // Helper methods

    private SkillDemandDTO createTestDemand(String title) {
        return SkillDemandDTO.builder()
            .title(title)
            .category(SkillCategoryDTO.builder()
                .id(1L)
                .name("Programming")
                .description("Software development skills")
                .build())
            .build();
    }

    private UserDetailDTO createTestUser(double lon, double lat) {
        Point location = geometryFactory.createPoint(new Coordinate(lon, lat));
        return UserDetailDTO.builder()
            .username("testUser")
            .location(UserLocationDTO.builder()
                .homeLocation(location)
                .build())
            .build();
    }

    private SkillOfferDTO createTestOffer(String title, double rating) {
        return SkillOfferDTO.builder()
            .title(title)
            .averageRatingHalfStars(rating)
            .build();
    }

    private List<SkillOfferDTO> createTestOffersWithDifferentRatings(int count) {
        List<SkillOfferDTO> offers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            offers.add(createTestOffer(
                "Java Expert " + i,
                8.0 + (i % 4) * 0.5
            ));
        }
        return offers;
    }

    private PaginatedQueryDTO<SkillOfferDTO> createEmptyPaginatedResponse() {
        return PaginatedQueryDTO.<SkillOfferDTO>builder()
            .items(new ArrayList<>())
            .total(0)
            .page(0)
            .pageSize(10)
            .hasMore(false)
            .build();
    }

    private PaginatedQueryDTO<SkillOfferDTO> createPaginatedResponse(List<SkillOfferDTO> items) {
        return PaginatedQueryDTO.<SkillOfferDTO>builder()
            .items(items)
            .total(items.size())
            .page(0)
            .pageSize(10)
            .hasMore(false)
            .build();
    }
}