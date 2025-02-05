package at.ac.ase.inso.group02.images.impl;

import at.ac.ase.inso.group02.images.ImageController;
import at.ac.ase.inso.group02.images.ImageService;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class ImageControllerImpl implements ImageController {

    ImageService imageService;

    @Override
    public byte[] getImage(UUID id) {
        Log.infov("Fetching image {0}", id);
        return imageService.getImageByUUID(id);
    }
}
