package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.dto.SocioRequestDTO;
import com.sistema.gestao.socios.exception.RecursoNaoEncontradoException;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.mapper.SocioMapper;
import com.sistema.gestao.socios.model.Categoria;
import com.sistema.gestao.socios.model.Socio;
import com.sistema.gestao.socios.repository.SocioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocioServiceTest {

    @Mock
    private SocioRepository socioRepository;

    @Mock
    private CategoriaService categoriaService; // Mock CategoriaService dependency

    @Mock
    private SocioMapper socioMapper;

    // TODO: Mock PasswordEncoder if implementing hashing

    @InjectMocks
    private SocioService socioService;

    private Socio socio;
    private Categoria categoria;
    private SocioRequestDTO socioRequestDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Standard", "Benefícios básicos", new BigDecimal("50.00"), null, null);
        socio = new Socio(1L, "João Silva", "12345678900", "joao@example.com", "999999999", "senha123", "PENDENTE", categoria, null, null);

        socioRequestDTO = new SocioRequestDTO();
        socioRequestDTO.setNome("João Silva");
        socioRequestDTO.setDocumento("12345678900");
        socioRequestDTO.setEmail("joao@example.com");
        socioRequestDTO.setTelefone("999999999");
        socioRequestDTO.setSenha("senha123");
        socioRequestDTO.setCategoriaId(1L);
    }

    @Test
    void testCadastrar_Success() {
        when(categoriaService.buscarPorId(anyLong())).thenReturn(categoria); // Categoria found
        when(socioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(socioRepository.findByDocumento(anyString())).thenReturn(Optional.empty());
        when(socioMapper.toSocio(any(SocioRequestDTO.class))).thenReturn(socio); // Assume mapper returns base entity
        when(socioRepository.save(any(Socio.class))).thenReturn(socio);
        // TODO: Mock passwordEncoder.encode if hashing

        Socio result = socioService.cadastrar(socioRequestDTO);

        assertNotNull(result);
        assertEquals("João Silva", result.getNome());
        assertEquals("PENDENTE", result.getStatusPagamento()); // Check initial status
        assertEquals(categoria, result.getCategoria());
        // TODO: Assert password hashing if implemented
        verify(categoriaService, times(1)).buscarPorId(1L);
        verify(socioRepository, times(1)).findByEmail("joao@example.com");
        verify(socioRepository, times(1)).findByDocumento("12345678900");
        verify(socioMapper, times(1)).toSocio(socioRequestDTO);
        verify(socioRepository, times(1)).save(socio);
    }

    @Test
    void testCadastrar_Fail_CategoriaNotFound() {
        when(categoriaService.buscarPorId(anyLong())).thenThrow(new RecursoNaoEncontradoException("Categoria não encontrada"));

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            socioService.cadastrar(socioRequestDTO);
        });

        assertEquals("Categoria não encontrada", exception.getMessage());
        verify(categoriaService, times(1)).buscarPorId(1L);
        verify(socioRepository, never()).findByEmail(anyString());
        verify(socioRepository, never()).findByDocumento(anyString());
        verify(socioRepository, never()).save(any(Socio.class));
    }

    @Test
    void testCadastrar_Fail_EmailExists() {
        when(categoriaService.buscarPorId(anyLong())).thenReturn(categoria);
        when(socioRepository.findByEmail(anyString())).thenReturn(Optional.of(socio)); // Email exists

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            socioService.cadastrar(socioRequestDTO);
        });

        assertEquals("Email já cadastrado: joao@example.com", exception.getMessage());
        verify(categoriaService, times(1)).buscarPorId(1L);
        verify(socioRepository, times(1)).findByEmail("joao@example.com");
        verify(socioRepository, never()).findByDocumento(anyString());
        verify(socioRepository, never()).save(any(Socio.class));
    }

     @Test
    void testCadastrar_Fail_DocumentoExists() {
        when(categoriaService.buscarPorId(anyLong())).thenReturn(categoria);
        when(socioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(socioRepository.findByDocumento(anyString())).thenReturn(Optional.of(socio)); // Documento exists

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            socioService.cadastrar(socioRequestDTO);
        });

        assertEquals("Documento já cadastrado: 12345678900", exception.getMessage());
        verify(categoriaService, times(1)).buscarPorId(1L);
        verify(socioRepository, times(1)).findByEmail("joao@example.com");
        verify(socioRepository, times(1)).findByDocumento("12345678900");
        verify(socioRepository, never()).save(any(Socio.class));
    }


    @Test
    void testListarTodos() {
        when(socioRepository.findAll()).thenReturn(List.of(socio));
        List<Socio> result = socioService.listarTodos();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(socioRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPorId_Success() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio));
        Socio result = socioService.buscarPorId(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(socioRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorId_Fail_NotFound() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            socioService.buscarPorId(1L);
        });
        assertEquals("Sócio não encontrado com id: 1", exception.getMessage());
        verify(socioRepository, times(1)).findById(1L);
    }

     @Test
    void testBuscarPorEmail_Success() {
        when(socioRepository.findByEmail(anyString())).thenReturn(Optional.of(socio));
        Socio result = socioService.buscarPorEmail("joao@example.com");
        assertNotNull(result);
        assertEquals("joao@example.com", result.getEmail());
        verify(socioRepository, times(1)).findByEmail("joao@example.com");
    }

     @Test
    void testBuscarPorEmail_Fail_NotFound() {
        when(socioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            socioService.buscarPorEmail("notfound@example.com");
        });
        assertEquals("Sócio não encontrado com email: notfound@example.com", exception.getMessage());
        verify(socioRepository, times(1)).findByEmail("notfound@example.com");
    }

    // Similar tests for buscarPorDocumento

    @Test
    void testAtualizar_Success() {
        SocioRequestDTO updateDto = new SocioRequestDTO();
        updateDto.setNome("João Silva Atualizado");
        updateDto.setTelefone("888888888");
        updateDto.setEmail("joao@example.com"); // Email not changed
        updateDto.setDocumento("12345678900"); // Documento not changed
        updateDto.setCategoriaId(1L); // Categoria not changed

        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio));
        // No need to mock findByEmail/findByDocumento as they are not changing

        // Configure the mock mapper to actually update the object
        doAnswer(invocation -> {
            SocioRequestDTO dtoArg = invocation.getArgument(0);
            Socio socioArg = invocation.getArgument(1);
            socioArg.setNome(dtoArg.getNome()); // Simulate update
            socioArg.setTelefone(dtoArg.getTelefone());
            // Simulate other field updates if the mapper did more
            return null; // Void method returns null
        }).when(socioMapper).updateSocioFromDto(any(SocioRequestDTO.class), any(Socio.class));

        when(socioRepository.save(any(Socio.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return saved entity (now modified)

        Socio result = socioService.atualizar(1L, updateDto);

        assertNotNull(result);
        assertEquals("João Silva Atualizado", result.getNome()); // Assertion should now pass
        assertEquals("888888888", result.getTelefone());
        verify(socioRepository, times(1)).findById(1L);
        verify(socioMapper, times(1)).updateSocioFromDto(updateDto, socio); // Verify mapper was called
        verify(socioRepository, times(1)).save(socio); // Verify save was called with modified object
    }

     @Test
    void testAtualizar_Success_ChangeEmailAndCategoria() {
        SocioRequestDTO updateDto = new SocioRequestDTO();
        updateDto.setNome("João Silva");
        updateDto.setEmail("joao.novo@example.com"); // Changed email
        updateDto.setDocumento("12345678900");
        updateDto.setCategoriaId(2L); // Changed categoria

        Categoria novaCategoria = new Categoria(2L, "VIP", "VIP Bens", BigDecimal.TEN, null, null);

        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio));
        when(categoriaService.buscarPorId(2L)).thenReturn(novaCategoria); // Mock finding new categoria
        when(socioRepository.findByEmail(eq("joao.novo@example.com"))).thenReturn(Optional.empty()); // New email is unique
        when(socioRepository.save(any(Socio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Socio result = socioService.atualizar(1L, updateDto);

        assertNotNull(result);
        assertEquals("joao.novo@example.com", result.getEmail());
        assertEquals(novaCategoria, result.getCategoria());
        verify(socioRepository, times(1)).findById(1L);
        verify(categoriaService, times(1)).buscarPorId(2L);
        verify(socioRepository, times(1)).findByEmail("joao.novo@example.com");
        verify(socioMapper, times(1)).updateSocioFromDto(updateDto, socio);
        verify(socioRepository, times(1)).save(socio);
    }


    @Test
    void testAtualizar_Fail_NotFound() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            socioService.atualizar(1L, socioRequestDTO);
        });
        assertEquals("Sócio não encontrado com id: 1", exception.getMessage());
        verify(socioRepository, times(1)).findById(1L);
        verify(socioRepository, never()).save(any(Socio.class));
    }

     @Test
    void testAtualizar_Fail_EmailExists() {
        SocioRequestDTO updateDto = new SocioRequestDTO();
        updateDto.setEmail("outro@example.com"); // Trying to change to an existing email
        updateDto.setCategoriaId(1L);

        Socio existingSocioWithEmail = new Socio(2L, "Outro", "987", "outro@example.com", "111", "pwd", "PAGO", categoria, null, null);

        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio));
        when(socioRepository.findByEmail(eq("outro@example.com"))).thenReturn(Optional.of(existingSocioWithEmail));

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            socioService.atualizar(1L, updateDto);
        });

        assertEquals("Email já cadastrado para outro sócio: outro@example.com", exception.getMessage());
        verify(socioRepository, times(1)).findById(1L);
        verify(socioRepository, times(1)).findByEmail("outro@example.com");
        verify(socioRepository, never()).save(any(Socio.class));
    }

    // Similar test for Documento Exists on update

    @Test
    void testDeletar_Success() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio));
        assertDoesNotThrow(() -> socioService.deletar(1L));
        verify(socioRepository, times(1)).findById(1L);
        verify(socioRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletar_Fail_NotFound() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            socioService.deletar(1L);
        });
        assertEquals("Sócio não encontrado com id: 1", exception.getMessage());
        verify(socioRepository, times(1)).findById(1L);
        verify(socioRepository, never()).deleteById(anyLong());
    }

    // TODO: Add test for deletar_Fail_HasDependencies if validation is added
}
