package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle user profile endpoints.
 *       - GET  api/users/me       → return authenticated user's profile
 *       - PUT  api/users/me       → update authenticated user's profile
 *       - GET  api/users/{id}     → admin: get any user (admin-only)
 *       Use @CurrentUser to resolve the authenticated principal.
 *       Inject UserService and return UserResponseDto.
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/users")
@Tag(name = "Users", description = "User profile endpoints")
public class UserController {

    private UserService userService;

    // TODO: @Operation(summary = "Get authenticated user profile") GET /me → UserResponseDto
    // TODO: @Operation(summary = "Update authenticated user profile") PUT /me → UserResponseDto
}
