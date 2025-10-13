package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
