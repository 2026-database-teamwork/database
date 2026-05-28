package database.assignment.domain.company.service;

import database.assignment.domain.car.dto.Car;
import database.assignment.domain.car.repository.CarRepository;
import database.assignment.domain.company.dto.Company;
import database.assignment.domain.company.dto.CompanyRegisterDto;
import database.assignment.domain.company.dto.Company;
import database.assignment.domain.company.repository.CompanyRepository;
import database.assignment.global.error.BusinessException;
import database.assignment.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CarRepository carRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository, CarRepository carRepository) {
        this.companyRepository = companyRepository;
        this.carRepository = carRepository;
    }

    public List<Company> findCompaniesByRegion(String region) {
        // 1. 입력값 region이 null이거나 공백일 경우를 안전하게 처리
        String searchKeyword = Optional.ofNullable(region)
                .map(String::trim)
                .filter(r -> !r.isBlank())
                .orElse("");

        if (searchKeyword.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_VARIABLE);
        }

        // 2. Stream을 사용하여 필터링 진행
        return companyRepository.findAllCompany().stream()
                .filter(company -> isAddressMatch(company, searchKeyword))
                .collect(Collectors.toList());
    }

    public Optional<Company> findCompanyById(Long companyId){
        return companyRepository.findCompanyById(companyId);
    }

    // 주소 비교 로직을 별도 메서드로 분리 (Optional 활용)
    private boolean isAddressMatch(Company company, String keyword) {
        return Optional.ofNullable(company.getCompanyAddress())
                .map(address -> address.contains(keyword))
                .orElse(false); // 주소가 null이면 false 반환
    }

    private boolean checkDuplicateCompanybyName(String name){
        return companyRepository.findCompanyByName(name).isPresent();
    }

    public void addCompany(CompanyRegisterDto companyRegisterDto){
        if(checkDuplicateCompanybyName(companyRegisterDto.getCompanyName())) {
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        companyRepository.createCompany(companyRegisterDto);
    }

    public List<Car> getCarsByCompany(Long companyId){
        companyRepository.findCompanyById(companyId)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_FOUND));
        return carRepository.findCarsByCompanyId(companyId);
    }

}
