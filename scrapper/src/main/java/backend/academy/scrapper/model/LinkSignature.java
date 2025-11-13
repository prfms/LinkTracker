package backend.academy.scrapper.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "link_signatures")
public class LinkSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "link_id", nullable = false)
    private Long linkId;

    @Column(name = "etag")
    private String etag;

    @Column(name = "last_modified")
    private String lastModified;

    @Column(name = "composite_hash", length = 64)
    private String compositeHash;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
