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
    GROUP_CONCAT(DISTINCT ar.name_artist ORDER BY ar.name_artist SEPARATOR ', ') AS artists
FROM Artwork aw
LEFT JOIN Artist_Artwork aa ON aw.id_artwork = aa.id_artwork
LEFT JOIN Artist ar ON aa.id_artist = ar.id_artist
GROUP BY
    aw.id_artwork,
    aw.title_artwork,
    aw.creationYear_artwork,
    aw.type_artwork,
    aw.medium_artwork,
    aw.dimensions_artwork,
    aw.price_artwork,
    aw.status_artwork;

-- simplify workshop (booked_places, reserved and available)
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
    
-- simplify review consultation, title, name email of the member
CREATE OR REPLACE VIEW v_artwork_reviews AS
SELECT
    r.id_review,
    aw.title_artwork,
    cm.name_communityMember,
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
WHERE status_artwork = 'available';
*/