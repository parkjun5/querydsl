package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.QTestEntity;
import study.querydsl.domain.TestEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@PersistenceContext
	EntityManager em;

	@Test
	void contextLoads() {
	}

	@Test
	void queryDslTest() throws Exception {
	    //given
		TestEntity test = new TestEntity("test");
		em.persist(test);
		em.flush();
		em.clear();
		//when
		QTestEntity qTestEntity = QTestEntity.testEntity;
		JPAQueryFactory query = new JPAQueryFactory(em);
	    //then
		TestEntity testEntity = query.selectFrom(qTestEntity)
				.fetchOne();

		assertThat(testEntity).isInstanceOf(TestEntity.class);
		assert testEntity != null;
		assertThat(testEntity.getId()).isEqualTo(test.getId());

	}

}
