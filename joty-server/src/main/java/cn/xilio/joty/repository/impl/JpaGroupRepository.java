package cn.xilio.joty.repository.impl;


import cn.xilio.joty.core.common.page.PageQuery;
import cn.xilio.joty.core.common.page.PageResponse;
import cn.xilio.joty.domain.dataobject.Group;
import cn.xilio.joty.repository.GroupRepository;
import cn.xilio.joty.repository.dao.GroupEntityRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Repository
public class JpaGroupRepository implements GroupRepository {
    @Resource
    private GroupEntityRepository groupEntityRepository;
    @Autowired
    private EntityManager entityManager;

    /**
     * Delete a group based on its ID
     *
     * @param groupId Group ID
     * @param userId  USER ID
     * @return Was the deletion successful
     */
    @Override
    public long deleteById(String groupId, String userId) {
        Specification<Group> spec = (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("userId"), userId),
                        cb.equal(root.get("id"), groupId)
                );
        return groupEntityRepository.delete(spec);
    }

    /**
     * Update group information based on group ID
     *
     * @param group Grouping entity
     * @return Is the update successful
     */
    @Override
    public Group saveGroup(Group group) {
        return groupEntityRepository.save(group);
    }

    /**
     * Retrieve all groups of the specified user
     *
     * @param query  Page param
     * @param userId USER ID
     * @return Grouped List
     */
    @Override
    public PageResponse<Group> getGroupsByUser(PageQuery query, String userId) {
        int page = query.getPage();
        int size = query.getSize();
        Specification<Group> spec = (root, query1, cb) ->
                cb.equal(root.get("userId"), userId);
        PageRequest pageRequest = PageRequest.of(
                page < 1 ? 0 : (page - 1),
                size,
                Sort.by(Sort.Direction.ASC, "sort")
        );
        Page<Group> entityPage = groupEntityRepository.findAll(spec, pageRequest);
        return PageResponse.of(
                entityPage.getContent(),
                (int) entityPage.getTotalElements(),
                entityPage.getNumber() + 1,
                entityPage.getSize(),
                entityPage.hasNext()
        );
    }


    /**
     * Query grouping based on grouping ID
     *
     * @param groupId Group ID
     * @param userId  User Id
     * @return Grouping entity, return null if it does not exist
     */
    @Override
    public Group getById(String groupId, String userId) {
        Specification<Group> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("id"), groupId));
            predicates.add(cb.equal(root.get("userId"), userId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return groupEntityRepository.findOne(spec).orElse(null);
    }

    /**
     * Get the number of groups for the specified user
     *
     * @param userId USER ID
     * @return Number of groups
     */
    @Override
    public long getCountByUser(String userId) {
        Specification<Group> spec = (root, query, cb) ->
                cb.equal(root.get("userId"), userId);
        return groupEntityRepository.count(spec);
    }

    @Override
    public void deleteBatch(List<String> ids, String userId) {
        Specification<Group> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("id").in(ids));
            predicates.add(cb.equal(root.get("userId"), userId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Group> groups = groupEntityRepository.findAll(spec);
        if (!groups.isEmpty()) {
            groupEntityRepository.deleteAll(groups);
        }
    }

    @Override
    public void addGroup(Group group) {
        String sql = """
                INSERT INTO t_group (
                    id, name, remark, sort, user_id, 
                     created_at, updated_at
                ) VALUES (
                    ?, ?, ?, ?, ?, 
                    ?, ?
                )
                """;
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        entityManager.createNativeQuery(sql)
                .setParameter(1, group.getId())
                .setParameter(2, group.getName())
                .setParameter(3, group.getRemark())
                .setParameter(4, group.getSort())
                .setParameter(5, group.getUserId())
                .setParameter(6, group.getCreatedAt())
                .setParameter(7, group.getUpdatedAt())
                .executeUpdate();
    }
}


