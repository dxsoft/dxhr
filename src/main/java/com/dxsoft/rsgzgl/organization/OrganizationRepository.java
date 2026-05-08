package com.dxsoft.rsgzgl.organization;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.SqlText;
import com.dxsoft.rsgzgl.security.OrganizationScope;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class OrganizationRepository {

    private final NamedParameterJdbcTemplate jdbc;

    OrganizationRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    List<OrganizationSummary> findAll(String keyword, OrganizationScope organizationScope, PageRequest pageRequest) {
        if (organizationScope.noneScope()) {
            return List.of();
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes())
                .addValue("limit", pageRequest.size())
                .addValue("offset", pageRequest.offset());

        String where = " where (:allOrganizations = TRUE OR o.dwbm IN (:organizationCodes))";
        if (keyword != null && !keyword.isBlank()) {
            where += " and (o.dwbm like :keyword or o.dwmc like :keyword or o.dwmc1 like :keyword)";
            parameters.addValue("keyword", "%" + keyword.trim() + "%");
        }

        return jdbc.query(
                """
                select o.id, o.dwbm, o.dwmc, o.dwmc1, o.dwsx, o.dwbz, o.bzrs,
                       (select count(*) from dryjbxx p where p.dwbm = o.dwbm) as active_personnel_count
                from dwbm o
                """ + where + """

                order by o.dwbm
                limit :limit offset :offset
                """,
                parameters,
                (rs, rowNum) -> new OrganizationSummary(
                        rs.getInt("id"),
                        SqlText.trim(rs.getString("dwbm")),
                        SqlText.trim(rs.getString("dwmc")),
                        SqlText.trim(rs.getString("dwmc1")),
                        SqlText.trim(rs.getString("dwsx")),
                        SqlText.trim(rs.getString("dwbz")),
                        rs.getInt("bzrs"),
                        rs.getInt("active_personnel_count")));
    }

    long count(String keyword, OrganizationScope organizationScope) {
        if (organizationScope.noneScope()) {
            return 0;
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("allOrganizations", organizationScope.all())
                .addValue("organizationCodes", organizationScope.organizationCodes().isEmpty() ? List.of("__NO_ORG__") : organizationScope.organizationCodes());
        String where = " where (:allOrganizations = TRUE OR dwbm IN (:organizationCodes))";
        if (keyword != null && !keyword.isBlank()) {
            where += " and (dwbm like :keyword or dwmc like :keyword or dwmc1 like :keyword)";
            parameters.addValue("keyword", "%" + keyword.trim() + "%");
        }

        Long count = jdbc.queryForObject("select count(*) from dwbm" + where, parameters, Long.class);
        return count == null ? 0 : count;
    }
}
