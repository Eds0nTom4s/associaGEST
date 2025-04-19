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

7.  **Autenticação JWT e Autorização (Etapa 7)**
    *   **Dependências**: Adicionadas dependências `spring-boot-starter-security` e `jjwt` (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) ao `pom.xml`.
    *   **Modelo**: Criada a entidade `Usuario` (implementando `UserDetails`) e a enumeração `Role` (`ADMIN`, `SOCIO`). Criado `UsuarioRepository`.
    *   **Configuração de Segurança (`SecurityConfig`)**:
        *   Configurado `SecurityFilterChain` para autenticação stateless (`SessionCreationPolicy.STATELESS`).
        *   Definido `PasswordEncoder` (BCrypt).
        *   Configurado `UserDetailsService` para buscar usuários pelo email.
        *   Configurado `AuthenticationProvider` e `AuthenticationManager`.
        *   Definidas regras de autorização:
            *   `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`: Públicos.
            *   `/api/administradores/**`, `/api/categorias/**`, `/api/relatorios-financeiros/**`: Requerem `ROLE_ADMIN`.
            *   `/api/socios/**`, `/api/pagamentos/**`, `/api/notificacoes/**`: Requerem `ROLE_ADMIN` ou `ROLE_SOCIO`.
            *   Outras requisições: Requerem autenticação.
    *   **Serviços JWT**:
        *   Criado `JwtService` para gerar, validar e extrair informações de tokens JWT. A chave secreta e expiração são configuradas em `application.properties`.
        *   Criado `AuthenticationService` com métodos `register` (cria usuário, gera token) e `login` (autentica, gera token).
    *   **Controlador de Autenticação**: Criado `AuthenticationController` com endpoints `/api/auth/register` e `/api/auth/login`.
    *   **Filtro JWT**: Criado `JwtAuthenticationFilter` para interceptar requisições, validar o token JWT no cabeçalho `Authorization: Bearer <token>` e configurar o `SecurityContextHolder`. O filtro é adicionado à cadeia de segurança em `SecurityConfig`.

8.  **Testes Automatizados para Autenticação (Etapa 8)**
    *   **Testes Unitários:**
        *   `JwtServiceTest`: Testa a geração, extração de claims e validação de tokens JWT (incluindo casos de expiração e tokens malformados).
        *   `AuthenticationServiceTest`: Testa os métodos `register` e `login` do serviço de autenticação, mockando dependências (`UsuarioRepository`, `PasswordEncoder`, `JwtService`, `AuthenticationManager`) para isolar a lógica do serviço. Valida cenários de sucesso, email existente e credenciais inválidas.
    *   **Testes de Integração:**
        *   `AuthenticationControllerIntegrationTest`: Utiliza `@SpringBootTest` e `MockMvc` para testar os endpoints `/api/auth/register` e `/api/auth/login`.
        *   Verifica o registro de usuários (ADMIN e SOCIO), tratamento de email duplicado, login com credenciais válidas e inválidas (senha incorreta, email inexistente).
        *   Testa o acesso a rotas protegidas (`/api/socios`, `/api/categorias`) com e sem token, com tokens válidos para diferentes roles (ADMIN, SOCIO) e com tokens inválidos, verificando os status HTTP esperados (200 OK, 401 Unauthorized, 403 Forbidden).
    *   Utiliza `@Transactional` para garantir o rollback do banco de dados após cada teste, mantendo o isolamento.

## Correções Adicionais

*   **Erro de Compilação em Teste de Integração**:
    *   **Problema**: Ocorria um erro de compilação no `SocioControllerIntegrationTest` ao tentar construir um objeto `Usuario` usando `.nome("...")`. A entidade `Usuario` não possui um campo `nome`.
    *   **Solução**: A chamada `.nome(...)` foi removida da construção do `Usuario` no método `setupAuthentication` do `SocioControllerIntegrationTest.java`.

## Padrões e Convenções

*   **Mensagens de Erro:** Mensagens de erro para recursos não encontrados utilizam "id" em minúsculo (ex: "Recurso não encontrado com id: 123").

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

## Testando a Autenticação

1.  **Registro**: Use o endpoint `POST /api/auth/register` (via Swagger UI ou Postman) com um corpo JSON como:
    ```json
    {
      "email": "admin@example.com",
      "senha": "password123",
      "role": "ADMIN"
    }
    ```
    Ou para um sócio:
    ```json
    {
      "email": "socio@example.com",
      "senha": "password123",
      "role": "SOCIO"
    }
    ```
    A resposta conterá um token JWT.
2.  **Login**: Use o endpoint `POST /api/auth/login` com um corpo JSON:
    ```json
    {
      "email": "admin@example.com",
      "senha": "password123"
    }
    ```
    A resposta conterá um novo token JWT.
3.  **Acessando Rotas Protegidas**: Copie o token JWT obtido. Para fazer requisições a endpoints protegidos (ex: `GET /api/socios`), adicione um cabeçalho `Authorization` à sua requisição com o valor `Bearer <seu_token_jwt>`.
    *   Sem um token válido, você receberá um erro 401 (Unauthorized) ou 403 (Forbidden) dependendo da configuração.
    *   Com um token válido, a requisição será processada de acordo com as regras de autorização do papel do usuário associado ao token.

## Perfis de Configuração (Spring Profiles)

Este projeto utiliza perfis do Spring Boot para gerenciar configurações específicas de cada ambiente:

*   **`dev` (Padrão):**
    *   **Propósito:** Ambiente de desenvolvimento local.
    *   **Banco de Dados:** H2 em memória (acessível em `/h2-console`).
    *   **Logging:** Nível `DEBUG` para o pacote do projeto e `TRACE` para parâmetros SQL. SQL formatado é exibido no console.
    *   **Segurança:** HTTPS desabilitado.
    *   **Porta:** `8081`.
    *   **Ativação:** É o perfil padrão se nenhum outro for especificado.

*   **`prod`:**
    *   **Propósito:** Ambiente de produção.
    *   **Banco de Dados:** Configurado para um PostgreSQL externo (ajustar `spring.datasource.url`, `username`, `password` em `application-prod.properties`). Validação do schema (`ddl-auto=validate`).
    *   **Logging:** Nível `WARN` para a raiz e `INFO` para o pacote do projeto.
    *   **Segurança:** HTTPS habilitado (requer configuração de keystore).
    *   **Porta:** `8080`.
    *   **Ativação:**
        *   **Maven:** `mvn spring-boot:run -Dspring.profiles.active=prod`
        *   **Variável de Ambiente:** `SPRING_PROFILES_ACTIVE=prod` (usado no Docker, por exemplo)
        *   **IDE:** Adicionar `-Dspring.profiles.active=prod` aos argumentos da VM na configuração de execução.

*   **`test`:**
    *   **Propósito:** Ambiente para execução de testes automatizados.
    *   **Banco de Dados:** H2 em memória isolado (`create-drop` para limpar entre testes).
    *   **Logging:** Nível `WARN` para minimizar logs durante os testes.
    *   **Segurança:** HTTPS desabilitado.
    *   **Porta:** Aleatória (`server.port=0`) para evitar conflitos.
    *   **Ativação:** Automaticamente ativado pelo Spring Boot Test (ex: via `@ActiveProfiles("test")` ou por padrão se nenhuma outra configuração for feita nos testes).

**Como Ativar um Perfil Específico (Exemplo: `dev`):**

*   **Linha de Comando (Maven):**
    ```bash
    mvn spring-boot:run -Dspring.profiles.active=dev
    ```
*   **Variável de Ambiente:**
    ```bash
    export SPRING_PROFILES_ACTIVE=dev # Linux/macOS
    set SPRING_PROFILES_ACTIVE=dev   # Windows CMD
    $env:SPRING_PROFILES_ACTIVE="dev" # Windows PowerShell
    # Ou configurar no docker-compose.yml / Dockerfile
    ```
*   **IDE (IntelliJ/Eclipse):**
    *   Vá para a configuração de execução/debug da sua aplicação Spring Boot.
    *   Adicione `-Dspring.profiles.active=dev` ao campo "VM options" ou "Program arguments".
    *   Alternativamente, defina a variável de ambiente `SPRING_PROFILES_ACTIVE=dev` na configuração de execução.

## Testando a Autenticação

1.  **Registro**: Use o endpoint `POST /api/auth/register` (via Swagger UI ou Postman) com um corpo JSON como:
    ```json

## Como Executar os Testes

Execute os testes usando o Maven:
```bash
mvn test
