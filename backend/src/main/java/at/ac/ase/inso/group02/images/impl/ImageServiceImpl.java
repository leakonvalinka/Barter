package at.ac.ase.inso.group02.images.impl;

import at.ac.ase.inso.group02.entities.Image;
import at.ac.ase.inso.group02.images.ImageRepository;
import at.ac.ase.inso.group02.images.ImageService;
import at.ac.ase.inso.group02.images.exceptions.ImageDataRetrievalError;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

import java.util.UUID;

@ApplicationScoped
@AllArgsConstructor
public class ImageServiceImpl implements ImageService {

    ImageRepository imageRepository;

    @Override
    @Transactional
    public byte[] getImageByUUID(UUID id) {
        Image image = imageRepository.getImageById(id);

        if (image == null) {
            throw new NotFoundException("Image with id " + id + " not found");
        }
        byte[] data;
        try {
            data = image.getData();
        } catch (Exception e) {
            throw new ImageDataRetrievalError("Could not retrieve image with id " + id, e);
        }
        return data;
    }
}
