package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;
import static study.querydsl.domain.QMember.member;
import static study.querydsl.domain.QTeam.team;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;
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
