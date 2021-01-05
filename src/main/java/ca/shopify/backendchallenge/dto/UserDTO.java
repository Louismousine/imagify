package ca.shopify.backendchallenge.dto;

import ca.shopify.backendchallenge.model.ImageEntity;
import ca.shopify.backendchallenge.model.User;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserDTO {
    @NotNull
    private String username;

    private String password;

    private Set<ImageDTO> images;

    private Set<ImageDTO> likedImages;

    private String ApiToken;

    public UserDTO(User user) {
        this.ApiToken = user.getApiToken();
        this.username = user.getUserName();
        this.images = new HashSet<>();
        this.likedImages = new HashSet<>();
        if (user.getImages() != null) {
			for (ImageEntity image : user.getImages()) {
				images.add(ImageDTO.convertImageToDTO(image));
			}
		}
        if (user.getLikedImages() != null) {
			for (ImageEntity image : user.getLikedImages()) {
				likedImages.add(ImageDTO.convertImageToDTO(image));
			}
		}
    }

    public UserDTO() {
    }
}
