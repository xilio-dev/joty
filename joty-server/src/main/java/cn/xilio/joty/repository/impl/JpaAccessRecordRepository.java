package cn.xilio.joty.repository.impl;


import cn.xilio.joty.adapter.portal.dto.response.StatsResponse;
import cn.xilio.joty.core.common.page.PageResponse;
import cn.xilio.joty.domain.dataobject.AccessRecord;
import cn.xilio.joty.domain.dataobject.ShortUrl;
import cn.xilio.joty.domain.model.DailyStatsDTO;
import cn.xilio.joty.repository.AccessRecordRepository;
import cn.xilio.joty.repository.dao.AccessRecordEntityRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class JpaAccessRecordRepository implements AccessRecordRepository {
    @Resource
    private AccessRecordEntityRepository accessRecordEntityRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<StatsResponse> findStatsCountByType(String shortCode, String type) {
        String sql = """
                    SELECT 
                        :type AS type,
                        ANY_VALUE(CASE :type
                            WHEN 'IP' THEN ip_address
                            WHEN 'OS' THEN COALESCE(os,'Unknown')
                            WHEN 'BROWSER' THEN COALESCE(browser,'Unknown')
                            WHEN 'DEVICE' THEN COALESCE(device_type,'Unknown')
                            WHEN 'COUNTRY' THEN COALESCE(country,'Unknown')
                            WHEN 'PROVINCE' THEN COALESCE(province,'未知')
                            ELSE 'unknown'
                        END) AS name,
                        COUNT(id) AS count
                    FROM access_record
                    WHERE short_code = :shortCode AND (:type != 'PROVINCE' OR country = '中国')
                    GROUP BY 
                        CASE :type
                            WHEN 'IP' THEN ip_address
                            WHEN 'OS' THEN os
                            WHEN 'BROWSER' THEN browser
                            WHEN 'COUNTRY' THEN country
                            WHEN 'DEVICE' THEN device_type
                            WHEN 'PROVINCE' THEN province
                            ELSE 'unknown'
                        END
                    ORDER BY count DESC
                    LIMIT 10
                """;

        // 创建原生查询并手动映射结果
        List<Object[]> results = entityManager.createNativeQuery(sql)
                .setParameter("type", type)
                .setParameter("shortCode", shortCode)
                .getResultList();

        // 转换为StatsResponse对象
        return results.stream()
                .map(row -> new StatsResponse(
                        (String) row[0],  // type
                        (String) row[1],  // name
                        ((Number) row[2]).longValue()  // count
                ))
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<AccessRecord> findAccessRecords(String shortCode, int page, int size) {

        // 3. 动态条件组装
        Specification<AccessRecord> spec = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("shortCode"), shortCode);

            return predicate;
        };
        PageRequest pageRequest = PageRequest.of(page < 1 ? 0 : (page - 1), size, Sort.by(Sort.Direction.DESC, "accessTime"));
        Page<AccessRecord> entityPage = accessRecordEntityRepository.findAll(spec, pageRequest);
        return PageResponse.of(
                entityPage.getContent(),
                (int) entityPage.getTotalElements(),
                entityPage.getNumber() + 1,
                entityPage.getSize(),
                entityPage.hasNext()
        );
    }

    @Override
    public AccessRecord saveAccessRecord(AccessRecord record) {
        return accessRecordEntityRepository.save(record);
    }

    @Override
    public boolean existsByIpAddressAndUserAgent(String shortCode, String ipAddress, String userAgent) {
        return accessRecordEntityRepository.existsByIpAddressAndUserAgentAndShortCode(ipAddress, userAgent,shortCode);
    }

    @Override
    public List<DailyStatsDTO> getDailyAccessStats(String startDate, String endDate, String shortCode) {
        return accessRecordEntityRepository.getRawDailyStats(startDate,endDate,shortCode).stream()
                .map(row -> {
                    Date rawDate = row[0] != null ?
                            new java.util.Date(((java.sql.Date)row[0]).getTime()) : null;
                    return new DailyStatsDTO(
                            rawDate,
                            ((Number)row[1]).longValue(),
                            ((Number)row[2]).longValue()
                    );
                })
                .collect(Collectors.toList());
    }
}

