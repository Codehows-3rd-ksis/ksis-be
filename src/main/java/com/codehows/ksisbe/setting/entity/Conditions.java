package com.codehows.ksisbe.setting.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conditions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conditions_id")
    private Long conditionsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", nullable = false)
//    @JsonBackReference
    private Setting setting;

    @Column(name = "conditions_key", length = 20, nullable = false)
    private String conditionsKey;

    @Column(name = "conditions_value", length = 255, nullable = false)
    private String conditionsValue;

    @Column(name = "attr", length = 20)
    private String attr;
}
