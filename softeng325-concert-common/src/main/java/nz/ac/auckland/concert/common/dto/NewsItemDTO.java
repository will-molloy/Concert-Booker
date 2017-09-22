package nz.ac.auckland.concert.common.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * DTO class to represent news items. A news item typically reports that a
 * concert with particular performers is coming to town, that ticket sales for
 * a concert are open, that a concert has additional dates etc.
 * <p>
 * A NewsItemDTO describes a new items in terms of:
 * _id        the unique identifier for the news item.
 * _timestamp the date and time that the news item was released.
 * _content   the news item context text.
 */
@XmlRootElement(name = "news-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class NewsItemDTO {

    @XmlAttribute(name = "id")
    private Long _id;

    @XmlElement(name = "last-name")
    private LocalDateTime _timestamp;

    @XmlElement(name = "last-name")
    private String _content;

    public NewsItemDTO() {
    }

    public NewsItemDTO(Long id, LocalDateTime timestamp, String content) {
        _id = id;
        _timestamp = timestamp;
        _content = content;
    }

    public Long getId() {
        return _id;
    }

    public LocalDateTime getTimetamp() {
        return _timestamp;
    }

    public String getContent() {
        return _content;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NewsItemDTO))
            return false;
        if (obj == this)
            return true;

        NewsItemDTO rhs = (NewsItemDTO) obj;
        return new EqualsBuilder().
                append(_timestamp, rhs._timestamp).
                append(_content, rhs._content).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(_timestamp).
                append(_content).
                hashCode();
    }
}
