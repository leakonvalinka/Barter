package at.ac.ase.inso.group02.images;

import java.util.UUID;

/**
 * service for retrieving images stored in the DB
 */
public interface ImageService {

    /**
     * retrieves image data by the image id
     *
     * @param id image id
     * @return a binary data stream of the image
     */
    byte[] getImageByUUID(UUID id);
}
