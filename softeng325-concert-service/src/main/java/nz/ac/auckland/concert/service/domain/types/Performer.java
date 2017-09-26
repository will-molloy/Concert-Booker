package nz.ac.auckland.concert.service.domain.types;

import nz.ac.auckland.concert.common.types.Genre;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PERFORMERS")
public class Performer {

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    @Column(nullable = false)
    private String imageName;

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "performers")
    private Set<Concert> concerts = new HashSet<>();

    public Performer() {
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Concert> getConcerts() {
        return Collections.unmodifiableSet(concerts);
    }

    public void setConcerts(Set<Concert> concerts) {
        this.concerts = concerts;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Performer))
            return false;
        if (obj == this)
            return true;

        Performer rhs = (Performer) obj;
        return new EqualsBuilder().
                append(name, rhs.name).
                append(imageName, rhs.imageName).
                append(genre, rhs.genre).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(name).
                append(imageName).
                append(genre).
                hashCode();
    }

    public long getId() {
        return id;
    }
}
