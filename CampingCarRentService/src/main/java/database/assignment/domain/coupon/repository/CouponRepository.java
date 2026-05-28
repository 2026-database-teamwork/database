package database.assignment.domain.coupon.repository;

import database.assignment.domain.coupon.dto.Coupon;
import database.assignment.domain.coupon.dto.MyCouponResponse;
import database.assignment.domain.coupon.dto.UserCoupon;

import java.util.List;

public interface CouponRepository {
    List<MyCouponResponse> findCouponByLicense(String license);
    int updateStatusToUsed(Long userCouponId);
}
