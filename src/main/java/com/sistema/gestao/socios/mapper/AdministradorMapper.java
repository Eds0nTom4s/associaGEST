package com.sistema.gestao.socios.mapper;

import com.sistema.gestao.socios.dto.AdministradorRequestDTO;
import com.sistema.gestao.socios.dto.AdministradorResponseDTO;
import com.sistema.gestao.socios.model.Administrador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdministradorMapper {

    AdministradorMapper INSTANCE = Mappers.getMapper(AdministradorMapper.class);

    // Maps Request DTO to Entity for creation
    @Mapping(target = "id", ignore = true) // Ignore ID on creation
    Administrador toAdministrador(AdministradorRequestDTO dto);

    // Maps Entity to Response DTO
    AdministradorResponseDTO toAdministradorResponseDTO(Administrador administrador);

    // Maps List of Entities to List of Response DTOs
    List<AdministradorResponseDTO> toAdministradorResponseDTOList(List<Administrador> administradores);

    // Updates an existing Administrador entity from a Request DTO
    // Ignore ID and password (handle password update separately in service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    void updateAdministradorFromDto(AdministradorRequestDTO dto, @MappingTarget Administrador administrador);
}
