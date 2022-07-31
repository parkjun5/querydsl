package study.querydsl.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.Team;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberQueryRepository memberQueryRepository;

    @PersistenceContext
    EntityManager em;

    @BeforeEach
    void testMemberSetting() {
        Member testMember = Member.createMember("tester", 1, null);
        memberRepository.save(testMember);
        영속성컨텐츠초기화();
    }


    @Test
    void saveTest() {
        //given
        Member testMember = Member.createMember("testUser", 888, null);

        //when
        Member savedMember = memberRepository.save(testMember);
        영속성컨텐츠초기화();

        //then
        Member findMember = em.find(Member.class, savedMember.getId());
        assertThat(findMember).isNotNull();
        assertThat(findMember).isEqualTo(testMember);
    }

    @Test
    void findByIdTest() throws Exception {
        //given
        Member tester2 = Member.createMember("tester2", 2, null);
        memberRepository.save(tester2);
        영속성컨텐츠초기화();
        //when
        Member findMember = memberRepository.findById(tester2.getId())
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
                () -> memberRepository.findById(memberId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원의 아이디입니다.")));
    }

    @Test
    void findAllTest() throws Exception {
        //given
        List<Member> all = memberRepository.findAll();
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
        List<Member> members = memberRepository.findByUsername("ㅁㄴㅇㅁㄴㅇ");
        //when

        //then
        assertThat(members).hasSize(0);
    }

    @Test
    void querydslFindAllTest() throws Exception {
        //given
        List<MemberTeamDto> allQuerydsl = memberRepository.findAllQuerydsl();
        //when

        //then
        assertThat(allQuerydsl).hasSize(1);
    }

    @Test
    void querydslFindByUsernameTest() throws Exception {
        //given
        List<MemberTeamDto> tester = memberRepository.findByUsernameQuery("tester");
        //when

        //then
        assertThat(tester.get(0).getUsername()).isEqualTo("tester");
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
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
        영속성컨텐츠초기화();

        //when
        MemberSearchCond memberSearchCond = new MemberSearchCond();
        memberSearchCond.setAgeGoe(15);
        memberSearchCond.setTeamName("teamB");
        memberSearchCond.setAgeLoe(40);
        List<MemberTeamDto> memberTeamDtos = memberQueryRepository.searchByBuilder(memberSearchCond);

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
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
        영속성컨텐츠초기화();

        //when
        MemberSearchCond memberSearchCond = new MemberSearchCond();
        memberSearchCond.setAgeGoe(20);
        memberSearchCond.setTeamName("teamA");
        memberSearchCond.setAgeLoe(40);

        List<MemberTeamDto> memberTeamDtoList = memberQueryRepository.searchByWhere(memberSearchCond);

        //then
        assertThat(memberTeamDtoList).hasSize(1);
    }

    @Test
    void searchPagingTest() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Team TeamB = new Team("teamB");
        em.persist(TeamB);

        Member member1 = Member.createMember("member1", 10, teamA);
        Member member2 = Member.createMember("member2", 20, teamA);

        Member member3 = Member.createMember("member3", 30, TeamB);
        Member member4 = Member.createMember("member4", 40, TeamB);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
        영속성컨텐츠초기화();

        //when
        MemberSearchCond memberSearchCond = new MemberSearchCond();

        PageRequest pageRequest = PageRequest.of(1, 2);

        Page<MemberTeamDto> result = memberRepository.searchPageSimple(memberSearchCond, pageRequest );

        //then
        System.out.println("result = " + result.getContent());

        assertThat(result).hasSize(2);
        assertThat(result.getContent())
                .extracting("username")
                .containsExactly("member3", "member4");
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void searchPageComplexTest() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Team TeamB = new Team("teamB");
        em.persist(TeamB);

        Member member1 = Member.createMember("member1", 10, teamA);
        Member member2 = Member.createMember("member2", 20, teamA);

        Member member3 = Member.createMember("member3", 30, TeamB);
        Member member4 = Member.createMember("member4", 40, TeamB);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
        영속성컨텐츠초기화();

        //when
        MemberSearchCond memberSearchCond = new MemberSearchCond();

        PageRequest pageRequest = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "username"));

        Page<MemberTeamDto> result = memberRepository.searchPageComplex(memberSearchCond, pageRequest );

        //then
        System.out.println("result = " + result.getContent());

        assertThat(result).hasSize(2);
        assertThat(result.getContent())
                .extracting("username")
                .containsExactly("member3", "member4");
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();
    }

    private void 영속성컨텐츠초기화() {
        em.flush();
        em.clear();
    }
}
