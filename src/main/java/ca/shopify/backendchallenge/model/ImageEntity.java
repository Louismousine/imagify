package ca.shopify.backendchallenge.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Entity

public class ImageEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @EqualsAndHashCode.Exclude
    @ManyToOne(cascade = CascadeType.DETACH)
    private User owner;

    private boolean isPrivate;

    private String picture;

    // the list of tags to search - one string with words separated by commas
    private String tags;

    // the list of lbaels that the Amazon Rekognition API has detected
    @ElementCollection
    private List<String> labels;

    @EqualsAndHashCode.Exclude
    @ManyToMany(cascade = CascadeType.DETACH)
    private Set<User> likers;

    @PreRemove
    private void removeImageFromHolders(){
        owner.getImages().remove(this);
        for(User liker: likers){
            liker.getLikedImages().remove(this);
        }
    }

}
