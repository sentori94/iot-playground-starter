package com.sentori.iot.repository;

import com.sentori.iot.model.run.RunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface RunRepository extends JpaRepository<RunEntity, UUID> {
    @Query("select case when count(r)>0 then true else false end from RunEntity r where r.status='RUNNING'")
    boolean existsRunning();

    List<RunEntity> findTop10ByOrderByStartedAtDesc();

    @Query("select r from RunEntity r where r.status='RUNNING'")
    Optional<RunEntity> findRunning();
}
