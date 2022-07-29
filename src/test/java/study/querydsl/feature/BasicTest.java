package study.querydsl.feature;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.MemberRole;
import study.querydsl.domain.Team;
import study.querydsl.repository.MemberRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.QMember.member;

@SpringBootTest
@Transactional
class BasicTest {

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
    @DisplayName("JPQL 기본 메소드")
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
    @DisplayName("Querydsl 기본 메소드")
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
    @DisplayName("상수 주입")
    void constant() {
        //given
        List<Tuple> members = queryFactory
                .select(
                        member.username, Expressions.constant("A")
                ).from(member)
                .fetch();

        //then
        assertThat(members).hasSize(4);
    }

    @Test
    @DisplayName("컬럼 concat")
    void concat() {
        //when
        List<String> concatStr = queryFactory
                .select(
                        member.username.concat("_").concat(member.age.stringValue())
                ).from(member)
                .fetch();

        //then
        assertThat(concatStr.get(0)).isEqualTo("member1_10");
    }

    @Test
    @DisplayName("Enum + concat")
    void enumConcat() {
        //given
        memberRepository.save(new Member("user", MemberRole.USER));
        memberRepository.save(new Member("admin", MemberRole.ADMIN));

        //when
        String role = queryFactory
                .select(
                        member.username.concat("_").concat(member.memberRole.stringValue())
                ).from(member)
                .where(member.memberRole.eq(MemberRole.ADMIN))
                .fetchOne();

        //then
        assertThat(role).isEqualTo("admin_ADMIN");
    }

    @Test
    @DisplayName("기본 Projection")
    void simpleProjection() {
        //when
        String findMember = queryFactory
                .select(member.username)
                .from(member)
                .where(member.age.eq(10))
                .fetchOne();

        //then
        assertThat(findMember).isEqualTo("member1");
    }
    
    @Test
    void bulkUpdate() {
        //when
        em.flush();
        em.clear();

        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.loe(20))
                .execute();

        List<Member> members = queryFactory.selectFrom(member)
                .fetch();

        //then
        assertThat(count).isEqualTo(2);

        for (Member mem : members) {
            if (mem.getAge() <= 20)
                assertThat(mem)
                    .extracting("username")
                    .isEqualTo("비회원");
        }
    }
}
