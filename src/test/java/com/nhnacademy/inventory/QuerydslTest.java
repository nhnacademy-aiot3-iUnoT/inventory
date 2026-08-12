package com.nhnacademy.inventory;

import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.QMedicine;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuerydslTest {

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("Medicine 엔티티 저장 및 Querydsl 동적/조건 조회 테스트")
    void medicineQuerydslTest() {

//        Medicine medicine = Medicine.builder()
//                .itemCode("MED-001")
//                .productName("타이레놀정500밀리그램")
//                .storageMethod("기밀용기, 室溫보관")
//                .companyName("한국존슨앤드존슨")
//                .build();

        Medicine medicine = Medicine.create(
                "MED-001",
                "타이레놀정500밀리그램",
                "기밀용기, 室溫보관",
                "12개월",
                null,
                "한국존슨앤드존슨"
        );


        em.persist(medicine);
        em.flush();
        em.clear();

        // when: QMedicine을 활용한 Querydsl 조회
        QMedicine qMedicine = QMedicine.medicine;

        List<Medicine> result = queryFactory
                .selectFrom(qMedicine)
                .where(qMedicine.productName.contains("타이레놀"))
                .fetch();

        // then: 검증
        assertThat(queryFactory).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getItemCode()).isEqualTo("MED-001");
        assertThat(result.getFirst().getCompanyName()).isEqualTo("한국존슨앤드존슨");
    }
}