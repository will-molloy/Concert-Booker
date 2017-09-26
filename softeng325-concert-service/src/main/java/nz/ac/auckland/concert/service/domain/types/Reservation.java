package nz.ac.auckland.concert.service.domain.types;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class Reservation {

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private long id;

    // remove and unlock seats on removal
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reservation")
    private Set<Seat> seats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceBand seatType;

    @ManyToOne // unidirectional
    @JoinColumn(referencedColumnName = "id", nullable = false, unique = true)
    private Concert concert;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne(cascade = CascadeType.PERSIST) // Don't delete users on removal
    @JoinColumn(nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private long expiryTime;

    @Column(nullable = false)
    private boolean confirmed = false;

    protected Reservation() {
    }

    public Reservation(Concert concert, LocalDateTime date, PriceBand seatType, Set<Seat> seats, User user, long expiryTime) {
        this.concert = concert;
        this.date = date;
        this.seatType = seatType;
        this.seats = seats;
        this.user = user;
        this.expiryTime = expiryTime;

        seats.forEach(seat -> seat.setReservation(this));
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
    }

    public PriceBand getSeatType() {
        return seatType;
    }

    public void setSeatType(PriceBand seatType) {
        this.seatType = seatType;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public long getId() {
        return id;
    }

    public long getExpiryDate() {
        return expiryTime;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryTime = expiryDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reservation that = (Reservation) o;

        if (id != that.id) return false;
        if (expiryTime != that.expiryTime) return false;
        if (confirmed != that.confirmed) return false;
        if (seats != null ? !seats.equals(that.seats) : that.seats != null) return false;
        if (seatType != that.seatType) return false;
        return date != null ? date.equals(that.date) : that.date == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (seats != null ? seats.hashCode() : 0);
        result = 31 * result + (seatType != null ? seatType.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (int) (expiryTime ^ (expiryTime >>> 32));
        result = 31 * result + (confirmed ? 1 : 0);
        return result;
    }
}
