package com.sistema.gestao.socios.mapper;

import com.sistema.gestao.socios.dto.SocioRequestDTO;
import com.sistema.gestao.socios.dto.SocioResponseDTO;
import com.sistema.gestao.socios.model.Categoria; // Import Categoria
import com.sistema.gestao.socios.model.Socio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

// Use CategoriaMapper to map the nested Categoria object
@Mapper(componentModel = "spring", uses = {CategoriaMapper.class})
public interface SocioMapper {

    SocioMapper INSTANCE = Mappers.getMapper(SocioMapper.class);

    // Maps Request DTO to Entity for creation
    // We need to handle the categoriaId separately (likely in the service layer)
    // MapStruct can't directly map categoriaId to a Categoria object without fetching it
    @Mapping(target = "id", ignore = true) // Ignore ID on creation
    @Mapping(target = "statusPagamento", ignore = true) // Status is managed internally
    @Mapping(target = "categoria", ignore = true) // Handle association in service
    @Mapping(target = "pagamentos", ignore = true) // Ignore collections
    @Mapping(target = "notificacoes", ignore = true) // Ignore collections
    Socio toSocio(SocioRequestDTO dto);

    // Maps Entity to Response DTO
    // Categoria will be mapped automatically by CategoriaMapper
    SocioResponseDTO toSocioResponseDTO(Socio socio);

    // Maps List of Entities to List of Response DTOs
    List<SocioResponseDTO> toSocioResponseDTOList(List<Socio> socios);

    // Updates an existing Socio entity from a Request DTO
    // Ignore ID, password, email, documento, categoria - handle these updates carefully in the service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "documento", ignore = true)
    @Mapping(target = "statusPagamento", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "pagamentos", ignore = true)
    @Mapping(target = "notificacoes", ignore = true)
    void updateSocioFromDto(SocioRequestDTO dto, @MappingTarget Socio socio);

    // Helper method if needed to map Categoria ID to Categoria object (requires fetching)
    // default Categoria map(Long value) {
    //     if (value == null) {
    //         return null;
    //     }
    //     // This requires injecting CategoriaRepository/Service or fetching logic here
    //     // It's generally better to handle this association logic in the service layer
    //     Categoria categoria = new Categoria();
    //     categoria.setId(value);
    //     return categoria;
    // }
}
