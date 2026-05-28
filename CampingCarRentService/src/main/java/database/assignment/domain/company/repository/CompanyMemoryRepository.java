package database.assignment.domain.company.repository;

import database.assignment.domain.company.dto.CompanyRegisterDto;
import database.assignment.domain.company.dto.Company;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CompanyMemoryRepository implements CompanyRepository{

    private final Map<Long, Company> companyDB = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    public CompanyMemoryRepository(){
        Company a = new Company(sequence.incrementAndGet(), "유관랜트",
                "대전광역시 서구 월평동", "0103942023948", "박정현","osdfjk@lsdk.com");
        companyDB.put(a.getCompanyId(), a);
    }

    @Override
    public List<Company> findAllCompany() {
        return companyDB.values().stream().collect(Collectors.toList());
    }

    @Override
    public Optional<Company> findCompanyByName(String name){
        return companyDB.values().stream()
                .filter(c->c.getCompanyName().contains(name))
                .findFirst();
    }

    @Override
    public Optional<Company> findCompanyById(Long id){
        return companyDB.values().stream()
                .filter(c->c.getCompanyId()==id)
                .findFirst();
    }

    @Override
    public void createCompany(CompanyRegisterDto request){
        Company company = new Company(
                sequence.incrementAndGet(),
                request.getCompanyName(),
               request.getCompanyAddress(),
                request.getCompanyPhone(),
                request.getRepresentativeName(),
                request.getRepresentativeEmail()
        );
        companyDB.put(company.getCompanyId(), company);
    }
}
