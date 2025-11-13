package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.LinkSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkSignatureRepository extends JpaRepository<LinkSignature, Long> {
    Optional<LinkSignature> findByLinkId(Long linkId);
}
