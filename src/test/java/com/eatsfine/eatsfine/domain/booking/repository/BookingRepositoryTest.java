package com.eatsfine.eatsfine.domain.booking.repository;

import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.booking.entity.mapping.BookingTable;
import com.eatsfine.eatsfine.domain.booking.enums.BookingStatus;
import com.eatsfine.eatsfine.domain.region.entity.Region;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreApprovalStatus;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BookingRepositoryTest.TestJpaConfig.class)
class BookingRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing
    static class TestJpaConfig {}

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("특정 날짜와 시간에 예약된 테이블 ID 목록을 정확히 조회한다")
    void findReservedTableIds_Success() {
        // given: 1. 필수 연관 데이터 생성 (User, Region)
        User owner = User.builder()
                .build();
        em.persist(owner);

        Region region = Region.builder()
                .province("경기도")
                .district("마포구")
                .city("서울시") 
                .build();
        em.persist(region);

        // 2. Store 생성
        Store store = Store.builder()
                .owner(owner)
                .region(region)
                .storeName("아웃백")
                .address("서울시 강남구")
                .phoneNumber("02-123-4567")
                .description("맛있는 식당")
                .mainImageUrl("https://example.com/image.jpg")
                .rating(new BigDecimal("4.5"))
                .category(Category.WESTERN)
                .minPrice(20000)
                .maxPrice(50000)
                .approvalStatus(StoreApprovalStatus.APPROVED)
                .bookingIntervalMinutes(30)
                .build();
        em.persist(store);

        // 3. StoreTable 생성
        StoreTable table1 = StoreTable.builder()
                .tableNumber("T1")
                .store(store)
                .tableSeats(4)
                .build();
        StoreTable table2 = StoreTable.builder()
                .tableNumber("T2")
                .store(store)
                .tableSeats(2)
                .build();
        em.persist(table1);
        em.persist(table2);

        LocalDate date = LocalDate.of(2026, 1, 9);
        LocalTime time = LocalTime.of(18, 0);

        // 4. Booking 생성
        Booking booking = Booking.builder()
                .store(store)
                .bookingDate(date)
                .partySize(4)
                .user(owner)
                .bookingTime(time)
                .status(BookingStatus.CONFIRMED)
                .build();
        em.persist(booking);

        // 5.  매핑 테이블 생성
        BookingTable bookingTable = BookingTable.builder()
                .booking(booking)
                .storeTable(table1)
                .build();
        em.persist(bookingTable);

        em.flush();
        em.clear();

        // when
        List<Long> reservedIds = bookingRepository.findReservedTableIds(store.getId(), date, time);

        // then
        assertThat(reservedIds).hasSize(1);
        assertThat(reservedIds).containsExactly(table1.getId());
        assertThat(reservedIds).doesNotContain(table2.getId());
    }
}