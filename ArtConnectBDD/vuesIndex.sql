USE ArtConnect;

-- simplify catalog consultation, sensitive data 
CREATE OR REPLACE VIEW v_public_artworks AS
SELECT
    aw.id_artwork,
    aw.title_artwork,
    aw.creationYear_artwork,
    aw.type_artwork,
    aw.medium_artwork,
    aw.dimensions_artwork,
    aw.price_artwork,
    aw.status_artwork,
    ar.name_artist AS artist
FROM Artwork aw
JOIN Artist ar ON aw.id_artist = ar.id_artist;

-- simplify workshop occupancy (booked and remaining places)
CREATE OR REPLACE VIEW v_workshop_occupancy AS
SELECT
    w.id_workshop,
    w.title_workshop,
    w.date_workshop,
    w.maxParticipants_workshop,
    COUNT(b.id_communityMember) AS booked_places,
    (w.maxParticipants_workshop - COUNT(b.id_communityMember)) AS remaining_places
FROM Workshop w
LEFT JOIN Booking b
    ON w.id_workshop = b.id_workshop
    AND b.paymentStatus_booking IN ('paid', 'pending')
GROUP BY
    w.id_workshop,
    w.title_workshop,
    w.date_workshop,
    w.maxParticipants_workshop;
    
-- simplify review consultation, title, name and email of the member
CREATE OR REPLACE VIEW v_artwork_reviews AS
SELECT
    r.id_review,
    aw.title_artwork,
    cm.name_communityMember,
    cm.email_communityMember,
    r.rating_review,
    r.comment_review,
    r.reviewDate_review
FROM Review r
JOIN Artwork aw ON r.id_artwork = aw.id_artwork
JOIN CommunityMember cm ON r.id_communityMember = cm.id_communityMember; 

CREATE INDEX idx_exhibition_dates
ON Exhibition(startDate_exhibition, endDate_exhibition);
/* 
Example : 
SELECT *
FROM Exhibition
WHERE startDate_exhibition <= '2026-07-01'
  AND endDate_exhibition >= '2026-07-01';
*/

CREATE INDEX idx_booking_workshop_status
ON Booking(id_workshop, paymentStatus_booking);
/*
Example: 
SELECT COUNT(*)
FROM Booking
WHERE id_workshop = 1
  AND paymentStatus_booking IN ('paid', 'pending');
*/

CREATE INDEX idx_review_artwork
ON Review(id_artwork);
/*
Example :
SELECT AVG(rating_review)
FROM Review
WHERE id_artwork = 4;
*/

CREATE INDEX idx_artwork_status
ON Artwork(status_artwork);
/*
Example : 
SELECT *
FROM Artwork
WHERE status_artwork = 'FOR_SALE';
*/

CREATE INDEX idx_artwork_artist
ON Artwork(id_artist);
/*
Example :
SELECT *
FROM Artwork
WHERE id_artist = 3;
*/

CREATE INDEX idx_member_discipline
ON CommunityMember_Discipline(name_discipline, id_communityMember);
/*
Example :
SELECT cm.*
FROM CommunityMember cm
JOIN CommunityMember_Discipline cmd
    ON cm.id_communityMember = cmd.id_communityMember
WHERE cmd.name_discipline = 'Photography';
*/