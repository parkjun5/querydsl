package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
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

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.domain.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext EntityManager em;

    @Autowired MemberRepository memberRepository;

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
    void startJPQL() throws Exception {
        //given
        String memberName = "member1";
        String qlString = "select m from Member m" +
                " where m.username = :username";

        Member findMemberByJpql = em.createQuery(qlString, Member.class)
                .setParameter("username", memberName)
                .getSingleResult();

        //then
        assertThat(findMemberByJpql.getUsername()).isEqualTo(memberName);
    }

    @Test
    void startQuerydsl() throws Exception {
        //given
        // QMember qMember = new QMember("member"); 같은 테이블 조인의 경우 사용
        //when
        Member findMemberByQuerydsl = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        //then
        assert findMemberByQuerydsl != null;
        assertThat(findMemberByQuerydsl.getAge()).isEqualTo(10);

    }

    @Test
    @DisplayName("검색조건: Equals / Not Equals")
    void search1() throws Exception {
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
    void search2() throws Exception {
        //given
        Member findMember = queryFactory.selectFrom(member)
                .where(member.age.between(15, 50))
                .fetchFirst();

        assertThat(findMember).isNotNull();
    }

    @Test
    @DisplayName("검색조건: Not null")
    void search3() throws Exception {
        //given
        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.isNotNull())
                .fetchFirst();

        assertThat(findMember).isNotNull();
    }

    @Test
    @DisplayName("검색조건: In Not In")
    void search4() throws Exception {
        //given
        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.in("mem", "mem1", "member1")
                        .and(member.username.notIn("m1","m2")))
                .fetchOne();

        assertThat(findMember).isNotNull();
    }

    @Test
    @DisplayName("검색조건: geo gt loe lt 비교")
    void search6() throws Exception {
        //given
        Member findMember = queryFactory.selectFrom(member)
                .where(member.age.goe(21))
                .fetchFirst();
        //geo >= gt > loe <= lt <
        assertThat(findMember).isNotNull();
    }

    @Test
    @DisplayName("검색조건: Like, Contains, StartWith ")
    void search7() throws Exception {
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
    @Test
    @DisplayName("And 사용 안하기 null은 무시가 된다. ")
    void searchAndParam() throws Exception {
        //given
        Member findMember = queryFactory.selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        (member.age.ne(15)), null
                )
                .fetchOne();

        assertThat(findMember).isNotNull();
    }

    @Test
    void resultFetch() throws Exception {
        //given
        List<Member> members = queryFactory
                .selectFrom(member)
                .fetch();
        //when

        //then
        assertThat(members.size()).isEqualTo(4);
    }

    /**
     *  회원 정렬 순서
     *  1. 회원 나이 내림차순
     *  2. 이름 올림차순
     *   단 이름이 없으면 마지막에 출력 null이면 last
     */
    @Test
    void sort() throws Exception {
        //given
        memberRepository.save(Member.createMember(null, 100, null));
        memberRepository.save(Member.createMember("member5", 100, null));
        memberRepository.save(Member.createMember("member6", 100, null));

        //when
        List<Member> members = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.desc().nullsLast())
                .fetch();
        //then
        for (Member member : members) {
            System.out.println("member = " + member);
        }

        assertThat(members.size()).isEqualTo(3);
    }
    
    @Test
    void paging1() throws Exception {
        //given
        List<Member> members = queryFactory
                .selectFrom(member)
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .offset(1)
                .limit(2)
                .fetch();

        //when

        //then
        assertThat(members.size()).isEqualTo(2);
        assertThat(members.get(0).getUsername()).isEqualTo("member3");
    }

}
