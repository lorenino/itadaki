package fr.esgi.hla.itadaki.business.enums;

/**
 * Defines the roles a user can have in the system.
 * Stored as a STRING column on the User entity (@Enumerated(EnumType.STRING)).
 */
public enum UserRole {
    USER,
    ADMIN
}
