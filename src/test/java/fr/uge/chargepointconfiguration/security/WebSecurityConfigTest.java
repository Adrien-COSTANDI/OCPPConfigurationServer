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
package fr.uge.chargepointconfiguration.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityConfigTest {

  @Autowired
  private MockMvc mvc;

  @Test
  @WithAnonymousUser
  void loginAllowed() throws Exception {
    mvc.perform(get("/about")).andDo(print()).andExpect(status().isOk());
  }

  @Test
  @WithAnonymousUser
  void unauthorizedApi() throws Exception {
    mvc.perform(get("/api")).andDo(print()).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRATOR")
  void authorizedApi() throws Exception {
    mvc.perform(get("/api/user/all")).andDo(print()).andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void notFound() throws Exception {
    mvc.perform(get("/notfound")).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void logoutPost() throws Exception {
    mvc.perform(post("/logout")).andDo(print()).andExpect(status().isNoContent());
  }

  @Test
  @WithAnonymousUser
  void logoutGet() throws Exception {
    mvc.perform(get("/logout")).andDo(print()).andExpect(status().isNoContent());
  }

  @Test
  @WithAnonymousUser
  void loginGet() throws Exception {
    mvc.perform(get("/")).andDo(print()).andExpect(status().isOk());
  }
}
