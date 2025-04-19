# associaGEST - Sistema de Gestão de Sócios

Este projeto é uma aplicação Spring Boot para gerenciamento de sócios de uma associação.

## Funcionalidades Implementadas

1.  **Configuração Inicial do Projeto (Etapa 1)**
    *   Criação do projeto Spring Boot com Maven.
    *   Adição das dependências essenciais: Spring Web, Spring Data JPA, H2 Database, Lombok, MapStruct, Springdoc OpenAPI, JUnit/Mockito.
    *   Configuração do `application.properties` para:
        *   Banco de dados H2 em memória (`jdbc:h2:mem:testdb`).
        *   Habilitação do Console H2 (`/h2-console`).
        *   Configurações JPA (`show-sql`, `format_sql`, `ddl-auto=update`).
        *   Configuração básica do Swagger/Springdoc.
    *   Criação da estrutura inicial de pacotes (`controller`, `service`, `repository`, `model`, `dto`, `mapper`).
    *   Criação da classe principal da aplicação (`AssociaGestApplication`).

2.  **Criação de Entidades e Repositórios (Etapa 2)**
    *   Definição das entidades JPA no pacote `model`:
        *   `Categoria`
        *   `Socio`
        *   `Pagamento`
        *   `Administrador`
        *   `RelatorioFinanceiro`
        *   `Notificacao`
    *   Mapeamento dos relacionamentos entre entidades (`@ManyToOne`, `@OneToMany`).
    *   Criação das interfaces de repositório Spring Data JPA correspondentes no pacote `repository`, estendendo `JpaRepository`.

3.  **Implementação da Camada de Serviço (Etapa 3)**
    *   Criação das classes de serviço no pacote `service` para cada entidade principal.
    *   Implementação de métodos CRUD básicos (`cadastrar`, `listarTodos`, `buscarPorId`, `atualizar`, `deletar`) com anotações `@Transactional`.
    *   Injeção dos repositórios correspondentes usando `@Autowired`.
    *   Adição de validações básicas (ex: verificação de email/documento duplicado no `SocioService`).
    *   Inclusão de comentários `// TODO:` para indicar pontos de melhoria (validações completas, hashing de senha, lógica de negócio específica).

4.  **Criação de DTOs, Mappers e Controladores REST (Etapa 4)**
    *   Criação de Data Transfer Objects (DTOs) (`*RequestDTO`, `*ResponseDTO`) no pacote `dto` para desacoplar a API da camada de modelo.
    *   Criação de interfaces Mapper com MapStruct no pacote `mapper` para conversão entre Entidades e DTOs.
    *   Criação dos controladores REST no pacote `controller` para cada entidade principal.
    *   Definição de endpoints RESTful (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`) seguindo padrões comuns.
    *   Injeção dos serviços e mappers nos controladores.
    *   Uso de DTOs nas assinaturas dos métodos dos controladores (`@RequestBody`, `ResponseEntity`).
    *   Adição de validação de entrada nos DTOs de requisição usando anotações de validação (`@Valid`, `@NotBlank`, `@NotNull`, etc.).
    *   Adição de documentação Swagger/OpenAPI básica (`@Tag`, `@Operation`, `@ApiResponse`).

5.  **Tratamento Global de Exceções (Etapa 5)**
    *   Criação de exceções personalizadas (`RecursoNaoEncontradoException`, `RegraNegocioException`) no pacote `exception`.
    *   Criação de DTOs para respostas de erro padronizadas (`ErrorResponse`, `ValidationError`).
    *   Implementação de um `GlobalExceptionHandler` (`@ControllerAdvice`) para interceptar exceções:
        *   Exceções personalizadas (retornando 404 ou 400).
        *   Erros de validação (`MethodArgumentNotValidException`, retornando 400 com detalhes dos campos).
        *   `ResponseStatusException` (usada anteriormente nos controllers).
        *   Exceções genéricas (retornando 500).
    *   Refatoração das camadas de Serviço e Controlador para lançar as exceções personalizadas apropriadas e remover `try-catch` redundantes.

6.  **Implementação de Testes Automatizados (Etapa 6)**
    *   **Testes Unitários:**
        *   Criação de classes de teste no diretório `src/test/java` para cada classe de Serviço (`CategoriaServiceTest`, `SocioServiceTest`, etc.).
        *   Uso de JUnit 5 e Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`).
        *   Teste dos métodos de serviço, mockando as dependências (repositórios, outros serviços, mappers).
        *   Verificação do comportamento esperado e do lançamento das exceções personalizadas.
    *   **Testes de Integração:**
        *   Criação de classes de teste no diretório `src/test/java` para cada classe de Controlador (`CategoriaControllerIntegrationTest`, `SocioControllerIntegrationTest`, etc.).
    *   Uso de `@SpringBootTest`, `@AutoConfigureMockMvc`. A anotação `@DirtiesContext` foi substituída por limpeza explícita do banco de dados (`@BeforeEach` com `deleteAll()`) para garantir um estado consistente antes de cada teste de integração.
    *   Utilização de `MockMvc` para simular requisições HTTP aos endpoints da API.
    *   Verificação dos status de resposta HTTP, conteúdo JSON das respostas e tratamento de erros (404, 400).
    *   Interação com o banco de dados H2 em memória configurado para os testes.
    *   **Correções Realizadas**:
        *   Adicionada limpeza explícita do banco de dados em todos os testes de integração.
        *   Adicionada anotação `@JsonFormat(pattern = "yyyy-MM-dd")` aos campos de data nos DTOs de resposta (`PagamentoResponseDTO`, `NotificacaoResponseDTO`, `RelatorioFinanceiroResponseDTO`) e requisição (`RelatorioFinanceiroRequestDTO`) para padronizar o formato.
        *   Verificado o `GlobalExceptionHandler` para garantir o tratamento correto de `MethodArgumentNotValidException`.
        *   Revisadas as expectativas nos testes de integração para alinhar com o comportamento da API e as correções de formato de data.
    *   **Arquivos Afetados (Exemplos de Correção)**:
        *   `src/test/java/com/sistema/gestao/socios/controller/*IntegrationTest.java` (Limpeza de BD)
        *   `src/main/java/com/sistema/gestao/socios/dto/*ResponseDTO.java` (Formato de Data)
        *   `src/main/java/com/sistema/gestao/socios/dto/RelatorioFinanceiroRequestDTO.java` (Formato de Data)
    *   **Próximos Passos**: Configurar CI/CD para executar os testes automaticamente.

## Como Executar

1.  Clone o repositório.
2.  Certifique-se de ter o Java (versão 17 ou superior) e o Maven instalados.
3.  Execute a aplicação usando o Maven:
    ```bash
    mvn spring-boot:run
    ```
4.  A aplicação estará disponível em `http://localhost:8080`.
5.  Acesse a documentação da API (Swagger UI) em `http://localhost:8080/swagger-ui.html`.
6.  Acesse o console do banco de dados H2 em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, User: `sa`, Password: [vazio]).

## Como Executar os Testes

Execute os testes usando o Maven:
```bash
mvn test
