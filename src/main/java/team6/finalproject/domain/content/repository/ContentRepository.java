package team6.finalproject.domain.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import team6.finalproject.domain.content.entity.content.Content;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
	// JpaRepository를 상속받는 것만으로 save(), findById(), deleteById()를 사용할 수 있습니다.
}
