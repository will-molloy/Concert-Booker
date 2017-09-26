package nz.ac.auckland.concert.service.domain.types;

import nz.ac.auckland.concert.common.types.PriceBand;

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

    @Version
    @Column(nullable = false, name = "opt_lock")
    private long version = 0L;

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

    // Delete reservations on removal
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Reservation> reservations = new HashSet<>();

    public void addReservation(Reservation reservation){
        reservations.add(reservation);
    }

    public void removeReservation(Reservation reservation){
        reservations.remove(reservation);
    }

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

    public void setPerformers(Set<Performer> performers) {
        this.performers = performers;
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

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Concert concert = (Concert) o;

        if (id != concert.id) return false;
        if (title != null ? !title.equals(concert.title) : concert.title != null) return false;
        if (dates != null ? !dates.equals(concert.dates) : concert.dates != null) return false;
        if (tariff != null ? !tariff.equals(concert.tariff) : concert.tariff != null) return false;
        return (performers != null ? !performers.equals(concert.performers) : concert.performers != null);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (dates != null ? dates.hashCode() : 0);
        result = 31 * result + (tariff != null ? tariff.hashCode() : 0);
        result = 31 * result + (performers != null ? performers.hashCode() : 0);
        return result;
    }
}
