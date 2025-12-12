package com.codehows.ksisbe.setting.entity;

import com.codehows.ksisbe.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
//    @JsonBackReference
    private User user;

    @Column(name = "setting_name", nullable = false, length = 255)
    private String settingName;

    @Column(name = "url", columnDefinition = "TEXT", nullable = false)
    private String url;

    @Column(name = "type", length = 10, nullable = false)
    private String type;  // 단일/다중 타입

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "rate")
    private Integer rate;

    @Column(name = "list_area", columnDefinition = "TEXT")
    private String listArea;

    @Column(name = "paging_type", length = 20)
    private String pagingType;

    @Column(name = "paging_area", columnDefinition = "TEXT")
    private String pagingArea;

    @Column(name = "paging_nextbtn", columnDefinition = "TEXT")
    private String pagingNextbtn;

    @Column(name = "max_page")
    private Integer maxPage;

    @Column(name = "link_area", columnDefinition = "TEXT")
    private String linkArea;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "is_delete", length = 1, nullable = false)
    @Builder.Default
    private String isDelete = "N";

    @OneToMany(mappedBy = "setting", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonManagedReference
    private List<Conditions> conditions = new ArrayList<>();
}
