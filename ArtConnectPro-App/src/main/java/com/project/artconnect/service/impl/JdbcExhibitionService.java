package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.persistence.JdbcExhibitionDao;
import com.project.artconnect.service.ExhibitionService;
import java.util.List;
import java.util.Optional;

public class JdbcExhibitionService implements ExhibitionService {

    private final ExhibitionDao exhibitionDao;

    public JdbcExhibitionService() {
        this(new JdbcExhibitionDao());
    }

    public JdbcExhibitionService(ExhibitionDao exhibitionDao) {
        this.exhibitionDao = exhibitionDao;
    }

    @Override
    public List<Exhibition> getAllExhibitions() {
        return exhibitionDao.findAll();
    }

    @Override
    public Optional<Exhibition> getExhibitionByTitle(String title) {
        if (title == null) {
            return Optional.empty();
        }
        return exhibitionDao.findAll().stream()
                .filter(exhibition -> title.equalsIgnoreCase(exhibition.getTitle()))
                .findFirst();
    }

    @Override
    public void createExhibition(Exhibition exhibition) {
        exhibitionDao.save(exhibition);
    }

    @Override
    public void updateExhibition(Exhibition exhibition) {
        exhibitionDao.update(exhibition);
    }

    @Override
    public void deleteExhibition(String title) {
        exhibitionDao.delete(title);
    }
}
