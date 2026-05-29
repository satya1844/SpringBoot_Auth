package com.vinay.AuthService.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	private String description;

	@Builder.Default
	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	// If you want a relation to User, add the mapping in both entities.
	// Example (uncomment and update User.java accordingly):
	// @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
	// private List<User> users = new ArrayList<>();

}
//cerate tehe department entity class with id and name fields and annotate it with @Entity and @Table(name = "departments") annotations. Also, add the necessary getters, setters, constructors, and builder pattern using Lombok annotations.
