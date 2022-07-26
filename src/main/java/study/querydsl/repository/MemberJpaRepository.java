package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.domain.Member;
 import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;
import static study.querydsl.domain.QMember.*;
import static study.querydsl.domain.QTeam.*;

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

    public List<MemberTeamDto> searchByBuilder(MemberSearchCond memberSearchCond) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (StringUtils.hasText(memberSearchCond.getUsername())) {
            booleanBuilder.and(member.username.eq(memberSearchCond.getUsername()));
        }

        if (StringUtils.hasText(memberSearchCond.getTeamName())) {
            booleanBuilder.and(team.name.eq(memberSearchCond.getTeamName()));
        }

        if (memberSearchCond.getAgeGoe() != null) {
            booleanBuilder.and(member.age.goe(memberSearchCond.getAgeGoe()));
        }

        if (memberSearchCond.getAgeLoe() != null) {
            booleanBuilder.and(member.age.loe(memberSearchCond.getAgeLoe()));
        }

        return queryFactory.select(new QMemberTeamDto(
                                member.id.as("memberId"),
                                member.username,
                                member.age,
                                team.id.as("teamId"),
                                team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(booleanBuilder)
                .fetch();
    }

    public List<MemberTeamDto> searchByWhere(MemberSearchCond memberSearchCond) {
        return queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .join(member.team, team)
                .where(
                        usernameEp(memberSearchCond.getUsername()),
                        teamNameEp(memberSearchCond.getTeamName()),
                        ageGoe(memberSearchCond.getAgeGoe()),
                        ageLoe(memberSearchCond.getAgeLoe())
                        )
                .fetch();
    }

    private BooleanExpression usernameEp(String username) {
        return isEmpty(username) ? null : member.username.eq(username);
    }

    private BooleanExpression teamNameEp(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
