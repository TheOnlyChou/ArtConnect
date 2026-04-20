USE ArtConnect;

CREATE TABLE IF NOT EXISTS Exhibition_Audit (
    id_audit INT AUTO_INCREMENT PRIMARY KEY,
    id_exhibition INT NOT NULL,
    old_title_exhibition VARCHAR(150),
    new_title_exhibition VARCHAR(150),
    old_startDate_exhibition DATE,
    new_startDate_exhibition DATE,
    old_endDate_exhibition DATE,
    new_endDate_exhibition DATE,
    audit_action VARCHAR(20) NOT NULL,
    audit_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DROP TRIGGER IF EXISTS trg_exhibition_check_dates_insert;
DROP TRIGGER IF EXISTS trg_exhibition_check_dates_update;
DROP TRIGGER IF EXISTS trg_booking_check_capacity;
DROP TRIGGER IF EXISTS trg_booking_check_capacity_update;
DROP TRIGGER IF EXISTS trg_exhibition_audit_update;

DELIMITER //

CREATE TRIGGER trg_exhibition_check_dates_insert
BEFORE INSERT ON Exhibition
FOR EACH ROW
BEGIN
    IF NEW.endDate_exhibition < NEW.startDate_exhibition THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : date de fin invalide';
    END IF;
END//

CREATE TRIGGER trg_exhibition_check_dates_update
BEFORE UPDATE ON Exhibition
FOR EACH ROW
BEGIN
    IF NEW.endDate_exhibition < NEW.startDate_exhibition THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : date de fin invalide';
    END IF;
END//

CREATE TRIGGER trg_booking_check_capacity
BEFORE INSERT ON Booking
FOR EACH ROW
BEGIN
    DECLARE v_current_count INT;
    DECLARE v_max_places INT;

    SELECT COUNT(*)
    INTO v_current_count
    FROM Booking
    WHERE id_workshop = NEW.id_workshop
      AND paymentStatus_booking IN ('paid', 'pending');

    SELECT maxParticipants_workshop
    INTO v_max_places
    FROM Workshop
    WHERE id_workshop = NEW.id_workshop;

    IF v_current_count >= v_max_places THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : atelier complet';
    END IF;
END//

CREATE TRIGGER trg_booking_check_capacity_update
BEFORE UPDATE ON Booking
FOR EACH ROW
BEGIN
    DECLARE v_current_count INT;
    DECLARE v_max_places INT;

    IF NEW.id_workshop <> OLD.id_workshop
       OR (OLD.paymentStatus_booking = 'cancelled' AND NEW.paymentStatus_booking IN ('paid', 'pending'))
       OR (OLD.paymentStatus_booking NOT IN ('paid', 'pending') AND NEW.paymentStatus_booking IN ('paid', 'pending')) THEN

        SELECT COUNT(*)
        INTO v_current_count
        FROM Booking
        WHERE id_workshop = NEW.id_workshop
          AND paymentStatus_booking IN ('paid', 'pending')
          AND NOT (id_workshop = OLD.id_workshop AND id_communityMember = OLD.id_communityMember);

        SELECT maxParticipants_workshop
        INTO v_max_places
        FROM Workshop
        WHERE id_workshop = NEW.id_workshop;

        IF NEW.paymentStatus_booking IN ('paid', 'pending') AND v_current_count >= v_max_places THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Erreur : atelier complet';
        END IF;
    END IF;
END//

CREATE TRIGGER trg_exhibition_audit_update
AFTER UPDATE ON Exhibition
FOR EACH ROW
BEGIN
    INSERT INTO Exhibition_Audit (
        id_exhibition,
        old_title_exhibition,
        new_title_exhibition,
        old_startDate_exhibition,
        new_startDate_exhibition,
        old_endDate_exhibition,
        new_endDate_exhibition,
        audit_action
    )
    VALUES (
        OLD.id_exhibition,
        OLD.title_exhibition,
        NEW.title_exhibition,
        OLD.startDate_exhibition,
        NEW.startDate_exhibition,
        OLD.endDate_exhibition,
        NEW.endDate_exhibition,
        'UPDATE'
    );
END//

DROP PROCEDURE IF EXISTS sp_create_workshop//
CREATE PROCEDURE sp_create_workshop(
    IN p_id_workshop INT,
    IN p_title_workshop VARCHAR(150),
    IN p_date_workshop DATETIME,
    IN p_durationMinutes_workshop INT,
    IN p_maxParticipants_workshop INT,
    IN p_price_workshop DECIMAL(10,2),
    IN p_location_workshop VARCHAR(150),
    IN p_description_workshop TEXT,
    IN p_level_workshop VARCHAR(20),
    IN p_id_artist INT
)
BEGIN
    DECLARE v_artist_count INT;

    SELECT COUNT(*)
    INTO v_artist_count
    FROM Artist
    WHERE id_artist = p_id_artist;

    IF v_artist_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : artiste inexistant';
    END IF;

    INSERT INTO Workshop (
        id_workshop,
        title_workshop,
        date_workshop,
        durationMinutes_workshop,
        maxParticipants_workshop,
        price_workshop,
        location_workshop,
        description_workshop,
        level_workshop,
        id_artist
    )
    VALUES (
        p_id_workshop,
        p_title_workshop,
        p_date_workshop,
        p_durationMinutes_workshop,
        p_maxParticipants_workshop,
        p_price_workshop,
        p_location_workshop,
        p_description_workshop,
        p_level_workshop,
        p_id_artist
    );
END//

DROP PROCEDURE IF EXISTS sp_book_workshop//
CREATE PROCEDURE sp_book_workshop(
    IN p_id_workshop INT,
    IN p_id_communityMember INT,
    IN p_paymentStatus_booking VARCHAR(20),
    IN p_bookingDate_booking DATETIME
)
BEGIN
    DECLARE v_workshop_count INT;
    DECLARE v_member_count INT;

    SELECT COUNT(*) INTO v_workshop_count
    FROM Workshop
    WHERE id_workshop = p_id_workshop;

    IF v_workshop_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : atelier inexistant';
    END IF;

    SELECT COUNT(*) INTO v_member_count
    FROM CommunityMember
    WHERE id_communityMember = p_id_communityMember;

    IF v_member_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : membre inexistant';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM Booking
        WHERE id_workshop = p_id_workshop
          AND id_communityMember = p_id_communityMember
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : reservation deja existante pour ce membre et cet atelier';
    END IF;

    INSERT INTO Booking (
        id_workshop,
        id_communityMember,
        paymentStatus_booking,
        bookingDate_booking
    )
    VALUES (
        p_id_workshop,
        p_id_communityMember,
        p_paymentStatus_booking,
        p_bookingDate_booking
    );
END//

DROP FUNCTION IF EXISTS fn_workshop_participant_count//
CREATE FUNCTION fn_workshop_participant_count(p_id_workshop INT)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE v_count INT;

    SELECT COUNT(*)
    INTO v_count
    FROM Booking
    WHERE id_workshop = p_id_workshop
      AND paymentStatus_booking IN ('paid', 'pending');

    RETURN v_count;
END//

DROP FUNCTION IF EXISTS fn_artwork_average_rating//
CREATE FUNCTION fn_artwork_average_rating(p_id_artwork INT)
RETURNS DECIMAL(4,2)
DETERMINISTIC
BEGIN
    DECLARE v_avg DECIMAL(4,2);

    SELECT COALESCE(AVG(rating_review), 0)
    INTO v_avg
    FROM Review
    WHERE id_artwork = p_id_artwork;

    RETURN v_avg;
END//

DELIMITER ;

/* Test cases
CALL sp_create_workshop(
    10,
    'Atelier test',
    '2026-08-01 14:00:00',
    90,
    12,
    25.00,
    'Paris',
    'Atelier de demonstration',
    'beginner',
    1
);

CALL sp_book_workshop(10, 1, 'paid', '2026-07-20 10:00:00');

SELECT fn_workshop_participant_count(10);

SELECT fn_artwork_average_rating(4);
*/