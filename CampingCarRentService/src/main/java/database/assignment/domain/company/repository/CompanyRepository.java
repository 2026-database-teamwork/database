package database.assignment.domain.company.repository;

import database.assignment.domain.company.dto.CompanyRegisterDto;
import database.assignment.domain.company.dto.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository {
    //회사조회
    public List<Company> findAllCompany();

    //회사이름으로 찾기
    public Optional<Company> findCompanyByName(String name);

    public Optional<Company> findCompanyById(Long id);
    //회사등록
    public void createCompany(CompanyRegisterDto companyRegisterDto);

}
