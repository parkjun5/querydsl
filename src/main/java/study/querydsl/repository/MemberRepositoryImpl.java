package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;
import static study.querydsl.domain.QMember.member;
import static study.querydsl.domain.QTeam.*;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberTeamDto> findAllQuerydsl() {
        return queryFactory.select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
                )).from(member)
                .leftJoin(member.team, team)
                .fetch();
    }

    @Override
    public List<MemberTeamDto> findByUsernameQuery(String username) {
        return queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(member.username.eq(username))
                .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCond memberSearchCond, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory.select(new QMemberTeamDto(
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = queryFactory
                .select(member.count())
                .from(member)
                .join(member.team, team)
                .where(
                        usernameEp(memberSearchCond.getUsername()),
                        teamNameEp(memberSearchCond.getTeamName()),
                        ageGoe(memberSearchCond.getAgeGoe()),
                        ageLoe(memberSearchCond.getAgeLoe())
                ).fetchOne();
        count =  count != null ? count : 0L;

        return new PageImpl<>(content, pageable, count);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCond memberSearchCond, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory.select(new QMemberTeamDto(
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(member.count())
                .from(member)
                .join(member.team, team)
                .where(
                        usernameEp(memberSearchCond.getUsername()),
                        teamNameEp(memberSearchCond.getTeamName()),
                        ageGoe(memberSearchCond.getAgeGoe()),
                        ageLoe(memberSearchCond.getAgeLoe())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
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
