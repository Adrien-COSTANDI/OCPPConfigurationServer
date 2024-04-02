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
package fr.uge.chargepointconfiguration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Component;

/**
 * Package visibility, just for testing purpose.
 */
@Component
class FakeController {

  @PreAuthorize("isAuthenticated()") // TODO : should not be necessary
  String authenticated() {
    return "authenticated api";
  }

  @PreAuthorize("hasRole('ADMINISTRATOR')")
  String admin() {
    return "Administrator api";
  }

  @PreAuthorize("hasRole('EDITOR')")
  String editor() {
    return "Editor api";
  }

  @PreAuthorize("hasRole('VISUALIZER')")
  String visualizer() {
    return "Visualizer api";
  }
}

@SpringBootTest
// @RunWith(SpringRunner.class)
public class PermissionTest {

  @Autowired
  private FakeController fakeController;

  @Test
  @WithAnonymousUser
  void notAuthenticated() {
    assertThrows(AccessDeniedException.class, () -> fakeController.authenticated());
  }

  @Test
  @WithMockUser
  void authenticated() {
    assertDoesNotThrow(() -> fakeController.authenticated());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRATOR")
  void authenticatedWithAdminRole() {
    assertDoesNotThrow(() -> fakeController.authenticated());
  }

  @Test
  @WithMockUser(roles = "RANDOM")
  void adminWithRandomRole() {
    assertThrows(AccessDeniedException.class, () -> fakeController.admin());
  }

  @Test
  @WithMockUser(roles = "VISUALIZER")
  void adminWithUserRole() {
    assertThrows(AccessDeniedException.class, () -> fakeController.admin());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRATOR")
  void adminWithAdministratorRole() {
    assertDoesNotThrow(() -> fakeController.admin());
  }

  @Test
  @WithMockUser(roles = "RANDOM")
  void editorWithRandomRole() {
    assertThrows(AccessDeniedException.class, () -> fakeController.editor());
  }

  @Test
  @WithMockUser(roles = "EDITOR")
  void editorWithEditorRole() {
    assertDoesNotThrow(() -> fakeController.editor());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRATOR")
  void editorWithAdministratorRole() {
    assertDoesNotThrow(() -> fakeController.editor());
  }

  @Test
  @WithMockUser(roles = "RANDOM")
  void visualizerWithRandomRole() {
    assertThrows(AccessDeniedException.class, () -> fakeController.visualizer());
  }

  @Test
  @WithMockUser(roles = "EDITOR")
  void visualizerWithEditorRole() {
    assertDoesNotThrow(() -> fakeController.visualizer());
  }

  @Test
  @WithMockUser(roles = "VISUALIZER")
  void visualizerWithVisualizerRole() {
    assertDoesNotThrow(() -> fakeController.visualizer());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRATOR")
  void visualizerWithAdministratorRole() {
    assertDoesNotThrow(() -> fakeController.visualizer());
  }
}
