-- ============================================================
-- V1: booking_table 슬롯 유니크 제약 및 Booking 낙관적 락 추가
-- ============================================================
-- 목적: 동시 예약 오버부킹 방지를 위한 DB 레벨 유니크 제약 추가
-- 영향 테이블: booking_table, booking
-- ============================================================

-- [Step 1] 새 컬럼 추가 — NULL 허용으로 먼저 추가 (기존 행 오류 방지)
ALTER TABLE booking_table
    ADD COLUMN booking_date DATE        NULL,
    ADD COLUMN booking_time TIME        NULL,
    ADD COLUMN is_active    TINYINT(1)  NULL;

-- [Step 2] 기존 booking_table 행에 booking 테이블의 날짜/시간 백필
--          CONFIRMED / PENDING 상태: is_active = 1 (활성 슬롯)
--          그 외 (CANCELED 등): is_active = NULL (슬롯 해제, 유니크 제약 제외)
UPDATE booking_table bt
    INNER JOIN booking b ON bt.booking_id = b.id
SET bt.booking_date = b.booking_date,
    bt.booking_time = b.booking_time,
    bt.is_active    = CASE
        WHEN b.status IN ('CONFIRMED', 'PENDING') THEN 1
        ELSE NULL
    END;

-- [Step 3] 백필 완료 후 NOT NULL 적용
ALTER TABLE booking_table
    MODIFY COLUMN booking_date DATE NOT NULL,
    MODIFY COLUMN booking_time TIME NOT NULL;

-- [Step 4] 유니크 제약 추가
--          is_active가 NULL이면 MySQL 유니크 인덱스에서 중복 체크 제외
--          → 취소된 슬롯에 재예약 가능
ALTER TABLE booking_table
    ADD CONSTRAINT uq_booking_table_slot
        UNIQUE (store_table_id, booking_date, booking_time, is_active);

-- [Step 5] Booking 낙관적 락(@Version) 컬럼 추가 — NULL 허용 (JPA가 첫 write 시 0으로 초기화)
ALTER TABLE booking
    ADD COLUMN version BIGINT NULL;
