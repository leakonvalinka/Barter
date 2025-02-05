package at.ac.ase.inso.group02.images.impl;

import at.ac.ase.inso.group02.entities.Image;
import at.ac.ase.inso.group02.images.ImageRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/**
 * implementation of ImageRepository simply saving the entities in our PostgreSQL database as blob
 */
@ApplicationScoped
public class ImageRepositoryPostgresBlobImpl implements ImageRepository, PanacheRepositoryBase<Image, UUID> {
    @Override
    public Image getImageById(UUID id) {
        return findById(id);
    }

    @Override
    public void saveImage(Image image) {
        persistAndFlush(image);
    }

    @Override
    public void removeImage(Image oldImage) {
        delete(oldImage);
    }
}
