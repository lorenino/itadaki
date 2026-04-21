package fr.esgi.hla.itadaki.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/** MapStruct mapper for statistics DTOs assembled by StatsServiceImpl from aggregation queries. */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatsMapper {
}
