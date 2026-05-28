package database.assignment.domain.company.controller;

import database.assignment.domain.car.dto.Car;
import database.assignment.domain.company.dto.CompanyRegisterDto;
import database.assignment.domain.company.dto.Company;
import database.assignment.domain.company.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
public class CompanyController {
    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/{region}")
    public ResponseEntity<List<Company>> getCompaniesByRegion(@PathVariable String region){
        return ResponseEntity.ok(companyService.findCompaniesByRegion(region));
    }

    @GetMapping("/{companyId}/cars")
    public ResponseEntity<List<Car>> getCompanyCars(@PathVariable Long companyId){
        return ResponseEntity.ok(companyService.getCarsByCompany(companyId));
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerCompany(@RequestBody CompanyRegisterDto companyRegisterDto){
        companyService.addCompany(companyRegisterDto);
        return ResponseEntity.ok("등록 성공");
    }



}
