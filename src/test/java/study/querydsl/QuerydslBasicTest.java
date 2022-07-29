package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.Team;
import study.querydsl.repository.MemberRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.QMember.member;
import static study.querydsl.domain.QTeam.team;

@SpringBootTest
@Transactional
class QuerydslBasicTest {

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
    void startJPQL() {
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
    void startQuerydsl() {
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
                        .and(member.username.notIn("m1","m2")))
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
    @Test
    @DisplayName("And 사용 안하기 null 값은 무시가 된다. ")
    void searchAndParam() {
        //given
        Member findMember = queryFactory.selectFrom(member)
                .where(
                        member.id.eq(1L),
                        member.age.ne(15), null
                )
                .fetchOne();
        assert findMember != null;

        Member findMember2 = queryFactory.selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        ageIsNot15(findMember), null
                )
                .fetchOne();


        assertThat(findMember).isNotNull();
        assertThat(findMember2).isNotNull();

    }

    private static BooleanExpression ageIsNot15(Member findMember) {
        if (findMember.getAge() == 0) {
            return  null;
        }
        return member.age.ne(15);
    }

    @Test
    void resultFetch() {
        //given
        List<Member> members = queryFactory
                .selectFrom(member)
                .fetch();
        //when

        //then
        assertThat(members).hasSize(4);
    }

    /**
     *  회원 정렬 순서
     *  1. 회원 나이 내림차순
     *  2. 이름 올림차순
     *   단 이름이 없으면 마지막에 출력 null 이면 last
     */
    @Test
    void sort() {
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

        assertThat(members).hasSize(3);
    }
    
    @Test
    void paging1() {
        //given
        List<Member> members = queryFactory
                .selectFrom(member)
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .offset(1)
                .limit(2)
                .fetch();

        //when

        //then
        assertThat(members).hasSize(2);
        assertThat(members.get(0).getUsername()).isEqualTo("member3");
    }
    
    @Test
    void aggregation() {
        //given
        List<Tuple> fetch = queryFactory
                .select(
                        member.count(),
                        member.age.avg(),
                        member.age.sum(),
                        member.age.max(),
                        member.age.min()
                ).from(member)
                .fetch();
        //when 
        Tuple tuple = fetch.get(0);

        //then
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    void group() {
        //given
        List<Tuple> fetch = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(team.name.eq("TeamA"))
                .fetch();
        //when
        Tuple tuple = fetch.get(0);

        //then
        assertThat(tuple.get(team.name)).isEqualTo("TeamA");
        assertThat(tuple.get(member.age.avg())).isEqualTo(15);
        assertThat(tuple.get(member.count())).isNull();
    }

    @Test
    void join() throws Exception {
        //given
        List<Member> members = queryFactory.selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("TeamA"))
                .fetch();

        //then
        assertThat(members)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    @Test
    void leftJoin() throws Exception {
        //given
        List<Member> sameAsWhereResult = queryFactory.selectFrom(member)
                .join(member.team, team).on(team.name.eq("TeamA"))
                .fetch();

        List<Member> fullMembers = queryFactory.selectFrom(member)
                .leftJoin(member.team, team).on(team.name.eq("TeamA"))
                .fetch();

        //then
        System.out.println("sameAsWhereResult = " + sameAsWhereResult);
        assertThat(sameAsWhereResult)
                .extracting("username")
                .containsExactly("member1", "member2");
        System.out.println("fullMembers = " + fullMembers);
        assertThat(fullMembers)
                .extracting("username")
                .containsExactly("member1", "member2", "member3", "member4");
    }

    @Test
    void thetaJoin() throws Exception {
        //given
        memberRepository.save(Member.createMember("TeamA", 15, null));
        memberRepository.save(Member.createMember("TeamB", 35, null));

        //when
        queryFactory
                .select(member)
                .from(member, team)
                .where(team.name.eq(member.username))
                .fetch();
        //then
    }

}
