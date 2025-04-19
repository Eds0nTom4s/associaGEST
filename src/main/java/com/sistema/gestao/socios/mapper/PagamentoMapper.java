package com.sistema.gestao.socios.mapper;

import com.sistema.gestao.socios.dto.PagamentoRequestDTO;
import com.sistema.gestao.socios.dto.PagamentoResponseDTO;
import com.sistema.gestao.socios.model.Categoria; // Import Categoria
import com.sistema.gestao.socios.model.Pagamento;
import com.sistema.gestao.socios.model.Socio; // Import Socio
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PagamentoMapper {

    PagamentoMapper INSTANCE = Mappers.getMapper(PagamentoMapper.class);

    // Maps Request DTO to Entity for creation
    // Handle socioId and categoriaId mapping in the service layer
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataPagamento", ignore = true) // Often set by service
    @Mapping(target = "socio", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    Pagamento toPagamento(PagamentoRequestDTO dto);

    // Maps Entity to Response DTO
    // Map nested Socio and Categoria objects to their IDs
    @Mapping(source = "socio.id", target = "socioId")
    @Mapping(source = "categoria.id", target = "categoriaId")
    PagamentoResponseDTO toPagamentoResponseDTO(Pagamento pagamento);

    // Maps List of Entities to List of Response DTOs
    List<PagamentoResponseDTO> toPagamentoResponseDTOList(List<Pagamento> pagamentos);

    // Update might not be standard for Pagamento, but if needed:
    // @Mapping(target = "id", ignore = true)
    // @Mapping(target = "dataPagamento", ignore = true)
    // @Mapping(target = "socio", ignore = true)
    // @Mapping(target = "categoria", ignore = true)
    // void updatePagamentoFromDto(PagamentoRequestDTO dto, @MappingTarget Pagamento pagamento);

     // Helper methods if needed to map IDs to Entities (requires fetching logic)
    // default Socio mapSocioIdToSocio(Long id) { ... }
    // default Categoria mapCategoriaIdToCategoria(Long id) { ... }
}
