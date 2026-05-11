package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.persistence.JdbcExhibitionDao;
import com.project.artconnect.persistence.JdbcGalleryDao;
import com.project.artconnect.service.GalleryService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcGalleryService implements GalleryService {

    private final GalleryDao galleryDao;
    private final ExhibitionDao exhibitionDao;

    public JdbcGalleryService() {
        this(new JdbcGalleryDao(), new JdbcExhibitionDao());
    }

    public JdbcGalleryService(GalleryDao galleryDao, ExhibitionDao exhibitionDao) {
        this.galleryDao = galleryDao;
        this.exhibitionDao = exhibitionDao;
    }

    @Override
    public List<Gallery> getAllGalleries() {
        List<Gallery> galleries = galleryDao.findAll();
        Map<String, Gallery> byName = new LinkedHashMap<>();
        for (Gallery gallery : galleries) {
            gallery.setExhibitions(new ArrayList<>());
            byName.put(gallery.getName(), gallery);
        }

        for (Exhibition exhibition : exhibitionDao.findAll()) {
            if (exhibition.getGallery() == null || exhibition.getGallery().getName() == null) {
                continue;
            }
            Gallery gallery = byName.get(exhibition.getGallery().getName());
            if (gallery != null) {
                gallery.addExhibition(exhibition);
            }
        }
        return galleries;
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return getAllGalleries().stream()
                .filter(gallery -> name.equalsIgnoreCase(gallery.getName()))
                .findFirst();
    }

    @Override
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        if (gallery == null || gallery.getName() == null) {
            return List.of();
        }
        return exhibitionDao.findAll().stream()
                .filter(exhibition -> exhibition.getGallery() != null
                        && gallery.getName().equalsIgnoreCase(exhibition.getGallery().getName()))
                .collect(Collectors.toList());
    }

    @Override
    public void createGallery(Gallery gallery) {
        galleryDao.save(gallery);
    }

    @Override
    public void updateGallery(Gallery gallery) {
        galleryDao.update(gallery);
    }

    @Override
    public void deleteGallery(String name) {
        galleryDao.delete(name);
    }
}
