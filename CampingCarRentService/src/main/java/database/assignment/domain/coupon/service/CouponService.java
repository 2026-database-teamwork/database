package database.assignment.domain.coupon.service;

import database.assignment.domain.coupon.dto.Coupon;
import database.assignment.domain.coupon.dto.MyCouponResponse;
import database.assignment.domain.coupon.dto.UserCoupon;
import database.assignment.domain.coupon.repository.CouponRepository;
import database.assignment.domain.rent.event.RentCompletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {
    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public List<MyCouponResponse> getMyCouponList(String license){
        return couponRepository.findCouponByLicense(license).stream()
                .filter(r->r.getStatus().equals("unused"))
                .collect(Collectors.toList());
    }


    @EventListener
    @Transactional
    public void handleRentCompleted(RentCompletedEvent event){
        couponRepository.updateStatusToUsed(event.userCouponId());
    }
}
