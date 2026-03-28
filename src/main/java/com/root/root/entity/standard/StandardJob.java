package com.root.root.entity.standard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "standard_job")
public class StandardJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hope;

    @OneToMany(mappedBy = "standardJob", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StandardJobRequirement> requirements = new ArrayList<>();
}
