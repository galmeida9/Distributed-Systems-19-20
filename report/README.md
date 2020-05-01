# Relatório do projeto Sauron

Sistemas Distribuídos 2019-2020, segundo semestre

## Autores
  
**Grupo A09** 

| Número | Nome                 | Utilizador                        | Correio eletrónico                                |
| -------|----------------------|-----------------------------------| --------------------------------------------------|
| 89423  | Catarina Machuqueiro | <https://github.com/Catarinaibm>  | <mailto:catarinamachuqueiro@tecnico.ulisboa.pt>   |
| 89446  | Gabriel Almeida      | <https://github.com/galmeida9>    | <mailto:gabriel.almeida@tecnico.ulisboa.pt>       |
| 91004  | Daniel Gonçalves     | <https://github.com/masterzeus05> | <mailto:daniel.a.goncalves@tecnico.ulisboa.pt>    |

![Catarina](catarina.png) ![Gabriel](gabriel.png) ![Daniel](daniel.png)


## Melhorias da primeira parte

- [Testes do `cam_join` para limites inferiores e superiores](https://github.com/tecnico-distsys/A09-Sauron/commit/b656a724d092cfff34f1389078657b251da2bef7)
- [Testes de sucesso para o `report` e `track`](https://github.com/tecnico-distsys/A09-Sauron/commit/b656a724d092cfff34f1389078657b251da2bef7)
- [Utilização do Status do GRPC](https://github.com/tecnico-distsys/A09-Sauron/commit/b656a724d092cfff34f1389078657b251da2bef7)
- [Criação de um módulo para o domínio, com a criação de classes](https://github.com/tecnico-distsys/A09-Sauron/commit/b656a724d092cfff34f1389078657b251da2bef7)
- [Métodos enviam exceções em caso de erro](https://github.com/tecnico-distsys/A09-Sauron/commit/b656a724d092cfff34f1389078657b251da2bef7)
- [Erros no servidor são enviados em exceções para o cliente](https://github.com/tecnico-distsys/A09-Sauron/commit/b656a724d092cfff34f1389078657b251da2bef7)
- [Utilização correta de estruturas concorrentes](https://github.com/tecnico-distsys/A09-Sauron/commit/55b2f3139079c287b379dad2216786e16ecf4bb4)

## Modelo de faltas

Faltas toleradas
- Uma réplica falhar entre mensagens de um cliente
- Uma réplica falhar entre mensagens de outra réplica
- Endereços de réplicas inválidos
- Gestor de réplicas não dar nenhuma réplica

Faltas não toleradas
- Gestor de réplicas não aceitar réplicas


## Solução

_(Figura da solução de tolerância a faltas)_

_(Breve explicação da solução, suportada pela figura anterior)_


## Protocolo de replicação

_(Explicação do protocolo)_

_(descrição das trocas de mensagens)_


## Opções de implementação

_(Descrição de opções de implementação, incluindo otimizações e melhorias introduzidas)_



## Notas finais

_(Algo mais a dizer?)_
