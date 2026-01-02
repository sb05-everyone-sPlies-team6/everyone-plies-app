package team6.finalproject.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "profile_image")
  private String profileImageUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(name = "locked", nullable = false)
  private Locked locked;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public User(String email, String password, String name) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.role = Role.USER;
    this.locked = Locked.Active;
    this.createdAt = LocalDateTime.now();
  }

  public void changePassword(String newPassword) {
    this.password = newPassword;
    this.updatedAt = LocalDateTime.now();
  }
}
