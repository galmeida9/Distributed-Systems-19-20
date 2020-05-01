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
###### TODO:
Faltas toleradas
- Uma réplica falhar entre mensagens de um cliente
- Uma réplica falhar entre mensagens de outra réplica
- Endereços de réplicas inválidos
- Alteração dos endereços das réplicas
- Gestor de réplicas não dar nenhuma réplica

Faltas não toleradas
- Gestor de réplicas não aceitar réplicas
- Ordem nas mensagens trocada


## Solução
###### TODO:

_(Figura da solução de tolerância a faltas)_

_(Breve explicação da solução, suportada pela figura anterior)_


## Protocolo de replicação

### Explicação do protocolo

Este protocolo consiste na possibilidade de haver várias réplicas e vários clientes, suportando alta disponibilidade e tolerância a partições (AP no teorema CAP), excluindo assim a coerência forte.

Este protocolo pretende que as réplicas devolvam sempre resposta aos clientes e mesmo com partições na rede, enviando assincronamente as operações em fundo.

Cada réplica tem o seu número de instância (de 0 a 9) e permite operações de leitura e de atualização, sendo que estas últimas alteram o estado replicado.

Possui também um timestamp vetorial, de modo a saber quantas operações já efetuou. Isto permite às outras réplicas saber se precisa de receber as operações de outra réplica ou de enviar.

Como este protocolo pretende-se que as réplicas troquem mensagens entre si ao fim de `x` tempo (_gossip_), propagando assim as operações de atualização.


### Descrição das trocas de mensagens

De `x` em `x` segundos, cada gestor de réplica envia o seu timestamp para as outras réplicas. Cada gestor de réplica que o recebe, devolve o seu próprio timestamp.

O gestor da réplica que enviou o seu timestamp inicialmente compara o seu timestamp com o timestamp de cada outra réplica, verificando se precisa de enviar operações que a outra réplica não tenha, enviando apenas as que lhe faltam.

No lado contrário, o gestor da réplica que recebeu o timestamp inicialmente pode receber timestamps e operações, atualizando-as no seu servidor.

## Opções de implementação
###### TODO:

_(Descrição de opções de implementação, incluindo otimizações e melhorias introduzidas)_

Melhoria:
- Réplica apenas envia updates que a outra réplica precisa

## Notas finais

_(Algo mais a dizer?)_
