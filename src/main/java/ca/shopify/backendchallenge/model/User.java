package ca.shopify.backendchallenge.model;

import ca.shopify.backendchallenge.configuration.ValidPassword;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Data
@Table(name = "users")
public class User {
	@Id
	@NotNull
	private String userName;

	@EqualsAndHashCode.Exclude
	@OneToMany(orphanRemoval = true, cascade = CascadeType.PERSIST)
	private Set<ImageEntity> images;

	@EqualsAndHashCode.Exclude
	@OneToMany(orphanRemoval = true, cascade = CascadeType.PERSIST)
	private Set<ImageEntity> likedImages;

	@ValidPassword
	private String password;

	private String apiToken;
}
