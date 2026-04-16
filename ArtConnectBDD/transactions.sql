USE ArtConnect;

DROP PROCEDURE IF EXISTS sp_register_member_to_multiple_workshops;

DELIMITER //

CREATE PROCEDURE sp_register_member_to_multiple_workshops(
    IN p_id_member INT,
    IN p_id_workshop_1 INT,
    IN p_id_workshop_2 INT,
    IN p_id_workshop_3 INT,
    IN p_booking_date DATE
)
BEGIN
    DECLARE v_member_count INT DEFAULT 0;
    DECLARE v_exists_1 INT DEFAULT 0;
    DECLARE v_exists_2 INT DEFAULT 0;
    DECLARE v_exists_3 INT DEFAULT 0;
    DECLARE v_already_1 INT DEFAULT 0;
    DECLARE v_already_2 INT DEFAULT 0;
    DECLARE v_already_3 INT DEFAULT 0;
    DECLARE v_capacity_1 INT DEFAULT 0;
    DECLARE v_capacity_2 INT DEFAULT 0;
    DECLARE v_capacity_3 INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- 0. Vérifier que les 3 ateliers sont différents
    IF p_id_workshop_1 = p_id_workshop_2
       OR p_id_workshop_1 = p_id_workshop_3
       OR p_id_workshop_2 = p_id_workshop_3 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : les ateliers doivent etre differents';
    END IF;

    -- 1. Vérifier que le membre existe
    SELECT COUNT(*)
    INTO v_member_count
    FROM CommunityMember
    WHERE id_communityMember = p_id_member;

    IF v_member_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : membre inexistant';
    END IF;

    -- 2. Vérifier que les ateliers existent
    SELECT COUNT(*)
    INTO v_exists_1
    FROM Workshop
    WHERE id_workshop = p_id_workshop_1;

    SELECT COUNT(*)
    INTO v_exists_2
    FROM Workshop
    WHERE id_workshop = p_id_workshop_2;

    SELECT COUNT(*)
    INTO v_exists_3
    FROM Workshop
    WHERE id_workshop = p_id_workshop_3;

    IF v_exists_1 = 0 OR v_exists_2 = 0 OR v_exists_3 = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : au moins un atelier est inexistant';
    END IF;

    -- 3. Vérifier que le membre n'est pas deja inscrit
    -- On ignore les reservations annulees
    SELECT COUNT(*)
    INTO v_already_1
    FROM Booking
    WHERE id_workshop = p_id_workshop_1
      AND id_communityMember = p_id_member
      AND paymentStatus_booking IN ('paid', 'pending');

    SELECT COUNT(*)
    INTO v_already_2
    FROM Booking
    WHERE id_workshop = p_id_workshop_2
      AND id_communityMember = p_id_member
      AND paymentStatus_booking IN ('paid', 'pending');

    SELECT COUNT(*)
    INTO v_already_3
    FROM Booking
    WHERE id_workshop = p_id_workshop_3
      AND id_communityMember = p_id_member
      AND paymentStatus_booking IN ('paid', 'pending');

    IF v_already_1 > 0 OR v_already_2 > 0 OR v_already_3 > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : le membre est deja inscrit a au moins un des ateliers';
    END IF;

    -- 4. Vérifier qu'il reste de la place dans chaque atelier
    SELECT CASE
        WHEN COUNT(b.id_communityMember) < w.maxParticipants_workshop THEN 1
        ELSE 0
    END
    INTO v_capacity_1
    FROM Workshop w
    LEFT JOIN Booking b
        ON w.id_workshop = b.id_workshop
       AND b.paymentStatus_booking IN ('paid', 'pending')
    WHERE w.id_workshop = p_id_workshop_1
    GROUP BY w.id_workshop, w.maxParticipants_workshop;

    SELECT CASE
        WHEN COUNT(b.id_communityMember) < w.maxParticipants_workshop THEN 1
        ELSE 0
    END
    INTO v_capacity_2
    FROM Workshop w
    LEFT JOIN Booking b
        ON w.id_workshop = b.id_workshop
       AND b.paymentStatus_booking IN ('paid', 'pending')
    WHERE w.id_workshop = p_id_workshop_2
    GROUP BY w.id_workshop, w.maxParticipants_workshop;

    SELECT CASE
        WHEN COUNT(b.id_communityMember) < w.maxParticipants_workshop THEN 1
        ELSE 0
    END
    INTO v_capacity_3
    FROM Workshop w
    LEFT JOIN Booking b
        ON w.id_workshop = b.id_workshop
       AND b.paymentStatus_booking IN ('paid', 'pending')
    WHERE w.id_workshop = p_id_workshop_3
    GROUP BY w.id_workshop, w.maxParticipants_workshop;

    IF v_capacity_1 = 0 OR v_capacity_2 = 0 OR v_capacity_3 = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : au moins un atelier est complet';
    END IF;

    -- 5. Inscription atomique aux trois ateliers
    INSERT INTO Booking (
        id_workshop,
        id_communityMember,
        paymentStatus_booking,
        bookingDate_booking
    )
    VALUES
    (p_id_workshop_1, p_id_member, 'pending', p_booking_date),
    (p_id_workshop_2, p_id_member, 'pending', p_booking_date),
    (p_id_workshop_3, p_id_member, 'pending', p_booking_date);

    COMMIT;
END//

DELIMITER ;

-- Logical error (test)
CALL sp_register_member_to_multiple_workshops(5, 1, 2, 3, '2026-06-20');