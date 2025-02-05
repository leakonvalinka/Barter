package at.ac.ase.inso.group02.images;

import at.ac.ase.inso.group02.entities.Image;

import java.util.UUID;

/**
 * repository for storing images
 */
public interface ImageRepository {

    /**
     * finds an image by its given id
     *
     * @param id image id
     * @return an Image entity with that id, null if none exists
     */
    Image getImageById(UUID id);

    /**
     * persists a new image into the DB (and sets its id)
     *
     * @param image new image, possibly without an id
     */
    void saveImage(Image image);

    /**
     * deletes an existing image from the DB
     *
     * @param oldImage image to delete
     */
    void removeImage(Image oldImage);
}
