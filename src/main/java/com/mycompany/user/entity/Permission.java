package com.mycompany.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "permission")

public class Permission  {
    @Id
    @Column(name = "id", unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", unique = true)
    private String url;

    @OneToMany(mappedBy = "permission", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<RolePermission> rolePermissionList;

    @Override
    public String toString() {
        return "Permission{id=" + id + ", url='" + url + "'}";
    }
}
