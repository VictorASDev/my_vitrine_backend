# MyVitrine — Backend

Backend da **MyVitrine**, uma plataforma que conecta lojistas, afiliados e criadores de conteúdo.

A plataforma permite que lojistas cadastrem produtos, que afiliados gerem links e cupons de divulgação com rastreamento de vendas e cálculo automático de comissão, e que lojistas contratem criadores de conteúdo para produção de material promocional, com controle de cachê.

## Sumário

- [Arquitetura](#arquitetura)
- [Modelo de dados](#modelo-de-dados)
- [Tecnologias](#tecnologias)
- [Como rodar](#como-rodar)
- [Como rodar com Docker](#como-rodar-com-docker)
- [Executar os testes](#executar-os-testes)
- [Documentação da API](#documentação-da-api)

## Arquitetura

O projeto segue uma arquitetura em camadas:

```text
Controller → Service → Repository → Database
```

| Camada | Responsabilidade |
|---|---|
| `Controller` | Recebe as requisições HTTP, valida os dados e retorna as respostas. |
| `Service` | Concentra as regras de negócio e operações transacionais. |
| `Repository` | Responsável pela persistência dos dados utilizando Spring Data JPA. |
| `Model` | Contém as entidades JPA. |
| `DTO` | Define os objetos de entrada e saída da API. |

Estrutura principal:

```text
com.myvitrine.api
├── controller
├── dto
│   ├── request
│   └── response
├── model
├── repository
└── service
```

O banco de dados é gerenciado exclusivamente pelo Flyway. O Hibernate utiliza `ddl-auto=validate` apenas para validar os mapeamentos das entidades — as migrations é que definem o schema.

## Modelo de dados

O diagrama abaixo representa as entidades do MVP e seus relacionamentos: um usuário pode possuir os perfis de lojista, afiliado e/ou criador; lojistas publicam produtos; afiliados geram links para produtos, que originam vendas e comissões; e lojistas podem contratar criadores para produzir conteúdo sobre um produto, gerando um cachê.

```mermaid
erDiagram
    USUARIO ||--o| PERFIL_LOJISTA : possui
    USUARIO ||--o| PERFIL_AFILIADO : possui
    USUARIO ||--o| PERFIL_CRIADOR : possui
    PERFIL_LOJISTA ||--o{ PRODUTO : publica
    PERFIL_AFILIADO ||--o{ LINK_AFILIADO : gera
    PRODUTO ||--o{ LINK_AFILIADO : referenciado_em
    LINK_AFILIADO ||--o{ VENDA : gera
    VENDA ||--|| COMISSAO : origina
    PERFIL_LOJISTA ||--o{ CONTRATACAO : solicita
    PERFIL_CRIADOR ||--o{ CONTRATACAO : aceita
    PRODUTO ||--o{ CONTRATACAO : tema_de
    CONTRATACAO ||--|| CACHE : origina

    USUARIO {
        uuid id PK
        string nome
        string email
        string senha_hash
        string tipo_perfil
        datetime criado_em
    }
    PERFIL_LOJISTA {
        uuid usuario_id PK
        string nome_loja
        string descricao
    }
    PERFIL_AFILIADO {
        uuid usuario_id PK
        string bio
        string nicho
    }
    PERFIL_CRIADOR {
        uuid usuario_id PK
        string bio
        string profile_photo_url
    }
    PRODUTO {
        uuid id PK
        uuid lojista_id FK
        string nome
        decimal preco
        decimal comissao_percentual
        string imagem_url
        boolean ativo
        datetime criado_em
    }
    LINK_AFILIADO {
        uuid id PK
        uuid afiliado_id FK
        uuid produto_id FK
        string codigo
        string tipo
        datetime criado_em
    }
    VENDA {
        uuid id PK
        uuid link_afiliado_id FK
        decimal valor
        datetime data_venda
    }
    COMISSAO {
        uuid id PK
        uuid venda_id FK
        decimal valor_comissao
        decimal valor_retencao
        string status
    }
    CONTRATACAO {
        uuid id PK
        uuid lojista_id FK
        uuid criador_id FK
        uuid produto_id FK
        string status
        datetime criado_em
    }
    CACHE {
        uuid id PK
        uuid contratacao_id FK
        decimal valor
        decimal valor_retencao
        string status
    }
```

> O diagrama é renderizado automaticamente pelo GitHub/GitLab (suporte nativo a Mermaid). Caso visualize em um ambiente sem esse suporte, cole o bloco acima em [mermaid.live](https://mermaid.live).

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- Spring Security / OAuth2
- Springdoc OpenAPI
- JUnit 5 e Mockito

## Como rodar com Docker

### Pré-requisitos

- Docker Engine
- Docker Compose

Na raiz do projeto, execute:

```bash
docker compose up --build
```

A API ficará disponível em `http://localhost:8080`. Para executar em segundo plano:

```bash
docker compose up --build -d
```

Para interromper os containers:

```bash
docker compose down
```

Os dados do PostgreSQL são mantidos no volume `postgres_data`. Para removê-los junto com os containers:

```bash
docker compose down -v
```

### Variáveis do Docker Compose

Crie um arquivo `.env` na raiz caso precise alterar os valores padrão:

```dotenv
POSTGRES_DB=myvitrine
POSTGRES_USER=postgres
POSTGRES_PASSWORD=123456
POSTGRES_PORT=5432
API_PORT=8080
CORS_ORIGINS=http://localhost:5173
JWT_COOKIE_SECURE=false
```

Em produção, use uma senha forte, mantenha `JWT_COOKIE_SECURE=true` quando a API estiver atrás de HTTPS e configure chaves RSA persistentes em `JWT_PRIVATE_KEY` e `JWT_PUBLIC_KEY`. Sem essas chaves, a aplicação gera um par temporário apenas para desenvolvimento.

## Como rodar

### Pré-requisitos

- Java 21
- PostgreSQL
- Maven

### Configuração do banco

Crie o banco:

```sql
CREATE DATABASE myvitrine;
```

Configure as credenciais por variáveis de ambiente ou no `application.yml`:

```bash
DB_URL=jdbc:postgresql://localhost:5432/myvitrine
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

O Hibernate usa `ddl-auto=validate`; a estrutura do banco é criada e atualizada exclusivamente pelas migrations do Flyway.

### Executar a aplicação

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

As migrations do Flyway são executadas automaticamente ao iniciar a aplicação.

## Executar os testes

```bash
./mvnw.cmd test
```

No Linux/macOS, use `./mvnw test`.

## Documentação da API

A API utiliza OpenAPI/Swagger para documentação dos endpoints.

Após iniciar a aplicação, a documentação pode ser acessada pela interface do Swagger configurada pelo Springdoc, normalmente em `http://localhost:8080/swagger-ui.html`.
