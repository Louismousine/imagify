package ca.shopify.backendchallenge.service;

import ca.shopify.backendchallenge.dto.ImageDTO;
import ca.shopify.backendchallenge.model.ImageEntity;
import ca.shopify.backendchallenge.model.User;
import ca.shopify.backendchallenge.repository.ImageRepository;
import ca.shopify.backendchallenge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AmazonClient amazonClient;

    /**
     * Deletes the requested images if they belong to the user.
     * @param ids
     */
    @Transactional
    public void deleteImages(Long[] ids, User user){
        String username  = user.getUserName();
        for(Long id: ids){
            ImageEntity image = imageRepository.findImageEntityById(id);
            if(image != null && image.getOwner().getUserName().equals(username)){
                this.amazonClient.deleteFileFromS3Bucket(image.getPicture());
                imageRepository.deleteById(id);
            }
        }
    }

    public ImageDTO uploadImage(User user, MultipartFile file, boolean isPrivate, String tags){
        ImageEntity img = new ImageEntity();
        String link = this.amazonClient.uploadFile(file);
        img.setPicture(link);
        img.setOwner(user);
        img.setPrivate(isPrivate);
        img.setTags(tags);
        imageRepository.save(img);
        user.getImages().add(img);
        userRepository.save(user);
        return ImageDTO.convertImageToDTO(img);
    }
}
