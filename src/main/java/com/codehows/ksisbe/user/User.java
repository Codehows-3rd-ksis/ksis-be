package com.codehows.ksisbe.user;

import com.codehows.ksisbe.setting.entity.Setting;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "role", nullable = false, length = 20)
    private String role; // e.g. "ROLE_ADMIN" or "ROLE_USER"

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "dept", length = 100)
    private String dept;

    @Column(name = "ranks", length = 50)
    private String ranks;

    @Column(name = "state", length = 20)
    private String state;

    @Column(name = "login_at")
    private LocalDateTime loginAt;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "is_delete", length = 1, nullable = false)
    private String isDelete;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Setting> settings = new ArrayList<>();
}
