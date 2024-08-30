package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.EndpointHit;
import ru.practicum.stat.ViewStatsDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT new ru.practicum.stat.ViewStatsDTO(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp between :start AND :end " +
            "GROUP BY e.app, e.uri " +
            "ORDER By COUNT(DISTINCT e.ip) DESC")
    List<ViewStatsDTO> findAllByUniqueIp(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.stat.ViewStatsDTO(e.app, e.uri, COUNT(e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp between :start AND :end " +
            "GROUP BY e.app, e.uri " +
            "ORDER By COUNT(e.ip) DESC")
    List<ViewStatsDTO> findAllByNonUniqueIp(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.stat.ViewStatsDTO(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp between :start AND :end AND e.uri IN :uris " +
            "GROUP BY e.app, e.uri " +
            "ORDER By COUNT(DISTINCT e.ip) DESC")
    List<ViewStatsDTO> findByUniqueIp(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                      @Param("uris") List<String> uris);


    @Query("SELECT new ru.practicum.stat.ViewStatsDTO(e.app, e.uri, COUNT(e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp between :start AND :end AND e.uri IN :uris " +
            "GROUP BY e.app, e.uri " +
            "ORDER By COUNT(e.ip) DESC")
    List<ViewStatsDTO> findByNonUniqueIp(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                         @Param("uris") List<String> uris);
}
