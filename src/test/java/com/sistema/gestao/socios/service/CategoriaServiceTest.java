package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.dto.CategoriaRequestDTO;
import com.sistema.gestao.socios.exception.RecursoNaoEncontradoException;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.mapper.CategoriaMapper;
import com.sistema.gestao.socios.model.Categoria;
import com.sistema.gestao.socios.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;
    private CategoriaRequestDTO categoriaRequestDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Premium", "Todos os benefícios", new BigDecimal("100.00"), Collections.emptyList(), Collections.emptyList());
        categoriaRequestDTO = new CategoriaRequestDTO();
        categoriaRequestDTO.setNome("Premium");
        categoriaRequestDTO.setBeneficios("Todos os benefícios");
        categoriaRequestDTO.setValorMensalidade(new BigDecimal("100.00"));
    }

    @Test
    void testCadastrar_Success() {
        when(categoriaRepository.findByNomeIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        Categoria result = categoriaService.cadastrar(categoria); // Assuming service takes entity for now

        assertNotNull(result);
        assertEquals("Premium", result.getNome());
        verify(categoriaRepository, times(1)).findByNomeIgnoreCase("Premium");
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    void testCadastrar_Fail_NomeExists() {
        when(categoriaRepository.findByNomeIgnoreCase(anyString())).thenReturn(Optional.of(categoria));

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            categoriaService.cadastrar(categoria);
        });

        assertEquals("Nome da categoria já existe: Premium", exception.getMessage());
        verify(categoriaRepository, times(1)).findByNomeIgnoreCase("Premium");
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void testListarTodos() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        List<Categoria> result = categoriaService.listarTodos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Premium", result.get(0).getNome());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPorId_Success() {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(categoria));

        Categoria result = categoriaService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(categoriaRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorId_Fail_NotFound() {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            categoriaService.buscarPorId(1L);
        });

        assertEquals("Categoria não encontrada com id: 1", exception.getMessage());
        verify(categoriaRepository, times(1)).findById(1L);
    }

    @Test
    void testAtualizar_Success() {
        CategoriaRequestDTO updateDto = new CategoriaRequestDTO();
        updateDto.setNome("Premium Plus");
        updateDto.setBeneficios("Mais benefícios");
        updateDto.setValorMensalidade(new BigDecimal("120.00"));

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(categoria));
        when(categoriaRepository.findByNomeIgnoreCase(eq("Premium Plus"))).thenReturn(Optional.empty()); // New name doesn't exist

        // Configure the mock mapper to actually update the object
        doAnswer(invocation -> {
            CategoriaRequestDTO dtoArg = invocation.getArgument(0);
            Categoria catArg = invocation.getArgument(1);
            catArg.setNome(dtoArg.getNome()); // Simulate update
            catArg.setBeneficios(dtoArg.getBeneficios());
            catArg.setValorMensalidade(dtoArg.getValorMensalidade());
            return null; // Void method returns null
        }).when(categoriaMapper).updateCategoriaFromDto(any(CategoriaRequestDTO.class), any(Categoria.class));

        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return saved entity (now modified)

        Categoria result = categoriaService.atualizar(1L, updateDto);

        assertNotNull(result);
        assertEquals("Premium Plus", result.getNome()); // Assertion should now pass
        assertEquals("Mais benefícios", result.getBeneficios());
        assertEquals(new BigDecimal("120.00"), result.getValorMensalidade());

        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, times(1)).findByNomeIgnoreCase("Premium Plus");
        verify(categoriaMapper, times(1)).updateCategoriaFromDto(updateDto, categoria); // Verify mapper was called
        verify(categoriaRepository, times(1)).save(categoria); // Verify save was called with modified object
    }

     @Test
    void testAtualizar_Fail_NotFound() {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            categoriaService.atualizar(1L, categoriaRequestDTO);
        });

        assertEquals("Categoria não encontrada com id: 1", exception.getMessage());
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void testAtualizar_Fail_NomeExists() {
        Categoria existingCategoriaWithSameName = new Categoria(2L, "Premium Plus", "Outros", BigDecimal.ONE, null, null);
        CategoriaRequestDTO updateDto = new CategoriaRequestDTO();
        updateDto.setNome("Premium Plus"); // Trying to update to an existing name

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(categoria)); // Found category with ID 1
        when(categoriaRepository.findByNomeIgnoreCase(eq("Premium Plus"))).thenReturn(Optional.of(existingCategoriaWithSameName)); // Found another category with the target name

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            categoriaService.atualizar(1L, updateDto);
        });

        assertEquals("Nome da categoria já existe: Premium Plus", exception.getMessage());
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, times(1)).findByNomeIgnoreCase("Premium Plus");
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }


    @Test
    void testDeletar_Success() {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(categoria)); // Found
        // Assume categoria.getSocios() returns empty list or null

        assertDoesNotThrow(() -> categoriaService.deletar(1L));

        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletar_Fail_NotFound() {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            categoriaService.deletar(1L);
        });

        assertEquals("Categoria não encontrada com id: 1", exception.getMessage());
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, never()).deleteById(anyLong());
    }

     @Test
    void testDeletar_Fail_HasSocios() {
        // Simulate categoria having associated socios
        Categoria categoriaComSocios = new Categoria(1L, "Test", "Bens", BigDecimal.TEN, List.of(new com.sistema.gestao.socios.model.Socio()), null);
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(categoriaComSocios));

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            categoriaService.deletar(1L);
        });

        assertEquals("Não é possível excluir categoria pois existem sócios associados.", exception.getMessage());
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, never()).deleteById(anyLong());
    }
}
