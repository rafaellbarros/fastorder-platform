package br.com.rafaellbarros.user.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity representing system users
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email", name = "uk_users_email")
        },
        indexes = {
                @Index(columnList = "email", name = "idx_users_email"),
                @Index(columnList = "active", name = "idx_users_active"),
                @Index(columnList = "createdAt", name = "idx_users_created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    /**
     * Deactivates the user
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Reactivates the user
     */
    public void reactivate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if user is active
     *
     * @return true if active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

}
