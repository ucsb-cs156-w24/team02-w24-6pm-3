package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;



@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

    @MockBean
    private MenuItemReviewRepository menuItemReviewRepository;

    @MockBean
    private UserRepository userRepository;

    // Tests for GET /api/menuitemreviews/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/menuitemreview/all"))
                .andExpect(status().is(403)); // assuming the security setup is similar
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/menuitemreview/all"))
                .andExpect(status().is(200)); // assuming there's no special restriction
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_menuitemreviews() throws Exception {
        // Arrange
        LocalDateTime reviewDate1 = LocalDateTime.now().minusDays(1);
        LocalDateTime reviewDate2 = LocalDateTime.now();
        MenuItemReview review1 = new MenuItemReview(1L, 1L, "user1@example.com", 5, reviewDate1, "Great!");
        MenuItemReview review2 = new MenuItemReview(2L, 2L, "user2@example.com", 4, reviewDate2, "Good!");

        ArrayList<MenuItemReview> expectedReviews = new ArrayList<>(Arrays.asList(review1, review2));
        when(menuItemReviewRepository.findAll()).thenReturn(expectedReviews);

        // Act & Assert
        MvcResult response = mockMvc.perform(get("/api/menuitemreview/all"))
                .andExpect(status().is(200)).andReturn();

        // Assert
        verify(menuItemReviewRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedReviews);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Example test for POST /api/menuitemreviews/post

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/menuitemreview/post"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/menuitemreview/post"))
                .andExpect(status().isForbidden()); // assuming only admins can post
    }

    @WithMockUser(roles = { "ADMIN" })
    @Test
    public void an_admin_user_can_post_a_new_menuitemreview() throws Exception {
        // Arrange
        LocalDateTime reviewDate = LocalDateTime.now();
        MenuItemReview newReview = new MenuItemReview(0L, 3L, "admin@example.com", 5, reviewDate, "Excellent!");

        when(menuItemReviewRepository.save(any(MenuItemReview.class))).thenReturn(newReview);

        // Act & Assert
        MvcResult response = mockMvc.perform(post("/api/menuitemreview/post")
                .param("itemId", String.valueOf(newReview.getItemId()))
                .param("reviewerEmail", newReview.getReviewerEmail())
                .param("stars", String.valueOf(newReview.getStars()))
                .param("comments", newReview.getComments())
                .param("dateReviewed", newReview.getDateReviewed().toString())
                .with(csrf()))
                .andExpect(status().is(200)).andReturn();


        // Assert
        verify(menuItemReviewRepository, times(1)).save(any(MenuItemReview.class));
        String expectedJson = mapper.writeValueAsString(newReview);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Further tests could be adapted from the UCSBDatesControllerTests template as needed
}
