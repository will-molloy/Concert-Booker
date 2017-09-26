package nz.ac.auckland.concert.service.domain.types;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

@Entity
public class NewsItem {

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private Long _id;

    @Column(nullable = false)
    private LocalDateTime _timestamp;

    @Column(nullable = false)
    private String _content;

    protected NewsItem() {
    }

    public NewsItem(LocalDateTime timestamp, String content) {
        _timestamp = timestamp;
        _content = content;
    }

    public Long getId() {
        return _id;
    }

    public LocalDateTime get_timestamp() {
        return _timestamp;
    }

    public void set_timestamp(LocalDateTime _timestamp) {
        this._timestamp = _timestamp;
    }

    public String get_content() {
        return _content;
    }

    public void set_content(String _content) {
        this._content = _content;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NewsItem))
            return false;
        if (obj == this)
            return true;

        NewsItem rhs = (NewsItem) obj;
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
