package ca.shopify.backendchallenge.dto;

import ca.shopify.backendchallenge.model.ImageEntity;
import lombok.Data;

@Data
public class ImageDTO {
    private long id;
    private String picture;
    private String ownerUsername;
    private  boolean isPrivate;
    // the list of tags to search
    private String[] tags;
    private int numberOfLikes;

    // converts an image to an image DTO
    public static ImageDTO convertImageToDTO(ImageEntity img){
        ImageDTO imgDto = new ImageDTO();
        imgDto.setPicture(img.getPicture());
        imgDto.setId(img.getId());
        imgDto.setOwnerUsername(img.getOwner().getUserName());
        imgDto.setPrivate(img.isPrivate());
        imgDto.setTags(img.getTags().split(","));
        if(img.getLikers()!=null)
        imgDto.setNumberOfLikes(img.getLikers().size());
        return imgDto;
    }
}
