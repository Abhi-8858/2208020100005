package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Click;

public interface ClickRepository extends JpaRepository<Click, Long> {
}
