package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import study.querydsl.domain.Member;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {


}
