package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "CONCERTS")
public class Concert {

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private long id;

    @Column(nullable = false)
    private String title;

    @ElementCollection
    @CollectionTable(
            name = "CONCERT_DATES",
            joinColumns = @JoinColumn(name = "CONCERT_ID", nullable = false)
    )
    @Column(nullable = false)
    private Set<LocalDateTime> dates;

    @ElementCollection
    @MapKeyColumn(name = "PRICE_BAND", table = "CONCERT_TARIFS")
    @MapKeyEnumerated(EnumType.STRING)
    @CollectionTable(
            name = "CONCERT_TARIFS",
            joinColumns = @JoinColumn(name = "CONCERT_ID", nullable = false)
    )
    @Column(nullable = false)
    private Map<PriceBand, BigDecimal> tariff;

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
    })
    @JoinTable(name = "CONCERT_PERFORMER",
            joinColumns = @JoinColumn(name = "CONCERT_ID", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID", nullable = false)
    )
    private Set<Performer> performers;

    @OneToMany(mappedBy = "concert")
    private Set<Booking> bookings;

    public Concert(String title) {
        this.title = title;
    }

    protected Concert() {
    } // required for JPA

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<LocalDateTime> getDates() {
        return dates; // modifiable
    }

    public void setDates(Set<LocalDateTime> dates) {
        this.dates = new HashSet<>(dates);
    }

    public Map<PriceBand, BigDecimal> getTariff() {
        return tariff;
    }

    public void setTariff(Map<PriceBand, BigDecimal> tariff) {
        this.tariff = new HashMap<>(tariff);
    }

    public Set<Performer> getPerformers() {
        return Collections.unmodifiableSet(performers);
    }

    // ManyToMany
    public void addPerformer(Performer performer) {
        this.performers.add(performer);
        performer.getConcerts().add(this);
    }

    // ManyToMany
    public void removePerformer(Performer performer) {
        this.performers.remove(performer);
        performer.getConcerts().remove(this);
    }

    public BigDecimal getTicketPrice(PriceBand seatType) {
        return tariff.get(seatType);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;
        return new EqualsBuilder().
                append(title, rhs.title).
                append(dates, rhs.dates).
                append(tariff, rhs.tariff).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(title).
                append(dates).
                append(tariff).
                hashCode();
    }

    public long getId() {
        return id;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }

    public void setPerformers(Set<Performer> performers) {
        this.performers = performers;
    }
}
