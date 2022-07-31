package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.Team;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired MemberJpaRepository memberJpaRepository;

    @PersistenceContext EntityManager em;

    @BeforeEach
    void testMemberSetting() {
        Member testMember = Member.createMember("tester", 1, null);
        memberJpaRepository.save(testMember);
        영속성컨텐츠초기화();
    }


    @Test
    void saveTest() {
        //given
        Member testMember = Member.createMember("testUser", 888, null);

        //when
        Long savedId = memberJpaRepository.save(testMember);
        영속성컨텐츠초기화();

        //then
        Member findMember = em.find(Member.class, savedId);
        assertThat(findMember).isNotNull();
        assertThat(findMember).isEqualTo(testMember);
    }

    @Test
    void findByIdTest() throws Exception {
        //given
        Member tester2 = Member.createMember("tester2", 2, null);
        memberJpaRepository.save(tester2);
        영속성컨텐츠초기화();
        //when
        Member findMember = memberJpaRepository.findById(tester2.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        //then
        assertThat(findMember).isEqualTo(tester2);
        assertThat(findMember)
                .extracting("age")
                .isEqualTo(2);
    }

    @Test
    void findById_But_NotFoundTest() throws Exception {
        //given
        Long memberId = 999L;
        //when

        //then
        assertThrows(IllegalArgumentException.class,
                () -> memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원의 아이디입니다.")));
    }

    @Test
    void findAllTest() throws Exception {
        //given
        List<Member> all = memberJpaRepository.findAll();
        //when

        //then
        assertThat(all).hasSize(1);
        assertThat(all)
                .extracting("username")
                .doesNotContain("m1", "test2", "111");
    }

    @Test
    void findByUsernameTest() throws Exception {
        //given
        List<Member> members = memberJpaRepository.findByUsername("ㅁㄴㅇㅁㄴㅇ");
        //when

        //then
        assertThat(members).hasSize(0);
    }

    @Test
    void querydslFindAllTest() throws Exception {
        //given
        List<Member> allQuerydsl = memberJpaRepository.findAllQuerydsl();
        //when

        //then
        assertThat(allQuerydsl).hasSize(1);
    }

    @Test
    void querydslFindByUsernameTest() throws Exception {
        //given
        List<Member> byUsernameQuery = memberJpaRepository.findByUsernameQuery("tester");
        //when

        //then
        assertThat(byUsernameQuery.get(0).getUsername()).isEqualTo("tester");
    }

    @Test
    void searchByBooleanBuilderTest() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Team TeamB = new Team("teamB");
        em.persist(TeamB);

        Member member1 = Member.createMember("member1", 10, teamA);
        Member member2 = Member.createMember("member2", 20, teamA);

        Member member3 = Member.createMember("member3", 30, TeamB);
        Member member4 = Member.createMember("member4", 40, TeamB);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        memberJpaRepository.save(member3);
        memberJpaRepository.save(member4);
        영속성컨텐츠초기화();

        //when
        MemberSearchCond memberSearchCond = new MemberSearchCond();
        memberSearchCond.setAgeGoe(15);
        memberSearchCond.setTeamName("teamB");
        memberSearchCond.setAgeLoe(40);
        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByBuilder(memberSearchCond);

        //then
        assertThat(memberTeamDtos).hasSize(2);
        assertThat(memberTeamDtos)
                .extracting("username")
                .containsExactly("member3", "member4");

    }

    @Test
    void searchByWhereTest() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Team TeamB = new Team("teamB");
        em.persist(TeamB);

        Member member1 = Member.createMember("member1", 10, teamA);
        Member member2 = Member.createMember("member2", 20, teamA);

        Member member3 = Member.createMember("member3", 30, TeamB);
        Member member4 = Member.createMember("member4", 40, TeamB);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        memberJpaRepository.save(member3);
        memberJpaRepository.save(member4);
        영속성컨텐츠초기화();

        //when
        MemberSearchCond memberSearchCond = new MemberSearchCond();
        memberSearchCond.setAgeGoe(20);
        memberSearchCond.setTeamName("teamA");
        memberSearchCond.setAgeLoe(40);

        List<MemberTeamDto> memberTeamDtoList = memberJpaRepository.searchByWhere(memberSearchCond);

        //then
        assertThat(memberTeamDtoList).hasSize(1);
    }

    private void 영속성컨텐츠초기화() {
        em.flush();
        em.clear();
    }
}