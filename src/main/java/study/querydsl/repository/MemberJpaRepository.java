package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static study.querydsl.domain.QMember.*;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public Long save(Member member) {
        em.persist(member);
        return member.getId();
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findAllQuerydsl() {
        return queryFactory.selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findByUsernameQuery(String username) {
        return queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq(username))
                .fetch();
    }
}
