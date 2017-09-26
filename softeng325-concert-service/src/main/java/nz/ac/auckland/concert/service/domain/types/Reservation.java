package nz.ac.auckland.concert.service.domain.types;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class Reservation {

    @Version
    @Column(nullable = false, name = "opt_lock")
    private long version = 0L;

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reservation") // remove reservation on removal
    @Column(nullable = false)
    private Set<Seat> seats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceBand seatType;

    @ManyToOne(cascade = CascadeType.PERSIST) // Don't delete concert on removal
    @JoinColumn(nullable = false)
    private Concert concert;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne(cascade = CascadeType.PERSIST) // Don't delete user on removal
    @JoinColumn(nullable = false)
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

        concert.addReservation(this);
        user.addReservation(this);
        seats.forEach(seat -> seat.setReservation(this));
    }

    public void confirm() {
        confirmed = true;
        expiryTime = Long.MAX_VALUE;
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

    public long getId() {
        return id;
    }

    public long getExpiryDate() {
        return expiryTime;
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
        if (concert != null ? !concert.equals(that.concert) : that.concert != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        return user != null ? user.equals(that.user) : that.user == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (seats != null ? seats.hashCode() : 0);
        result = 31 * result + (seatType != null ? seatType.hashCode() : 0);
        result = 31 * result + (concert != null ? concert.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (int) (expiryTime ^ (expiryTime >>> 32));
        result = 31 * result + (confirmed ? 1 : 0);
        return result;
    }
}
