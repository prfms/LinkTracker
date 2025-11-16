package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.LinkSignature;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkSignatureRepository extends JpaRepository<LinkSignature, Long> {

    Optional<LinkSignature> findByLinkId(int linkId);
}
