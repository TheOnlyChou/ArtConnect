package com.project.artconnect.service.impl;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.service.ExhibitionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryExhibitionService implements ExhibitionService {
    private final List<Exhibition> exhibitions = new ArrayList<>();

    public InMemoryExhibitionService() {
        // minimal seed data can be added if desired; keep empty for safety
    }

    @Override
    public List<Exhibition> getAllExhibitions() {
        return new ArrayList<>(exhibitions);
    }

    @Override
    public Optional<Exhibition> getExhibitionByTitle(String title) {
        return exhibitions.stream().filter(e -> e.getTitle() != null && e.getTitle().equals(title)).findFirst();
    }

    @Override
    public void createExhibition(Exhibition exhibition) {
        exhibitions.add(exhibition);
    }

    @Override
    public void updateExhibition(Exhibition exhibition) {
        this.deleteExhibition(exhibition.getTitle());
        this.createExhibition(exhibition);
    }

    @Override
    public void deleteExhibition(String title) {
        exhibitions.removeIf(e -> e.getTitle() != null && e.getTitle().equals(title));
    }
}
