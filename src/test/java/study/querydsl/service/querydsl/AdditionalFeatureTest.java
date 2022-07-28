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
class AdditionalFeatureTest {
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
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순
     * 2. 이름 올림차순
     * 단 이름이 없으면 마지막에 출력 null 이면 last
     */
    @Test
    @DisplayName("Sort 정렬")
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
    @DisplayName("페이징 offset and limit")
    void paging1() {
        //given
        List<Member> members = queryFactory
                .selectFrom(member)
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .offset(1)
                .limit(2)
                .fetch();

        //then
        assertThat(members).hasSize(2);
        assertThat(members.get(0).getUsername()).isEqualTo("member3");
    }

    @Test
    @DisplayName("sql 기본 메소드 count avg sum max min")
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
    @DisplayName("Group By and Having")
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
}
