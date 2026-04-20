USE ArtConnect;

INSERT INTO Artist VALUES
(1, 'Lea Moreau', 1990, 'lea.moreau@artconnect.com', 'Peintre contemporaine travaillant sur la memoire urbaine.', '0611223344', 'Paris', 'https://leamoreau.art', '@lea.moreau.art', TRUE),
(2, 'Karim Benali', 1985, 'karim.benali@artconnect.com', 'Sculpteur specialise dans les materiaux recycles.', '0622334455', 'Marseille', 'https://karimbenali.art', '@karim_benali', TRUE),
(3, 'Sofia Nguyen', 1994, 'sofia.nguyen@artconnect.com', 'Artiste numerique explorant les interactions homme-machine.', '0633445566', 'Lyon', 'https://sofianguyen.art', '@sofia.nguyen.studio', TRUE),
(4, 'Thomas Diallo', 1988, 'thomas.diallo@artconnect.com', 'Photographe documentaliste des espaces urbains et sociaux.', '0644556677', 'Lille', 'https://thomasdiallo.art', '@thomasdiallo.photo', TRUE);

INSERT INTO Discipline VALUES
('Painting'),
('Sculpture'),
('Digital Art'),
('Photography'),
('Mixed Media');

INSERT INTO Artist_Discipline VALUES
(1, 'Painting'),
(1, 'Mixed Media'),
(2, 'Sculpture'),
(2, 'Mixed Media'),
(3, 'Digital Art'),
(3, 'Photography'),
(4, 'Photography');

INSERT INTO Artwork VALUES
(1, 'Fragments de Ville', 2023, 'Painting', 'Acrylic on canvas', '120x90 cm', 1800.00, 'FOR_SALE', 'Une serie inspiree des traces laissees par les passants dans la ville.', 1),
(2, 'Murmures Recycles', 2024, 'Sculpture', 'Metal and recycled plastic', '80x60x50 cm', 2500.00, 'EXHIBITED', 'Sculpture realisee a partir de materiaux industriels reemployes.', 2),
(3, 'Lignes d Horizon', 2022, 'Photography', 'Fine art print', '70x100 cm', 950.00, 'SOLD', 'Photographie grand format sur la transformation du paysage urbain.', 4),
(4, 'Pulse 404', 2025, 'Installation', 'Screens, sensors, code', '300x200 cm', 4200.00, 'EXHIBITED', 'Installation interactive sur la surcharge numerique et l attention humaine.', 3),
(5, 'Silence Bleu', 2021, 'Painting', 'Oil on canvas', '100x80 cm', 1600.00, 'FOR_SALE', 'Composition minimaliste autour du silence et de la profondeur.', 1),
(6, 'Transit Humain', 2024, 'Digital Collage', 'Archival print', '60x80 cm', 1200.00, 'FOR_SALE', 'Collage numerique sur les flux de circulation et les gestes repetitifs.', 3),
(7, 'Racines Numeriques', 2025, 'Mixed Media', 'Wood, LED, code', '150x150 cm', 3900.00, 'EXHIBITED', 'Oeuvre hybride questionnant les rapports entre nature et interfaces.', 3);

INSERT INTO ArtworkTag VALUES
('urban'),
('eco'),
('abstract'),
('immersive'),
('recycled'),
('portrait'),
('interactive');

INSERT INTO Artwork_ArtworkTag VALUES
(1, 'urban'),
(1, 'abstract'),
(2, 'eco'),
(2, 'recycled'),
(3, 'portrait'),
(4, 'immersive'),
(4, 'interactive'),
(5, 'abstract'),
(6, 'urban'),
(7, 'eco'),
(7, 'interactive');

INSERT INTO Gallery VALUES
(1, 'Galerie Lumiere', '12 rue des Arts, Paris', 'Claire Dumas', 'Tue-Sat 10:00-19:00', '0144556677', 4.6, 'https://galerielumiere.fr'),
(2, 'Espace Horizon', '45 avenue du Port, Lyon', 'Marc Revers', 'Wed-Sun 11:00-18:30', '0472001122', 4.4, 'https://espacehorizon.fr'),
(3, 'Studio Seine', '8 quai des Createurs, Lille', 'Ines Martin', 'Tue-Sun 09:30-18:00', '0322557788', 4.7, 'https://studioseine.fr');

INSERT INTO Exhibition VALUES
(1, 'Repenser la Matiere', '2026-05-10', '2026-07-01', 'Exposition autour du recyclage, du detournement et de la durabilite.', 'Claire Dupont', 'Sustainability', 1),
(2, 'Pixels et Memoires', '2026-06-15', '2026-08-30', 'Dialogue entre pratiques numeriques et memoires personnelles.', 'Marc Nguyen', 'Digital identities', 2),
(3, 'Regards Urbains', '2026-09-01', '2026-10-15', 'Regards croises sur la ville, ses rythmes et ses habitants.', 'Ines Martin', 'City and movement', 1),
(4, 'Intimites Lumineuses', '2026-11-05', '2026-12-12', 'Parcours autour de la lumiere, du retrait et de la contemplation.', 'Sarah El Fassi', 'Light and silence', 3);

INSERT INTO Artwork_Exhibition VALUES
(2, 1),
(7, 1),
(4, 2),
(6, 2),
(7, 2),
(1, 3),
(3, 3),
(6, 3),
(5, 4),
(3, 4);

INSERT INTO CommunityMember VALUES
(1, 'Emma Laurent', 'emma.laurent@mail.com', 2001, '0677001100', 'Paris', 'student'),
(2, 'Lucas Bernard', 'lucas.bernard@mail.com', 1998, '0677002200', 'Lyon', 'premium'),
(3, 'Nina Chen', 'nina.chen@mail.com', 2000, '0677003300', 'Paris', 'standard'),
(4, 'Mehdi Ait Ali', 'mehdi.aitali@mail.com', 1997, '0677004400', 'Lille', 'artist_supporter'),
(5, 'Clara Rossi', 'clara.rossi@mail.com', 2002, '0677005500', 'Bordeaux', 'student');

INSERT INTO CommunityMember_Discipline VALUES
(1, 'Painting'),
(1, 'Mixed Media'),
(2, 'Photography'),
(3, 'Digital Art'),
(3, 'Photography'),
(4, 'Sculpture'),
(5, 'Painting');

INSERT INTO Review VALUES
(1, 5, 'Une oeuvre tres forte, le message ecologique est clair sans etre simpliste.', '2026-05-20', 2, 1),
(2, 4, 'Installation impressionnante, tres immersive.', '2026-06-20', 4, 2),
(3, 5, 'J ai adore l interaction entre les ecrans et les capteurs.', '2026-06-22', 4, 3),
(4, 3, 'Belle technique mais j aurais aime plus de contraste dans la composition.', '2026-09-05', 1, 4),
(5, 4, 'Le melange nature et numerique fonctionne tres bien.', '2026-07-02', 7, 2),
(6, 5, 'Une photo tres sensible, tres bien imprimee.', '2026-09-12', 3, 5),
(7, 4, 'Sujet tres actuel et execution propre.', '2026-09-08', 6, 1);

INSERT INTO Workshop VALUES
(1, 'Initiation a la peinture texturee', '2026-06-22 14:00:00', 120, 12, 35.00, 'Paris - Salle Atelier A', 'Atelier de decouverte des textures, matieres et couches en peinture.', 'beginner', 1),
(2, 'Photographie urbaine de nuit', '2026-07-05 18:30:00', 180, 10, 45.00, 'Lyon - Depart Espace Horizon', 'Sortie et pratique guidee autour de la lumiere artificielle et des cadrages urbains.', 'intermediate', 4),
(3, 'Creer une installation interactive', '2026-07-18 10:00:00', 240, 8, 60.00, 'Paris - Lab Numerique', 'Introduction aux capteurs, ecrans et dispositifs artistiques interactifs.', 'advanced', 3),
(4, 'Sculpture a partir de materiaux recycles', '2026-06-28 15:00:00', 150, 10, 40.00, 'Paris - Fabrique 12', 'Atelier de creation avec metal, plastique et objets recuperes.', 'beginner', 2);

INSERT INTO Booking VALUES
(1, 1, 'paid', '2026-06-01 09:30:00'),
(1, 3, 'paid', '2026-06-03 11:10:00'),
(1, 5, 'pending', '2026-06-04 16:45:00'),
(2, 2, 'paid', '2026-06-10 14:00:00'),
(2, 4, 'cancelled', '2026-06-11 08:50:00'),
(3, 2, 'paid', '2026-06-20 19:20:00'),
(3, 3, 'paid', '2026-06-21 10:15:00'),
(4, 1, 'paid', '2026-06-06 12:30:00'),
(4, 4, 'pending', '2026-06-07 17:40:00');