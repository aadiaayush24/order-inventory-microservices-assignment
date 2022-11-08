package me.aadish.demo.web

import groovy.util.logging.Slf4j
import me.aadish.demo.domain.Department
import me.aadish.demo.service.DepartmentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
@Slf4j
class DepartmentController {
    @Autowired
    DepartmentService departmentService

    @PostMapping(value='/departments', produces= MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity createDepartment(@RequestBody  @Valid @NotNull Department department) {
        departmentService.createDepartment(department)
        return new ResponseEntity(department, HttpStatus.CREATED)
    }

    @GetMapping(value='/departments/{id}', produces=MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity getDepartment(@PathVariable("id") Long departmentId) {
        Department department=departmentService.findDepartment(departmentId)
        if(department) {
            return new ResponseEntity(department, HttpStatus.OK)
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND)
    }
}
