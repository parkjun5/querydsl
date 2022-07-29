package study.querydsl.feature;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;
import study.querydsl.domain.Team;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.repository.MemberRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.QMember.member;

@SpringBootTest
@Transactional
public class DtoTest {

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
    @DisplayName("Dto Projection Setter 생성")
    void dtoProjectionWithSetter() {
        //given
        QMember subMember = new QMember("subMember");

        //when
        MemberDto memberDto = queryFactory
                .select(Projections.bean(
                        MemberDto.class,
                        member.username,
                        member.age
                )).from(member)
                .where(member.age.eq(
                        select(subMember.age.max())
                                .from(subMember)
                ))
                .fetchOne();

        //then
        assertThat(memberDto)
                .extracting("age")
                .isEqualTo(40);
        assertThat(memberDto)
                .extracting("username")
                .isEqualTo("member4");
    }

    @Test
    @DisplayName("Dto Projection 필드 생성")
    void dtoProjectionWithField() {
        //given
        QMember subMember = new QMember("subMember");

        //when
        MemberDto memberDto = queryFactory
                .select(Projections.fields(
                        MemberDto.class,
                        member.username,
                        member.age
                )).from(member)
                .where(member.age.eq(
                        select(subMember.age.min())
                                .from(subMember)
                ))
                .fetchOne();

        //then
        assertThat(memberDto)
                .extracting("age")
                .isEqualTo(10);
        assertThat(memberDto)
                .extracting("username")
                .isEqualTo("member1");
    }

    @Test
    @DisplayName("Dto Projection 생성자 생성")
    void dtoProjectionWithConstructor() {
        //given
        QMember subMember = new QMember("subMember");

        //when
        UserDto userDto = queryFactory
                .select(Projections.constructor(
                        UserDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(
                                select(subMember.age.avg().intValue())
                                        .from(subMember), "userAge")
                )).from(member)
                .where(member.age.eq(
                        select(subMember.age.min())
                                .from(subMember)
                ))
                .fetchOne();

        //then
        assertThat(userDto)
                .extracting("userAge")
                .isEqualTo(25);
        assertThat(userDto)
                .extracting("name")
                .isEqualTo("member1");
    }

    @Test
    @DisplayName("Dto 내부 객체에 생성자 @QueryProjection 추가로 QDto 생성 ")
    void findDtoByQueryProjection() {
        //given
        List<MemberDto> memberDtoList = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();
        //when
        String username = memberDtoList.stream()
                .filter(dto -> dto.getAge() == 30)
                .map(MemberDto::getUsername)
                .collect(toSingleton());
        //then
        assertThat(username).isEqualTo("member3");
    }

    public static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }

    @Test
    @DisplayName("Dto Projection 검색 + distinct")
    void findDtoWithDistinct() {
        //given
        memberRepository.save(Member.createMember("members1", 10, null));
        memberRepository.save(Member.createMember("members2", 20, null));
        memberRepository.save(Member.createMember("members3", 30, null));
        memberRepository.save(Member.createMember("members4", 40, null));
        em.flush();
        em.clear();

        //when
        List<MemberDto> memberDtoList = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.age))
                .distinct()
                .from(member)
                .fetch();
        //then
        assertThat(memberDtoList).hasSize(4);
    }
}
