package nz.ac.auckland.concert.common.dto;

import nz.ac.auckland.concert.common.types.PriceBand;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DTO class to represent concerts.
 * <p>
 * A ConcertDTO describes a concert in terms of:
 * _id           the unique identifier for a concert.
 * _title        the concert's title.
 * _dates        the concert's scheduled dates and times (represented as a
 * Set of LocalDateTime instances).
 * _tariff       concert pricing - the cost of a ticket for each price band
 * (A, B and C) is set individually for each concert.
 * _performerIds identification of each performer playing at a concert
 * (represented as a set of performer identifiers).
 */
@XmlRootElement(name = "concert")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConcertDTO {

    @XmlAttribute(name = "id")
    private Long _id;

    @XmlElement(name = "title")
    private String _title;

    @XmlElement(name = "dates")
    private Set<LocalDateTime> _dates;

    @XmlElement(name = "tariff")
    private Map<PriceBand, BigDecimal> _tariff;

    @XmlElement(name = "performer-ids")
    private Set<Long> _performerIds;

    public ConcertDTO() {
    }

    public ConcertDTO(Long id, String title, Set<LocalDateTime> dates,
                      Map<PriceBand, BigDecimal> ticketPrices, Set<Long> performerIds) {
        _id = id;
        _title = title;
        _dates = new HashSet<LocalDateTime>(dates);
        _tariff = new HashMap<PriceBand, BigDecimal>(ticketPrices);
        _performerIds = new HashSet<Long>(performerIds);
    }

    public Long getId() {
        return _id;
    }

    public String getTitle() {
        return _title;
    }

    public Set<LocalDateTime> getDates() {
        return Collections.unmodifiableSet(_dates);
    }

    public BigDecimal getTicketPrice(PriceBand seatType) {
        return _tariff.get(seatType);
    }

    public Set<Long> getPerformerIds() {
        return Collections.unmodifiableSet(_performerIds);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConcertDTO))
            return false;
        if (obj == this)
            return true;

        ConcertDTO rhs = (ConcertDTO) obj;
        return new EqualsBuilder().
                append(_title, rhs._title).
                append(_dates, rhs._dates).
                append(_tariff, rhs._tariff).
                append(_performerIds, rhs._performerIds).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(_title).
                append(_dates).
                append(_tariff).
                append(_performerIds).
                hashCode();
    }
}
