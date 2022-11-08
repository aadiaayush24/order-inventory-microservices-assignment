package me.aadish.demo.domain

import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
@NoArgsConstructor
@AllArgsConstructor
class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long departmentId;
    @Column
    private String departmentName;
    @Column
    private String departmentAddress;
    @Column
    private String departmentCode;
}
