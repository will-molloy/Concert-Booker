package nz.ac.auckland.concert.service.domain.types;

import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;

import javax.persistence.*;

@Entity
public class Seat {

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

    // ensure seats are bound to a reservation and delete reservations on removal
    // reservation has concert and date therefore all reserved seats for all dates/concerts are stored in the database
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private Reservation reservation;

    public Seat(SeatRow row, SeatNumber number) {
        this.row = row;
        this.number = number;
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

    /**
     * Excludes Reservation here so seats without reservations can be compared to those with reservations.
     * Reservations equals() and hashCode() includes seats so this would create a cycle.
     * Excludes ID so seats not persisted can be compared to persisted seats.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Seat seat = (Seat) o;

        if (row != seat.row) return false;
        return number != null ? number.equals(seat.number) : seat.number == null;
    }

    @Override
    public int hashCode() {
        int result = row != null ? row.hashCode() : 0;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        return result;
    }
}
