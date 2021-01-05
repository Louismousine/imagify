package ca.shopify.backendchallenge.service;

import ca.shopify.backendchallenge.dto.ImageDTO;
import ca.shopify.backendchallenge.exception.AmazonException;
import ca.shopify.backendchallenge.model.ImageEntity;
import ca.shopify.backendchallenge.model.User;
import ca.shopify.backendchallenge.repository.ImageRepository;
import ca.shopify.backendchallenge.repository.UserRepository;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

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

    public ImageDTO uploadImage(User user, MultipartFile file, boolean isPrivate, String tags) throws AmazonException {
        ImageEntity img = new ImageEntity();
        String link = this.amazonClient.uploadFile(file);
        img.setLabels(detectLabelsWithAmazonAI(link.split("/")[4]));
        img.setPicture(link);
        img.setOwner(user);
        img.setPrivate(isPrivate);
        img.setTags(tags);
        imageRepository.save(img);
        user.getImages().add(img);
        userRepository.save(user);
        return ImageDTO.convertImageToDTO(img);
    }

    private List<String> detectLabelsWithAmazonAI(String fileName){
        System.out.println(fileName);
        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
                .withRegion("us-east-2") // The first region to try your request against
                .build();;

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image()
                        .withS3Object(new S3Object()
                                .withName(fileName).withBucket("shopifybackendimage")))
                .withMaxLabels(3)
                .withMinConfidence(75F);

        try {
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List<Label> labels = result.getLabels();
            List<String> labelsList = new ArrayList<>();
            for(Label l: labels){
                labelsList.add(l.getName());
            }
            return labelsList;
        } catch(AmazonRekognitionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
