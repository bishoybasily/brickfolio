package com.gmail.bishoybasily.brickfolio.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.gmail.bishoybasily.brickfolio.domain.entity.Developer;

public interface DevelopersRepository extends JpaRepository<Developer, String>, JpaSpecificationExecutor<Developer> {

}
