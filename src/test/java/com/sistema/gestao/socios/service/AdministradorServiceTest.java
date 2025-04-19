package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.dto.AdministradorRequestDTO;
import com.sistema.gestao.socios.exception.RecursoNaoEncontradoException;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.mapper.AdministradorMapper;
import com.sistema.gestao.socios.model.Administrador;
import com.sistema.gestao.socios.repository.AdministradorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministradorServiceTest {

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private AdministradorMapper administradorMapper;

    // TODO: Mock PasswordEncoder if implementing hashing

    @InjectMocks
    private AdministradorService administradorService;

    private Administrador administrador;
    private AdministradorRequestDTO adminRequestDTO;

    @BeforeEach
    void setUp() {
        administrador = new Administrador(1L, "Admin User", "admin@example.com", "senhaforte");
        adminRequestDTO = new AdministradorRequestDTO();
        adminRequestDTO.setNome("Admin User");
        adminRequestDTO.setEmail("admin@example.com");
        adminRequestDTO.setSenha("senhaforte");
    }

    @Test
    void testCadastrar_Success() {
        when(administradorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(administradorMapper.toAdministrador(any(AdministradorRequestDTO.class))).thenReturn(administrador);
        when(administradorRepository.save(any(Administrador.class))).thenReturn(administrador);
        // TODO: Mock passwordEncoder.encode if hashing

        Administrador result = administradorService.cadastrar(adminRequestDTO);

        assertNotNull(result);
        assertEquals("Admin User", result.getNome());
        verify(administradorRepository, times(1)).findByEmail("admin@example.com");
        verify(administradorMapper, times(1)).toAdministrador(adminRequestDTO);
        verify(administradorRepository, times(1)).save(administrador);
        // TODO: Assert password hashing if implemented
    }

    @Test
    void testCadastrar_Fail_EmailExists() {
        when(administradorRepository.findByEmail(anyString())).thenReturn(Optional.of(administrador));

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            administradorService.cadastrar(adminRequestDTO);
        });

        assertEquals("Email de administrador já cadastrado: admin@example.com", exception.getMessage());
        verify(administradorRepository, times(1)).findByEmail("admin@example.com");
        verify(administradorRepository, never()).save(any(Administrador.class));
    }

    @Test
    void testListarTodos() {
        when(administradorRepository.findAll()).thenReturn(List.of(administrador));
        List<Administrador> result = administradorService.listarTodos();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(administradorRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPorId_Success() {
        when(administradorRepository.findById(anyLong())).thenReturn(Optional.of(administrador));
        Administrador result = administradorService.buscarPorId(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(administradorRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorId_Fail_NotFound() {
        when(administradorRepository.findById(anyLong())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            administradorService.buscarPorId(1L);
        });
        assertEquals("Administrador não encontrado com id: 1", exception.getMessage());
        verify(administradorRepository, times(1)).findById(1L);
    }

     @Test
    void testBuscarPorEmail_Success() {
        when(administradorRepository.findByEmail(anyString())).thenReturn(Optional.of(administrador));
        Administrador result = administradorService.buscarPorEmail("admin@example.com");
        assertNotNull(result);
        assertEquals("admin@example.com", result.getEmail());
        verify(administradorRepository, times(1)).findByEmail("admin@example.com");
    }

     @Test
    void testBuscarPorEmail_Fail_NotFound() {
        when(administradorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            administradorService.buscarPorEmail("notfound@example.com");
        });
        assertEquals("Administrador não encontrado com email: notfound@example.com", exception.getMessage());
        verify(administradorRepository, times(1)).findByEmail("notfound@example.com");
    }

    @Test
    void testAtualizar_Success() {
        AdministradorRequestDTO updateDto = new AdministradorRequestDTO();
        updateDto.setNome("Admin User Updated");
        updateDto.setEmail("admin@example.com"); // Email not changed
        // Password update handled separately

        when(administradorRepository.findById(anyLong())).thenReturn(Optional.of(administrador));
        // No need to mock findByEmail as it's not changing

        // Configure the mock mapper to actually update the object
        doAnswer(invocation -> {
            AdministradorRequestDTO dtoArg = invocation.getArgument(0);
            Administrador adminArg = invocation.getArgument(1);
            adminArg.setNome(dtoArg.getNome()); // Simulate the name update
            // Simulate other field updates if the mapper did more
            return null; // Void method returns null
        }).when(administradorMapper).updateAdministradorFromDto(any(AdministradorRequestDTO.class), any(Administrador.class));

        // Mock save to return the (now modified) object passed to it
        when(administradorRepository.save(any(Administrador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Administrador result = administradorService.atualizar(1L, updateDto);

        assertNotNull(result);
        assertEquals("Admin User Updated", result.getNome()); // Assertion should now pass
        verify(administradorRepository, times(1)).findById(1L);
        verify(administradorMapper, times(1)).updateAdministradorFromDto(updateDto, administrador); // Verify the mapper was called
        verify(administradorRepository, times(1)).save(administrador); // Verify save was called with the modified object
    }

     @Test
    void testAtualizar_Success_ChangeEmail() {
        AdministradorRequestDTO updateDto = new AdministradorRequestDTO();
        updateDto.setNome("Admin User");
        updateDto.setEmail("admin.novo@example.com"); // Changed email

        when(administradorRepository.findById(anyLong())).thenReturn(Optional.of(administrador));
        when(administradorRepository.findByEmail(eq("admin.novo@example.com"))).thenReturn(Optional.empty()); // New email is unique
        when(administradorRepository.save(any(Administrador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Administrador result = administradorService.atualizar(1L, updateDto);

        assertNotNull(result);
        assertEquals("admin.novo@example.com", result.getEmail());
        verify(administradorRepository, times(1)).findById(1L);
        verify(administradorRepository, times(1)).findByEmail("admin.novo@example.com");
        verify(administradorMapper, times(1)).updateAdministradorFromDto(updateDto, administrador);
        verify(administradorRepository, times(1)).save(administrador);
    }


    @Test
    void testAtualizar_Fail_NotFound() {
        when(administradorRepository.findById(anyLong())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            administradorService.atualizar(1L, adminRequestDTO);
        });
        assertEquals("Administrador não encontrado com id: 1", exception.getMessage());
        verify(administradorRepository, times(1)).findById(1L);
        verify(administradorRepository, never()).save(any(Administrador.class));
    }

     @Test
    void testAtualizar_Fail_EmailExists() {
        AdministradorRequestDTO updateDto = new AdministradorRequestDTO();
        updateDto.setEmail("outro@example.com"); // Trying to change to an existing email

        Administrador existingAdminWithEmail = new Administrador(2L, "Outro Admin", "outro@example.com", "pwd");

        when(administradorRepository.findById(anyLong())).thenReturn(Optional.of(administrador));
        when(administradorRepository.findByEmail(eq("outro@example.com"))).thenReturn(Optional.of(existingAdminWithEmail));

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            administradorService.atualizar(1L, updateDto);
        });

        assertEquals("Email já cadastrado para outro administrador: outro@example.com", exception.getMessage());
        verify(administradorRepository, times(1)).findById(1L);
        verify(administradorRepository, times(1)).findByEmail("outro@example.com");
        verify(administradorRepository, never()).save(any(Administrador.class));
    }

    @Test
    void testDeletar_Success() {
        when(administradorRepository.findById(anyLong())).thenReturn(Optional.of(administrador));
        // Assume count > 1 or validation is not implemented yet
        // when(administradorRepository.count()).thenReturn(2L);
        assertDoesNotThrow(() -> administradorService.deletar(1L));
        verify(administradorRepository, times(1)).findById(1L);
        verify(administradorRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletar_Fail_NotFound() {
        when(administradorRepository.findById(anyLong())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            administradorService.deletar(1L);
        });
        assertEquals("Administrador não encontrado com id: 1", exception.getMessage());
        verify(administradorRepository, times(1)).findById(1L);
        verify(administradorRepository, never()).deleteById(anyLong());
    }

    // TODO: Add test for deletar_Fail_LastAdmin if validation is implemented
}
