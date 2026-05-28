package database.assignment.domain.coupon.controller;

import database.assignment.domain.coupon.dto.Coupon;
import database.assignment.domain.coupon.dto.MyCouponResponse;
import database.assignment.domain.coupon.dto.UserCoupon;
import database.assignment.domain.coupon.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/my")
    public ResponseEntity<List<MyCouponResponse>> getMyCouponList(@RequestBody String license){
        return ResponseEntity.ok(couponService.getMyCouponList(license));
    }
}
