package com.sistema.gestao.socios.mapper;

import com.sistema.gestao.socios.dto.NotificacaoRequestDTO;
import com.sistema.gestao.socios.dto.NotificacaoResponseDTO;
import com.sistema.gestao.socios.model.Notificacao;
import com.sistema.gestao.socios.model.Socio; // Import Socio
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificacaoMapper {

    NotificacaoMapper INSTANCE = Mappers.getMapper(NotificacaoMapper.class);

    // Maps Request DTO to Entity for creation
    // Handle socioId mapping in the service layer
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataEnvio", ignore = true) // Often set by service
    @Mapping(target = "socio", ignore = true)
    Notificacao toNotificacao(NotificacaoRequestDTO dto);

    // Maps Entity to Response DTO
    @Mapping(source = "socio.id", target = "socioId")
    NotificacaoResponseDTO toNotificacaoResponseDTO(Notificacao notificacao);

    // Maps List of Entities to List of Response DTOs
    List<NotificacaoResponseDTO> toNotificacaoResponseDTOList(List<Notificacao> notificacoes);

    // Update is likely not applicable for Notificacao
}
