package database.assignment.domain.coupon.repository;

import database.assignment.domain.coupon.dto.Coupon;
import database.assignment.domain.coupon.dto.MyCouponResponse;
import database.assignment.domain.coupon.dto.UserCoupon;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CouponJdbcRepository implements CouponRepository{
    private final JdbcTemplate jdbcTemplate;

    public CouponJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MyCouponResponse> findCouponByLicense(String license) {
        String sql = """
            SELECT 
                uc.user_coupon_id, 
                c.name, 
                c.discount_type, 
                c.discount_value, 
                c.min_order_amount,
                c.max_discount_amount,
                uc.status, 
                uc.issued_at,
                c.end_date
            FROM usercoupons uc
            INNER JOIN coupons c ON uc.coupon_id = c.coupon_id
            WHERE uc.license = ?
        """;
        return jdbcTemplate.query(sql, CouponResponseRowMapper(), license);
    }

    @Override
    public int updateStatusToUsed(Long userCouponId) {
        String sql = "UPDATE usercoupons set status = 'used', used_at = NOW() WHERE user_coupon_id = ?";
        return jdbcTemplate.update(sql, userCouponId);
    }

    private RowMapper<MyCouponResponse> CouponResponseRowMapper(){
        return (rs, rowNum)->{
            return new MyCouponResponse(
                    rs.getLong("user_coupon_id"),
                    rs.getString("name"),
                    rs.getString("discount_type"),
                    rs.getInt("discount_value"),
                    rs.getInt("min_order_amount"),
                    rs.getInt("max_discount_amount"),
                    rs.getString("status"),
                    rs.getObject("issued_at", LocalDateTime.class),
                    rs.getObject("end_date", LocalDateTime.class)
            );
        };
    }
}
