package com.eatsfine.eatsfine.domain.booking.entity.mapping;

import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class BookingMenu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    private Menu menu;

    public void confirmBooking(Booking booking) {
        this.booking = booking;
    }

}
