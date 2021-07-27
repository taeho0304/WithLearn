package com.ssafy.db.repository;

import com.ssafy.db.entity.ConferenceCategory;
import com.ssafy.db.entity.UserConference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * 방 모델 관련 디비 쿼리 생성을 위한 JPA Query Method 인터페이스 정의.
 */
@Repository
public interface ConferenceCategoryRepository extends JpaRepository<ConferenceCategory, Long> {

}