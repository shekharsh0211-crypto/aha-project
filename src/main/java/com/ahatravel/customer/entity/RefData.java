package com.ahatravel.customer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ref_data", indexes = {
        @Index(name = "idx_refdata_category", columnList = "category"),
        @Index(name = "idx_refdata_code", columnList = "category, code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "label", nullable = false, length = 200)
    private String label;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
