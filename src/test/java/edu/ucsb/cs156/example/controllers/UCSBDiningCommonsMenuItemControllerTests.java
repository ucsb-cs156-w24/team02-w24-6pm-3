package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;


@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)

public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

    @MockBean
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    @MockBean
    UserRepository userRepository;

            // Tests for GET /api/ucsbdiningcommonsmenuitem/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().is(200)); // logged
        }
                

        
        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbdiningcommonsmenuitem() throws Exception {

                // arrange
                UCSBDiningCommonsMenuItem item1 = UCSBDiningCommonsMenuItem.builder()
                                .diningCommonsCode("ortega")
                                .name("Tofu Banh Mi Sandwich (v)")
                                .station("Entree Specials")                               
                                .build();

                UCSBDiningCommonsMenuItem item2 = UCSBDiningCommonsMenuItem.builder()
                                .diningCommonsCode("ortega")
                                .name("Baked Pesto Pasta with Chicken")
                                .station("Entree Specials")                               
                                .build();
                ArrayList<UCSBDiningCommonsMenuItem> expectedItems = new ArrayList<>();
                expectedItems.addAll(Arrays.asList(item1, item2));

                when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedItems);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedItems);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/ucsbdiningcommonsmenuitem/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsbdiningcommonsmenuitem() throws Exception {
                // arrange
                UCSBDiningCommonsMenuItem item1 = UCSBDiningCommonsMenuItem.builder()
                                .diningCommonsCode("ortega")
                                .name("Tofu Banh Mi Sandwich (v)")
                                .station("Entree Specials")                               
                                .build();

                when(ucsbDiningCommonsMenuItemRepository.save(eq(item1))).thenReturn(item1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbdiningcommonsmenuitem/post?diningCommonsCode=ortega&name=Tofu Banh Mi Sandwich (v)&station=Entree Specials")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(item1);
                String expectedJson = mapper.writeValueAsString(item1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }


        @Test
        @WithMockUser(roles = { "USER" })
        public void getById_NotFound_ThrowsEntityNotFoundException() throws Exception {
        String nonExistentCode = "nonexistent";
        when(ucsbDiningCommonsMenuItemRepository.findById(nonExistentCode)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?diningCommonsCode=" + nonExistentCode))
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = {"USER"})
        public void getById_EntityNotFound_ReturnsNotFoundStatus() throws Exception {
        String missingCode = "missing";
        when(ucsbDiningCommonsMenuItemRepository.findById(missingCode)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?diningCommonsCode=" + missingCode))
                .andExpect(status().isNotFound());
}
        @Test
        @WithMockUser(roles = {"USER"})
        public void getById_WhenNotFound_ShouldReturnNotFoundStatus() throws Exception {
        String diningCommonsCode = "nonexistent";
        when(ucsbDiningCommonsMenuItemRepository.findById(diningCommonsCode)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?diningCommonsCode=" + diningCommonsCode))
                .andExpect(status().isNotFound());
        }



        // Tests for GET /api/ucsbdiningcommonsmenuitem/...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?diningCommonsCode=ortega"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                UCSBDiningCommonsMenuItem item1 = UCSBDiningCommonsMenuItem.builder()
                                .diningCommonsCode("ortega")
                                .name("Tofu Banh Mi Sandwich (v)")
                                .station("Entree Specials")                               
                                .build();

                when(ucsbDiningCommonsMenuItemRepository.findById(item1.getDiningCommonsCode())).thenReturn(Optional.of(item1));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?diningCommonsCode=ortega"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(item1.getDiningCommonsCode());
                String expectedJson = mapper.writeValueAsString(item1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                UCSBDiningCommonsMenuItem item1 = UCSBDiningCommonsMenuItem.builder()
                                .diningCommonsCode("ortega")
                                .name("Tofu Banh Mi Sandwich (v)")
                                .station("Entree Specials")                               
                                .build();

                when(ucsbDiningCommonsMenuItemRepository.findById(item1.getDiningCommonsCode())).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?diningCommonsCode=ortega"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(item1.getDiningCommonsCode());
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDiningCommonsMenuItem with id ortega not found", json.get("message"));
        }


                // Tests for PUT /api/ucsbdinigcommonsmenuitem?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbdate() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem item1 = UCSBDiningCommonsMenuItem.builder()
                                .diningCommonsCode("ortega")
                                .name("Tofu Banh Mi Sandwich (v)")
                                .station("Entree Specials")                               
                                .build();

                UCSBDiningCommonsMenuItem item1edited = UCSBDiningCommonsMenuItem.builder()
                                .diningCommonsCode("ortega")
                                .name("Baked Pesto Pasta with Chicken")
                                .station("Entree Specials")                               
                                .build();

                String requestBody = mapper.writeValueAsString(item1edited);

                when(ucsbDiningCommonsMenuItemRepository.findById(item1.getDiningCommonsCode())).thenReturn(Optional.of(item1));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdiningcommonsmenuitem?diningCommonsCode=ortega")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(item1.getDiningCommonsCode());
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(item1edited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem item1edited = UCSBDiningCommonsMenuItem.builder()
                                .diningCommonsCode("ortega")
                                .name("Baked Pesto Pasta with Chicken")
                                .station("Entree Specials")                               
                                .build();


                String requestBody = mapper.writeValueAsString(item1edited);

                when(ucsbDiningCommonsMenuItemRepository.findById(item1edited.getDiningCommonsCode())).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdiningcommonsmenuitem?diningCommonsCode=ortega")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(item1edited.getDiningCommonsCode());
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id ortega not found", json.get("message"));

        }



}