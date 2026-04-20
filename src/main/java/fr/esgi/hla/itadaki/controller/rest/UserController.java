package fr.esgi.hla.itadaki.controller.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle user profile endpoints.
 *       - GET  /api/users/me       → return authenticated user's profile
 *       - PUT  /api/users/me       → update authenticated user's profile
 *       - GET  /api/users/{id}     → admin: get any user (admin-only)
 *       Use @CurrentUser to resolve the authenticated principal.
 *       Inject UserService and return UserResponseDto.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    // TODO: Inject UserService
    // TODO: Implement GET /me endpoint
    // TODO: Implement PUT /me endpoint
}
