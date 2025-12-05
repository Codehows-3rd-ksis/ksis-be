package com.codehows.ksisbe.crawling.entity;

import com.codehows.ksisbe.scheduler.entity.Scheduler;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crawl_work")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_id")
    private Long workId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", nullable = false)
    private Setting setting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Scheduler scheduler;  // NULL 허용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_by")
    private User startedBy;

    @Column(name = "type", length = 10, nullable = false)
    private String type;

    @Column(name = "state", length = 50, nullable = false)
    private String state;

    @Column(name = "fail_count", nullable = false)
    private Integer failCount;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "collect_count", nullable = false)
    private Integer collectCount;

    @Column(name = "expect_end_at")
    private LocalDateTime expectEndAt;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "is_delete", length = 1, nullable = false)
    private String isDelete;

    @OneToMany(mappedBy = "crawlWork", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrawlResultItem> crawlResultItems = new ArrayList<>();
}