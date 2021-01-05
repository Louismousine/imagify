package ca.shopify.backendchallenge.repository;

import ca.shopify.backendchallenge.model.ImageEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Louis
 *
 */
@Repository
public interface ImageRepository extends CrudRepository<ImageEntity, Long>, JpaSpecificationExecutor<ImageEntity> {

    ImageEntity findImageEntityById(Long id);

    void deleteById(Long id);

    void deleteAll();

}
