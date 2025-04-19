package com.sistema.gestao.socios.mapper;

import com.sistema.gestao.socios.dto.CategoriaRequestDTO;
import com.sistema.gestao.socios.dto.CategoriaResponseDTO;
import com.sistema.gestao.socios.model.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // Import Mapping annotation
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring") // Integrate with Spring DI
public interface CategoriaMapper {

    CategoriaMapper INSTANCE = Mappers.getMapper(CategoriaMapper.class);

    // Maps Request DTO to Entity for creation
    @Mapping(target = "id", ignore = true) // Ignore ID during creation from DTO
    @Mapping(target = "socios", ignore = true) // Ignore list mapping from DTO
    @Mapping(target = "pagamentos", ignore = true) // Ignore list mapping from DTO
    Categoria toCategoria(CategoriaRequestDTO dto);

    // Maps Entity to Response DTO
    CategoriaResponseDTO toCategoriaResponseDTO(Categoria categoria);

    // Maps List of Entities to List of Response DTOs
    List<CategoriaResponseDTO> toCategoriaResponseDTOList(List<Categoria> categorias);

    // Updates an existing Categoria entity from a Request DTO
    @Mapping(target = "id", ignore = true) // Ignore ID from DTO during update (it comes from @MappingTarget)
    @Mapping(target = "socios", ignore = true) // Ignore list mapping from DTO
    @Mapping(target = "pagamentos", ignore = true) // Ignore list mapping from DTO
    void updateCategoriaFromDto(CategoriaRequestDTO dto, @MappingTarget Categoria categoria);
}
