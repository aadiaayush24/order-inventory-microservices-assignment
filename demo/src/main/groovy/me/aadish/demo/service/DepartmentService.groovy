package me.aadish.demo.service

import groovy.util.logging.Slf4j
import me.aadish.demo.domain.Department
import me.aadish.demo.repository.DepartmentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

import javax.persistence.EntityManager

@Service
@Slf4j
class DepartmentService {
    @Autowired
    DepartmentRepository departmentRepository
    void createDepartment(Department department)    {
        departmentRepository.save(department)
        return
    }
    Department findDepartment(Long departmentId)    {
        Department department=departmentRepository.findByDepartmentId(departmentId)
        return department
    }
}
