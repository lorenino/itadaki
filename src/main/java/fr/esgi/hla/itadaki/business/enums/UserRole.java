package fr.esgi.hla.itadaki.business.enums;

/**
 * TODO: Defines the roles a user can have in the system.
 *       - USER  : standard authenticated user
 *       - ADMIN : administrator with elevated privileges
 *       Will be stored as a String column on the User entity.
 */
public enum UserRole {
    USER,
    ADMIN
}
