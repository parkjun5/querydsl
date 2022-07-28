package study.querydsl.service.querydsl;


import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;
import study.querydsl.domain.Team;
import study.querydsl.repository.MemberRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.QMember.member;

@SpringBootTest
@Transactional
class SubQueryTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void setEntity() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("TeamA");
        em.persist(teamA);
        Team TeamB = new Team("TeamB");
        em.persist(TeamB);

        Member member1 = Member.createMember("member1", 10, teamA);
        Member member2 = Member.createMember("member2", 20, teamA);

        Member member3 = Member.createMember("member3", 30, TeamB);
        Member member4 = Member.createMember("member4", 40, TeamB);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
    }

    /**
     * 나이가 가장 많은 사람을 조회
     */
    @Test
    @DisplayName("서브쿼리 Basic Test")
    void subQueryOne() {
        //given
        QMember subMember = new QMember("subMember");

        //when
        Member oldestMember = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.age.eq(
                        select(subMember.age.max())
                                .from(subMember)
                )).fetchOne();

        //then
        assertThat(oldestMember)
                .extracting("username")
                .isEqualTo("member4");
    }

    /**
     * 나이가 평균이상은 회원
     */
    @Test
    @DisplayName("서브쿼리 <= 조건")
    void subQueryGoe() {
        //given
        QMember subMember = new QMember("subMember");

        //when
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(subMember.age.avg())
                                .from(subMember)
                )).fetch();

        //then
        assertThat(members)
                .extracting("age")
                .containsExactly(30, 40);
    }


    @Test
    @DisplayName("조건 In + 서브쿼리")
    void subQueryIn() {
        //given
        QMember subMember = new QMember("subMember");

        //when
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(subMember.age)
                                .from(subMember)
                                .where(subMember.age.gt(10))
                )).fetch();

        //then
        assertThat(members)
                .extracting("age")
                .containsExactly(20, 30, 40);
    }

    @Test
    @DisplayName("셀렉트에 서브쿼리")
    void selectSubQuery() {
        //given
        QMember subMember = new QMember("subMember");

        //when
        List<Tuple> members = queryFactory
                .select(
                        member.username,
                        select(subMember.age.avg())
                                .from(subMember)
                ).from(member)
                .fetch();

        //then
        assertThat(members).hasSize(4);
    }
}
