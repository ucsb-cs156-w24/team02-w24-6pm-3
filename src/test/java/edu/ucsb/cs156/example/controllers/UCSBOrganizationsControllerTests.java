package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {

        @MockBean
        UCSBOrganizationsRepository ucsbOrganizationsRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/ucsborganizations/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrganizationsRepository.findById(eq("ZPR"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=ZPR"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findById(eq("ZPR"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganizations with id ZPR not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

                // arrange

                UCSBOrganizations ZPR = UCSBOrganizations.builder()
                                .orgCode("ZPR")
                                .orgTranslationShort("ZETA PHI RHO")
                                .orgTranslation("ZETA PHI RHO")
                                .inactive(false)
                                .build();

                UCSBOrganizations SKY = UCSBOrganizations.builder()
                                .orgCode("SKY")
                                .orgTranslationShort("SKYDIVING CLUB")
                                .orgTranslation("SKYDIVING CLUB AT UCSB")
                                .inactive(false)
                                .build();

                ArrayList<UCSBOrganizations> expectedOrganization = new ArrayList<>();
                expectedOrganization.addAll(Arrays.asList(ZPR, SKY));

                when(ucsbOrganizationsRepository.findAll()).thenReturn(expectedOrganization);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrganization);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/ucsborganization...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_organization() throws Exception {
                // arrange

                UCSBOrganizations OSLI = UCSBOrganizations.builder()
                                .orgCode("OSLI")
                                .orgTranslationShort("STUDENT LIFE")
                                .orgTranslation("OFFICE OF STUDENT LIFE")
                                .inactive(false)
                                .build();

                when(ucsbOrganizationsRepository.save(eq(OSLI))).thenReturn(OSLI);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganization/post?orgCode=OSLI&orgTranslationShort=STUDENT LIFE&orgTranslation=OFFICE OF STUDENT LIFE&inactive=false")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).save(OSLI);
                String expectedJson = mapper.writeValueAsString(OSLI);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }


        // Tests for GET /api/ucsborganization?...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganization?orgCode=OSLI"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBOrganizations org = UCSBOrganizations.builder()
                                .orgCode("KRC")
                                .orgTranslationShort("KOREAN RADIO CL")
                                .orgTranslation("KOREAN RADIO CLUB")
                                .inactive(false)
                                .build();

                when(ucsbOrganizationsRepository.findById(eq("KRC"))).thenReturn(Optional.of(org));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=KRC"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationsRepository, times(1)).findById(eq("KRC"));
                String expectedJson = mapper.writeValueAsString(org);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for DELETE /api/ucsborganization?...

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                UCSBOrganizations ZPR = UCSBOrganizations.builder()
                                .orgCode("ZPR")
                                .orgTranslationShort("ZETA PHI RHO")
                                .orgTranslation("ZETA PHI RHO")
                                .inactive(false)
                                .build();

                when(ucsbOrganizationsRepository.findById(eq("ZPR"))).thenReturn(Optional.of(ZPR));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=ZPR")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("ZPR");
                verify(ucsbOrganizationsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id ZPR deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationsRepository.findById(eq("DNE"))).thenReturn(Optional.empty()); //DNE = does not exist

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=DNE")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("DNE");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id DNE not found", json.get("message"));
        }

        // Tests for PUT /api/ucsborganization?...

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_organization() throws Exception {
                // arrange

                UCSBOrganizations SKY_ORIG = UCSBOrganizations.builder()
                                .orgCode("SKY")
                                .orgTranslationShort("SKYDIVING CLUB")
                                .orgTranslation("SKYDIVING CLUB AT UCSB")
                                .inactive(false)
                                .build();

                UCSBOrganizations SKY_EDIT = UCSBOrganizations.builder()
                                .orgCode("SKYZ")
                                .orgTranslationShort("SKYDIVERS")
                                .orgTranslation("SKYDIVERS CLUB AT UCSB")
                                .inactive(false)
                                .build();

                String requestBody = mapper.writeValueAsString(SKY_EDIT);

                when(ucsbOrganizationsRepository.findById(eq("SKY"))).thenReturn(Optional.of(SKY_ORIG));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgCode=SKY")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("SKY");
                verify(ucsbOrganizationsRepository, times(1)).save(SKY_EDIT); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }


        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
                // arrange

                UCSBOrganizations editedOrg = UCSBOrganizations.builder()
                                .orgCode("DNE")
                                .orgTranslationShort("DOES NOT")
                                .orgTranslation("DOES NOT EXIST")
                                .inactive(false)
                                .build();

                String requestBody = mapper.writeValueAsString(editedOrg);

                when(ucsbOrganizationsRepository.findById(eq("DNE"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgCode=DNE")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationsRepository, times(1)).findById("DNE");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganizations with id DNE not found", json.get("message"));

        }
}
