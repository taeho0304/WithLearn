package com.ssafy.db.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.db.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 방 모델 관련 디비 쿼리 생성을 위한 구현 정의.
 */
@Repository
public class ConferenceRepositorySupport {
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    QConference qConference = QConference.conference;
    QUser quser = QUser.user;
    QUserConference qUserConference = QUserConference.userConference;
    QConferenceCategory qConferenceCategory = QConferenceCategory.conferenceCategory;

    public Optional<List<ConferenceCategory>> findCategories() {
        List<ConferenceCategory> categoryList = jpaQueryFactory.select(qConferenceCategory).from(qConferenceCategory).fetch();
        return Optional.ofNullable(categoryList);
    }

    public Optional<Conference> findConferenceByConferenceId(Long conferenceId) {
        Conference conference = jpaQueryFactory.select(qConference).from(qConference)
                .where(qConference.id.eq(conferenceId)).fetchOne();
        if(conference == null) return Optional.empty();
        return Optional.ofNullable(conference);
    }

    public Optional<List<Conference>> findConferences(String title, String sort, Integer size, Long conferenceCategory, String userName) {
        JPAQuery<Conference> conferences = jpaQueryFactory.select(qConference).from(qConference);

        // where 절
        if ( title != null && conferenceCategory == null && userName == null) conferences.where(qConference.title.eq(title));
        else if ( title == null && conferenceCategory != null && userName == null) conferences.where(qConference.conferenceCategory.id.eq(conferenceCategory));
        else if ( title == null && conferenceCategory == null && userName != null) conferences.where(quser.name.eq(userName));
        else if ( title != null && conferenceCategory != null && userName == null) conferences.where(qConference.title.eq(title), qConference.conferenceCategory.id.eq(conferenceCategory));
        else if ( title == null && conferenceCategory != null && userName != null) conferences.where(qConference.conferenceCategory.id.eq(conferenceCategory), quser.name.eq(userName));
        else if ( title != null && conferenceCategory == null && userName != null) conferences.where(qConference.title.eq(title), quser.name.eq(userName));
        else if ( title != null && conferenceCategory != null && userName != null) conferences.where(qConference.title.eq(title), qConference.conferenceCategory.id.eq(conferenceCategory), quser.name.eq(userName));

        // 정렬
        if(sort!=null) {
            String[] sortOption = sort.split(",");
            if (sortOption[1].equals("asc") && sortOption[0].equals("title"))
                conferences.orderBy(qConference.title.asc());
            else if (sortOption[1].equals("desc") && sortOption[0].equals("title"))
                conferences.orderBy(qConference.title.desc());
//            else if (sortOption[1].equals("asc") && sortOption[0].equals("call_start_time"))
//                conferences.orderBy(qConference.callStartTime.asc());
//            else if (sortOption[1].equals("desc") && sortOption[0].equals("call_start_time"))
//                conferences.orderBy(qConference.callStartTime.desc());
            //마감, is_Free, is_active, 리뷰순 정렬, rate순
        }
        // size
        if(size != null) conferences.limit(size);

        if(conferences == null) return Optional.empty();
        return Optional.ofNullable(conferences.fetch());
    }

    public Optional<ConferenceCategory> findCategoriesByName(String name) {
        ConferenceCategory conferenceCategory = jpaQueryFactory.select(qConferenceCategory).from(qConferenceCategory)
                .where(qConferenceCategory.name.eq(name)).fetchOne();
        if(conferenceCategory == null) return Optional.empty();
        return Optional.ofNullable(conferenceCategory);
    }

    public Optional<List<UserConference>> findUserConferenceByConferenceId(Long conference_id) {
        List<UserConference> userConferences = jpaQueryFactory.select(qUserConference).from(qUserConference)
                .where(qUserConference.conference.id.eq(conference_id)).fetch();
        if(userConferences == null) return Optional.empty();
        return Optional.ofNullable(userConferences);
    }
}