package study.querydsl.service.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.domain.Member;
import study.querydsl.domain.Team;
import study.querydsl.repository.MemberRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.QMember.member;

@SpringBootTest
@Transactional
class SearchTest {

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


    @Test
    @DisplayName("검색조건: Equals / Not Equals")
    void search1() {
        //given
        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.ne(15))
                        .and(member.team.name.eq("teamB").not()))
                .fetchOne();

        assertThat(findMember).isNotNull();
    }

    @Test
    @DisplayName("검색조건: Between")
    void search2() {
        //given
        Member findMember = queryFactory.selectFrom(member)
                .where(member.age.between(15, 50))
                .fetchFirst();

        assertThat(findMember).isNotNull();
    }

    @Test
    @DisplayName("검색조건: Not null")
    void search3() {
        //given
        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.isNotNull())
                .fetchFirst();

        assertThat(findMember).isNotNull();
    }

    @Test
    @DisplayName("검색조건: In Not In")
    void search4() {
        //given
        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.in("mem", "mem1", "member1")
                        .and(member.username.notIn("m1", "m2")))
                .fetchOne();

        assertThat(findMember).isNotNull();
    }

    @Test
    @DisplayName("검색조건: geo gt loe lt 비교")
    void search6() {
        //given
        Member findMember = queryFactory.selectFrom(member)
                .where(member.age.goe(21))
                .fetchFirst();
        //geo >= gt > loe <= lt <
        assertThat(findMember).isNotNull();
    }

    @Test
    @DisplayName("검색조건: Like, Contains, StartWith ")
    void search7() {
        //given
        Member findMember1 = queryFactory.selectFrom(member)
                .where(member.username.like("member%")) // like member%
                .fetchFirst();

        Member findMember2 = queryFactory.selectFrom(member)
                .where(member.username.contains("member")) // like %member%
                .fetchFirst();

        Member findMember3 = queryFactory.selectFrom(member)
                .where(member.username.startsWith("member")) // like member%
                .fetchFirst();

        assertThat(findMember1).isNotNull();
        assertThat(findMember2).isNotNull();
        assertThat(findMember3).isNotNull();
    }
}
