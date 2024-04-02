/*
 * The MIT License
 * Copyright © 2024 LastProject-ESIEE
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.uge.chargepointconfiguration.firmware;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FirmwareControllerTest {
  @Autowired
  private MockMvc mvc;

  @Test
  @Disabled
  void getAllFirmwares() {}

  @Test
  @Disabled
  void getFirmwareById() {}

  @Test
  @WithMockUser(roles = "EDITOR")
  void getPage() throws Exception {
    mvc.perform(get("/api/firmware/search")
            .queryParam("size", "2")
            .queryParam("page", "0")
            .queryParam("sortBy", "version")
            .queryParam("order", "desc"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.total", is(3)))
        .andExpect(jsonPath("$.totalElement", is(3)))
        .andExpect(jsonPath("$.page", is(0)))
        .andExpect(jsonPath("$.size", is(2)))
        .andExpect(jsonPath("$.data", hasSize(2)))
        .andExpect(jsonPath("$.data[0].id", is(3)))
        .andExpect(jsonPath("$.data[1].id", is(1)));

    mvc.perform(get("/api/firmware/search")
            .queryParam("size", "2")
            .queryParam("page", "1")
            .queryParam("sortBy", "version")
            .queryParam("order", "desc"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.total", is(3)))
        .andExpect(jsonPath("$.totalElement", is(3)))
        .andExpect(jsonPath("$.page", is(1)))
        .andExpect(jsonPath("$.size", is(2)))
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id", is(2)));
  }

  @Test
  @WithMockUser(roles = "EDITOR")
  void getPageWithFilterMultiple() throws Exception {
    mvc.perform(get("/api/firmware/search")
            .queryParam("size", "2")
            .queryParam("page", "0")
            .queryParam("sortBy", "version")
            .queryParam("order", "desc")
            .queryParam("request", "version>`6`,url:`Firmware3`"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.total", is(1)))
        .andExpect(jsonPath("$.totalElement", is(3)))
        .andExpect(jsonPath("$.page", is(0)))
        .andExpect(jsonPath("$.size", is(2)))
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id", is(3)));
  }

  @Test
  @WithMockUser(roles = "EDITOR")
  void getPageWithFilter() throws Exception {
    mvc.perform(get("/api/firmware/search")
            .queryParam("size", "2")
            .queryParam("page", "0")
            .queryParam("sortBy", "version")
            .queryParam("order", "desc")
            .queryParam("request", "version>`6`"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.total", is(2)))
        .andExpect(jsonPath("$.totalElement", is(3)))
        .andExpect(jsonPath("$.page", is(0)))
        .andExpect(jsonPath("$.size", is(2)))
        .andExpect(jsonPath("$.data", hasSize(2)))
        .andExpect(jsonPath("$.data[0].id", is(3)))
        .andExpect(jsonPath("$.data[1].id", is(1)));
  }
}
