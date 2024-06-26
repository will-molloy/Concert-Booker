package nz.ac.auckland.concert.service.domain.types;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Seat {

    @Version
    @Column(nullable = false, name = "opt_lock")
    private long version = 0L;

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatRow row;

    @Convert(converter = SeatNumberConverter.class)
    @Column(nullable = false)
    private SeatNumber number;

    private PriceBand seatType;
    private LocalDateTime concertDate;

    // ensure seats are bound to a reservation and delete reservations on removal
    // reservation has concert and date therefore all reserved seats for all dates/concerts are stored in the database
    @ManyToOne(cascade = CascadeType.ALL)
    private Reservation reservation;

    public Seat(SeatRow row, SeatNumber number, PriceBand seatType, LocalDateTime concertDate) {
        this.row = row;
        this.number = number;
        this.seatType = seatType;
        this.concertDate = concertDate;
    }

    protected Seat() {
    }

    public SeatRow getRow() {
        return row;
    }

    public void setRow(SeatRow row) {
        this.row = row;
    }

    public SeatNumber getNumber() {
        return number;
    }

    public void setNumber(SeatNumber number) {
        this.number = number;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public LocalDateTime getConcertDate() {
        return concertDate;
    }

    public void setConcertDate(LocalDateTime concertDate) {
        this.concertDate = concertDate;
    }

    /**
     * Excludes Reservation so seats without reservations can be compared to those with reservations.
     * Reservations equals() and hashCode() includes seats so this would create a cycle.
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Seat seat = (Seat) o;

        if (id != seat.id) return false;
        if (row != seat.row) return false;
        if (number != null ? !number.equals(seat.number) : seat.number != null) return false;
        return concertDate != null ? concertDate.equals(seat.concertDate) : seat.concertDate == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (row != null ? row.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (concertDate != null ? concertDate.hashCode() : 0);
        return result;
    }
}
