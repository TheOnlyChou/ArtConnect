CREATE DATABASE IF NOT EXISTS ArtConnect
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE ArtConnect;

-- TABLES PRINCIPALES

CREATE TABLE Artist (
    id_artist INT PRIMARY KEY,
    name_artist VARCHAR(100) NOT NULL,
    birthYear_artist SMALLINT,
    contactEmail_artist VARCHAR(150) UNIQUE,
    bio_artist TEXT,
    phone_artist VARCHAR(30),
    city_artist VARCHAR(100),
    website_artist VARCHAR(255),
    socialMedia_artist VARCHAR(255),
    isActive BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE Discipline (
    name_discipline VARCHAR(100) PRIMARY KEY
);

CREATE TABLE Artwork (
    id_artwork INT PRIMARY KEY,
    title_artwork VARCHAR(150) NOT NULL,
    creationYear_artwork SMALLINT,
    type_artwork VARCHAR(80),
    medium_artwork VARCHAR(120),
    dimensions_artwork VARCHAR(80),
    price_artwork DECIMAL(10,2),
    status_artwork ENUM('FOR_SALE', 'SOLD', 'EXHIBITED') NOT NULL DEFAULT 'FOR_SALE',
    description_artwork TEXT,
    id_artist INT NOT NULL,
    CONSTRAINT fk_artwork_artist
        FOREIGN KEY (id_artist) REFERENCES Artist(id_artist)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE ArtworkTag (
    name_artworkTag VARCHAR(100) PRIMARY KEY
);

CREATE TABLE Gallery (
    id_gallery INT PRIMARY KEY,
    name_gallery VARCHAR(120) NOT NULL,
    address_gallery VARCHAR(255),
    ownerName_gallery VARCHAR(100),
    openingHours_gallery VARCHAR(100),
    contactPhone_gallery VARCHAR(30),
    rating_gallery DECIMAL(2,1),
    website_gallery VARCHAR(255)
);

CREATE TABLE Exhibition (
    id_exhibition INT PRIMARY KEY,
    title_exhibition VARCHAR(150) NOT NULL,
    startDate_exhibition DATE NOT NULL,
    endDate_exhibition DATE NOT NULL,
    description_exhibition TEXT,
    curatorName_exhibition VARCHAR(100),
    theme_exhibition VARCHAR(100),
    id_gallery INT NOT NULL,
    CONSTRAINT chk_exhibition_dates CHECK (endDate_exhibition >= startDate_exhibition),
    CONSTRAINT fk_exhibition_gallery
        FOREIGN KEY (id_gallery) REFERENCES Gallery(id_gallery)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE CommunityMember (
    id_communityMember INT PRIMARY KEY,
    name_communityMember VARCHAR(100) NOT NULL,
    email_communityMember VARCHAR(150) NOT NULL UNIQUE,
    birthYear_communityMember SMALLINT,
    phone_communityMember VARCHAR(30),
    city_communityMember VARCHAR(100),
    membershipType_communityMember ENUM('standard', 'premium', 'student', 'artist_supporter') NOT NULL DEFAULT 'standard'
);

CREATE TABLE Workshop (
    id_workshop INT PRIMARY KEY,
    title_workshop VARCHAR(150) NOT NULL,
    date_workshop DATETIME NOT NULL,
    durationMinutes_workshop INT NOT NULL,
    maxParticipants_workshop INT NOT NULL,
    price_workshop DECIMAL(10,2) NOT NULL DEFAULT 0,
    location_workshop VARCHAR(150),
    description_workshop TEXT,
    level_workshop ENUM('beginner', 'intermediate', 'advanced') NOT NULL,
    id_artist INT NOT NULL,
    CONSTRAINT chk_workshop_duration CHECK (durationMinutes_workshop > 0),
    CONSTRAINT chk_workshop_max_participants CHECK (maxParticipants_workshop > 0),
    CONSTRAINT chk_workshop_price CHECK (price_workshop >= 0),
    CONSTRAINT fk_workshop_artist
        FOREIGN KEY (id_artist) REFERENCES Artist(id_artist)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE Review (
    id_review INT PRIMARY KEY,
    rating_review TINYINT NOT NULL,
    comment_review TEXT,
    reviewDate_review DATE NOT NULL,
    id_artwork INT NOT NULL,
    id_communityMember INT NOT NULL,
    CONSTRAINT chk_review_rating CHECK (rating_review BETWEEN 1 AND 5),
    CONSTRAINT fk_review_artwork
        FOREIGN KEY (id_artwork) REFERENCES Artwork(id_artwork)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_review_member
        FOREIGN KEY (id_communityMember) REFERENCES CommunityMember(id_communityMember)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- TABLES D'ASSOCIATION

CREATE TABLE Artist_Discipline (
    id_artist INT NOT NULL,
    name_discipline VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_artist, name_discipline),
    CONSTRAINT fk_artist_discipline_artist
        FOREIGN KEY (id_artist) REFERENCES Artist(id_artist)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_artist_discipline_discipline
        FOREIGN KEY (name_discipline) REFERENCES Discipline(name_discipline)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE CommunityMember_Discipline (
    id_communityMember INT NOT NULL,
    name_discipline VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_communityMember, name_discipline),
    CONSTRAINT fk_member_discipline_member
        FOREIGN KEY (id_communityMember) REFERENCES CommunityMember(id_communityMember)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_member_discipline_discipline
        FOREIGN KEY (name_discipline) REFERENCES Discipline(name_discipline)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Artwork_ArtworkTag (
    id_artwork INT NOT NULL,
    name_artworkTag VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_artwork, name_artworkTag),
    CONSTRAINT fk_artwork_tag_artwork
        FOREIGN KEY (id_artwork) REFERENCES Artwork(id_artwork)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_artwork_tag_tag
        FOREIGN KEY (name_artworkTag) REFERENCES ArtworkTag(name_artworkTag)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Artwork_Exhibition (
    id_artwork INT NOT NULL,
    id_exhibition INT NOT NULL,
    PRIMARY KEY (id_artwork, id_exhibition),
    CONSTRAINT fk_artwork_exhibition_artwork
        FOREIGN KEY (id_artwork) REFERENCES Artwork(id_artwork)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_artwork_exhibition_exhibition
        FOREIGN KEY (id_exhibition) REFERENCES Exhibition(id_exhibition)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Booking (
    id_workshop INT NOT NULL,
    id_communityMember INT NOT NULL,
    paymentStatus_booking ENUM('pending', 'paid', 'cancelled') NOT NULL DEFAULT 'pending',
    bookingDate_booking DATETIME NOT NULL,
    PRIMARY KEY (id_workshop, id_communityMember),
    CONSTRAINT fk_booking_workshop
        FOREIGN KEY (id_workshop) REFERENCES Workshop(id_workshop)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_booking_member
        FOREIGN KEY (id_communityMember) REFERENCES CommunityMember(id_communityMember)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);