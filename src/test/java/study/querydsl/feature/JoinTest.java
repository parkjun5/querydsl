package study.querydsl.feature;

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
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.QMember.member;
import static study.querydsl.domain.QTeam.team;

@SpringBootTest
@Transactional
class JoinTest {

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
    @DisplayName("Basic Inner Join Test")
    void join() {
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
    @DisplayName("leftJoin Test")
    void leftJoin() {
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
    @DisplayName("연관 관계 없는 막 조인")
    void thetaJoin() {
        //given
        memberRepository.save(Member.createMember("TeamA", 15, null));
        memberRepository.save(Member.createMember("TeamB", 35, null));

        //when
        List<Member> fetch = queryFactory
                .select(member)
                .from(member, team)
                .where(team.name.eq(member.username))
                .fetch();
        //then
        assertThat(fetch)
                .as("이름 확인")
                .extracting("username")
                .containsExactly("TeamA", "TeamB");

    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 TeamA인 팀만 조인 회원은 모두 조회
     * JPQL : select m, t form Member m left join m.team on t.name = 'TeamA'
     */
    @Test
    @DisplayName("Join + on 절로 조건추가 레프트조인 전용")
    void joinOnFiltering() {
        //given
        List<Tuple> tuples = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("TeamA"))
                .fetch();

        //then
        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);

            System.out.println("tuple.get(team) = " + tuple.get(team));
            if (tuple.get(team) == null) {
                assertThat(tuple.get(member))
                        .extracting("username")
                        .isIn("member3", "member4");
            } else {
                assertThat(tuple.get(team))
                        .extracting("name")
                        .isEqualTo("TeamA");
            }
        }

    }

    /**
     * 연관 관계가 없는 엔티티 외부 조왼
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    @DisplayName("연관 관계없는 엔티티 외부 조인")
    void joinOnNoRelation() {
        //given
        memberRepository.save(Member.createMember("TeamA", 99, null));
        memberRepository.save(Member.createMember("TeamB", 99, null));
        memberRepository.save(Member.createMember("TeamC", 99, null));

        //when
        List<Tuple> tuples = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        //then
        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);
            if (tuple.get(team) != null) {
                assertThat(tuple.get(member))
                        .extracting("username")
                        .isEqualTo(Objects.requireNonNull(tuple.get(team)).getName());
            } else {
                assertThat(tuple.get(member))
                        .extracting("username")
                        .isNotIn("TeamA", "TeamB");
            }
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    @DisplayName("Fetch Join Lazy 로딩을 강제로 끌고오게하기")
    void fetchJoin() {
        em.flush();
        em.clear();
        //given
        List<Member> members = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .fetch();
        //then
        for (Member findMember : members) {
            boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
            assertThat(loaded).as("페치 조인 적용").isTrue();
        }
    }
}
