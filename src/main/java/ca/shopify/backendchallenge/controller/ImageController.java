package ca.shopify.backendchallenge.controller;

import ca.shopify.backendchallenge.dto.ImageDTO;
import ca.shopify.backendchallenge.exception.RequestException;
import ca.shopify.backendchallenge.exception.TokenException;
import ca.shopify.backendchallenge.model.ImageEntity;
import ca.shopify.backendchallenge.model.User;
import ca.shopify.backendchallenge.repository.ImageRepository;
import ca.shopify.backendchallenge.repository.UserRepository;
import ca.shopify.backendchallenge.service.ImageService;
import ca.shopify.backendchallenge.service.UserService;
import com.sipios.springsearch.anotation.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

// Class that handles the deletion and addition of images to the repository.

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/image")
public class ImageController {

    // Declaration of needed services.
    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    // Deletes the specified images if the user making the request is the image's owner.
    @DeleteMapping("")
    public ResponseEntity<?> deleteByIds(@RequestHeader String token, @RequestParam Long[] ids) throws TokenException {
        User user = userService.validateApiToken(token);
        imageService.deleteImages(ids, user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Upload a single image. The tags are a single string with comma separated values.
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("imageFile") MultipartFile file, @RequestHeader String token, @RequestParam boolean isPrivate, @RequestParam String tags) throws IOException {
        try {
            if (file.getContentType() != null && (file.getContentType().contains("jpg") || file.getContentType().contains("png"))) {
                User user = userService.validateApiToken(token);
                ImageDTO image = imageService.uploadImage(user, file, isPrivate, tags);
                return new ResponseEntity<>(image, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
        } catch (TokenException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    // Get an image by its id.
    @GetMapping(path = {"/{imageId}"})
    public ImageDTO getImage(@PathVariable("imageId") Long id, @RequestHeader String token) throws TokenException, RequestException {
        User user = userService.validateApiToken(token);
        final Optional<ImageEntity> retrievedImage = imageRepository.findById(id);
        if (retrievedImage.isPresent()) {
            ImageEntity image = retrievedImage.get();
            if (!image.isPrivate() || user.getUserName().equals(image.getOwner().getUserName())) {
                ImageDTO img = ImageDTO.convertImageToDTO(image);
                return img;
            } else {
                throw new RequestException("Image is private");
            }
        }
        throw new RequestException("No image has this id");
    }

    // Get all of a user's images that are public (or all if the requester is the searched user)
    @GetMapping(path = {"/user/{username}"})
    public List<ImageDTO> getUserImages(@PathVariable("username") String username, @RequestHeader String token) throws TokenException, RequestException {
        User requester = userService.validateApiToken(token);
        User user = userRepository.findUserByUserName(username);
        if (user == null) {
            throw new RequestException("No such user");
        }
        List<ImageDTO> images = new ArrayList<>();
        if (user.getImages() != null) {
            for (ImageEntity image : user.getImages()) {
                if (!image.isPrivate() || requester.getUserName().equals(image.getOwner().getUserName())) {
                    images.add(ImageDTO.convertImageToDTO(image));
                }
            }
        }
        return images;
    }

    // Gets all images that are not private, or are private and are the requester's
    @GetMapping("")
    public List<ImageDTO> getAllImages(@RequestHeader String token) throws TokenException {
        User user = userService.validateApiToken(token);
        Iterable<ImageEntity> iterator = imageRepository.findAll();
        List<ImageDTO> images = new ArrayList<>();
        iterator.forEach(e -> {
            if (!e.isPrivate() || user.getUserName().equals(e.getOwner().getUserName())) {
                images.add(ImageDTO.convertImageToDTO(e));
            }
        });
        return images;
    }

    // Search images by keyword.
    @GetMapping("/find")
    public ResponseEntity<List<ImageDTO>> searchByTags(@SearchSpec Specification<ImageEntity> specs, @RequestHeader String token) throws TokenException {
        User user = userService.validateApiToken(token);
        List<ImageEntity> images = imageRepository.findAll(Specification.where(specs));
        List<ImageDTO> imagesToReturn = new ArrayList<>();
        for (ImageEntity e : images) {
            if (!e.isPrivate() || user.getUserName().equals(e.getOwner().getUserName())) {
                ImageDTO image = ImageDTO.convertImageToDTO(e);
                imagesToReturn.add(image);
            }
        }
        return new ResponseEntity<>(imagesToReturn, HttpStatus.OK);
    }

    // Edit tags.
    @PutMapping("/tags/{id}")
    public ResponseEntity<?> editTags(@RequestHeader String token, @PathVariable("id") Long id, @RequestParam String tags) throws TokenException {
        User user = userService.validateApiToken(token);
        ImageEntity ie = imageRepository.findImageEntityById(id);
        if (ie != null && ie.getOwner().getUserName().equals(user.getUserName())) {
            ie.setTags(tags);
            imageRepository.save(ie);
            return new ResponseEntity<>(ImageDTO.convertImageToDTO(ie), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Make the image private or public.
    @PutMapping("/private/{id}")
    public ResponseEntity<?> makePrivate(@RequestHeader String token, @PathVariable("id") Long id) throws TokenException {
        User user = userService.validateApiToken(token);
        ImageEntity ie = imageRepository.findImageEntityById(id);
        if (ie != null && ie.getOwner().getUserName().equals(user.getUserName())) {
            ie.setPrivate(!ie.isPrivate());
            imageRepository.save(ie);
            return new ResponseEntity<>(ImageDTO.convertImageToDTO(ie), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Like picture / unlike picture
    @PostMapping("/like/{id}")
    public ResponseEntity<?> likePicture(@RequestHeader String token, @PathVariable("id") Long id) throws TokenException {
        User user = userService.validateApiToken(token);
        ImageEntity ie = imageRepository.findImageEntityById(id);
        if (ie != null) {
            Set<ImageEntity> list = user.getLikedImages();
            Set<User> likers = ie.getLikers();
            if (likers == null) {
                likers = new HashSet<>();
            }
            if (list.contains(ie)) {
                likers.remove(user);
                ie.setLikers(likers);
                list.remove(ie);
                user.setLikedImages(list);
            } else {
                likers.add(user);
                ie.setLikers(likers);
                list.add(ie);
                user.setLikedImages(list);
            }
            userRepository.save(user);
            imageRepository.save(ie);
            return new ResponseEntity<>(ImageDTO.convertImageToDTO(ie), HttpStatus.OK);
        }
        return new ResponseEntity<>("No image has this id", HttpStatus.BAD_REQUEST);
    }


}
