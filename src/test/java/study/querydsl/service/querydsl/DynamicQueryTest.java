package study.querydsl.service.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
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

@SpringBootTest
@Transactional
class DynamicQueryTest {

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
    @DisplayName("동적쿼리 + BooleanBuilder")
    void dynamicQueryBooleanBuilder() {
        //given
        String usernameParam = "member1";
        Integer ageParam = null;

        //when
        List<Member> result = searchMember1(usernameParam, ageParam);

        //then
        assertThat(result).hasSize(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    @DisplayName("동적쿼리 where 문에서 처리 재활용 가능")
    void dynamicQueryWhereParam() {
        //given
        String usernameParam = "member1";
        Integer ageParam = null;

        //when
        List<Member> result = searchMember2(usernameParam, ageParam);

        //then
        assertThat(result).hasSize(1);
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return queryFactory
                .selectFrom(member)
                .where(allEq(usernameParam, ageParam))
                .fetch();
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam != null ? member.age.eq(ageParam) : null;
    }

    private BooleanExpression userNameEq(String usernameParam) {
        return usernameParam != null ? member.username.eq(usernameParam) : null;
    }

    private Predicate allEq(String usernameParam, Integer ageParam) {
        return userNameEq(usernameParam).and(ageEq(ageParam));
    }
}
