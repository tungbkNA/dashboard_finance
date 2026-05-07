package com.internal.projectmgmt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    public enum PermissionType {
        SCREEN, ACTION
    }

    @Id
    @Column(name = "code", length = 100)
    private String code;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "parent_code", length = 100)
    private String parentCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PermissionType type;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}
