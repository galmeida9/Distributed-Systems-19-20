

# Guião de Demonstração

## 1. Preparação do Sistema

Primeiro é necessário instalar todos os módulos, para isso realizamos o comando `mvn clean install -DskipTests` na root do projeto.

### 1.1. Server
Para correr o servidor realiza-se o comando `mvn clean compile exec:java` no diretório do servidor `silo-server/`.

#### 1.1.1. Utilização
Para sair do servidor envia-se um `SIGINT`, ou seja, `CTRL+C`. Os dados guardados no servidor não são permanentes, ou seja, quando este é desligado todos os dados são apagados.

### 1.2. Eye

Para correr, é necessário primeiro iniciar o silo-server.
De seguida, na pasta `eye` corre-se o comando `target/appassembler/bin/eye localhost 8080 Alameda 1 1 `.

#### 1.2.1 Utilização

Para utilizar o cliente Eye, podemos enviar observações do tipo `person` com o comando `person,[id]`
 e observações do tipo `car` com o comando `car,[id]`, em que `[id]` é o identificador que se deseja enviar. 
 O cliente deve responder `Added a [type] with id [id].`, 
 em que `type` corresponde ao tipo enviado e `[id]` corresponde ao identificador enviado..
 
É possível também usar o comando `zzz,[tempo]`, em que `[tempo]` corresponde ao tempo que vai esperar 
em milissegundos após o envio do comando. Este comando não imprime nada.

É possível também enviar comentários usando o comando `# [...]`.

Ao enviar uma linha vazia, o cliente envia as observações para o servidor e responde `Report was [Status]`,
 em que `[Status]` corresponde ao estado que o servidor devolve, sendo `OK` se tudo correu bem e 
 `NOK` se ocorreram erros ou até uma mensagem diferente caso tenha sido registada uma observação com
 argumentos errados.

### 1.3. Spotter

Para correr, é necessário primeiro iniciar o silo-server. De seguida, na pasta spotter corre-se o comando `target/appassembler/bin/spotter localhost 8080`.

#### 1.3.1 Utilização

Para procurar a observação do carro ou pessoa com o identificador ou fragmento de identificador executar o comando `spot [tipo] [id/fragmentoId]`, obtendo como resultado observações com o formato: `Tipo,Identificador,Data-Hora,Nome-Câmera,Latitude-Câmera,Longitude-Câmera`.

Para procurar o caminho percorrido pelo carro ou pessoa com o identificador exato executar o comando `trail [tipo] [id]`, obtendo como resultado observações com o formato: Tipo,Identificador,Data-Hora,Nome-Câmera,Latitude-Câmera,Longitude-Câmera.

Para saber o estado do servidor executar o comando `ctrl_ping [mensagem]`, recebe como resposta Hello [mensagem]!</br>
Para limpar o servidor executar o comando `ctrl_clear`.</br>
Para inicializar parâmetros executar o `comando ctrl_init`.</br>
Tanto o comando ctrl_clear como o ctrl_init recebem como resposta do servidor o `[Status]`, sendo `OK` se tudo correu bem e `NOK` se ocorreram erros.

Para sair do cliente Spotter executar o comando0 `exit`.

Para obter informaçao sobre os comandos executar o comando `help`.

## 2. Teste das operações

### 2.1 cam_join

Dando como argumentos o `zoohost zooport nome_da_camâra latitude longitude` e ainda tendo como argumento opcional a réplica a que se vai tentar conectar, a camâra tem de ter um id entre  3 e 15 (inclusive), por exemplo `$ eye localhost 2181 Alameda 1 2 1`, é rejeitado:

```
Login success.
```

Dando como argumento uma camâra com id menor que 3 ou maior que 15, como por exemplo `$ eye localhost 2181 ab 1 2 1` ou `% eye localhost 2181 aaaaaaaaaaaaaaaaaaab 1 2 1`, é rejeitado:

```
Login failure.
```

### 2.2  cam_info

### 2.3. report

Para demonstrar a utilização, temos o ficheiro `eye_1.txt`, que demonstra o Eye a receber variadas observações e a enviá-las no final:

```
> person,5638247
Added a person with id 5638247.
> car,20SD20
Added a car with id 20SD20.
> 
Report was OK
```
No ficheiro `eye_2.txt`, envia-se observações do tipo `person` e envia-se para o servidor com sucesso:
```
> person,5638246
Added a person with id 5638246.
> person,6428365
Added a person with id 6428365.
> 
Report was OK
```
No ficheiro `eye_3.txt`, envia-se observações do tipo `car` e envia-se para o servidor com sucesso:

```
> car,20SD24
Added a car with id 20SD24.
> car,20SD25
Added a car with id 20SD25.
> 
Report was OK
```

No ficheiro `eye_4.txt`, demonstra-se a utilização de comentários e do comando `zzz` com 1000 milisegundos (1 segundo), enviando depois uma observação:

```
> # Comentario
> person,1
Added a person with id 1.
> zzz,1000
> 
Report was OK
```

No ficheiro `eye_5.txt`, demonstra-se a tentativa de enviar observações de um carro com ID inválido:

```
> car,1
Added a car with id 1.
> 
Report was NOK
```

No ficheiro `eye_6.txt`, demonstra-se a tentativa de enviar observações de uma pessoa com ID inválido:

```
> person,abc
Added a person with id abc.
> 
Report was NOK
```

### 2.4. track

Para as seguintes demonstrações foram usados alguns dos exemplos da secção `report`:
```
$ eye localhost 2181 Tagus 1 2 1 < eye1.txt
$ eye localhost 2181 Tagus 1 2 1 < eye2.txt
$ eye localhost 2181 Tagus 1 2 1 < eye3.txt
$ eye localhost 2181 Tagus 1 2 1 < eye4.txt
```

No ficheiro `spotter_1.txt`, procura a observação do carro usando o identificador do carro, que terá como resultado: 
```
> spot car 20SD24
car,20SD24,2020-05-01T22:25:34.025821500,Tagus,1.0,2.0
```

No ficheiro `spotter_2.txt`, procura a observação da pessoa usando o identificador da pessoa, que terá como resultado: 
```
> spot person 6428365
person,6428365,2020-05-01T22:25:23.682830600,Tagus,1.0,2.0
```

### 2.5. track_match

No ficheiro `spotter_3.txt`, procura a observação do carro usando um fragmento do identificador do carro, que terá como resultado:
```
> spot car 20SD*
car,20SD20,2020-05-01T22:25:15.161571700,Tagus,1.0,2.0
car,20SD24,2020-05-01T22:25:34.025821500,Tagus,1.0,2.0
car,20SD25,2020-05-01T22:25:34.028785700,Tagus,1.0,2.0
```

### 2.6. trace

No ficheiro `spotter_4.txt`, procura o caminho percorrido pelo carro usando o identificador do carro, que terá como resultado: 
```
> trail car 20SD24
car,20SD24,2020-05-01T22:25:34.025821500,Tagus,1.0,2.0
```

## 3. Replicação e tolerância a faltas

### 3.1. Comandos

#### 3.1.1. Lançar réplicas

Para se abrir um servidor usam-se os seguintes argumentos obrigatórios: `zooHost zooPort instance host port`, em que *instance* é o número da réplica do servidor. Exemplo:
`$ silo-server locahost 2181 1 localhost 8080`

Se tentarmos utilizar o mesmo porto ou caminho de uma réplica já corrente, é nos devolvida a seguinte mensagem `Failed to bind`, seguido da terminação do servidor.

#### 3.1.2. Fornecer dados

Para se fornecerem dados aos servidores é usada a aplicação *eye* e requer os seguintes argumentos: `zooHost zooPort` e tendo como argumento opcional a réplica a que se quer ligar, se não for fornecida uma réplica, liga-se a uma réplica aleatória, se for, tenta-se conectar à escolhida, se esta não existir o *eye* termina.

#### 3.1.3. Fazer interrogações

Para se fazerem interrogações ao servidor é o usado o *spotter* e requer os mesmos argumentos que o *eye*, tendo o exato mesmo comportamento que ele.

### 3.2. Funcionamento normal

As réplicas 30 em 30 segundos mandam mensagens de gossip entre si em background, para não impedir o cliente, para se atualizarem em relação ao que foi feito nas outras réplicas. Neste exemplo a réplica um inicia o gossip e contacta outra réplica para lhe enviar uma câmara que se conectou a ela:

```
Replica 1 initiating gossip...

Contacting replica 2 at localhost:8082...
Sending timestamp {1=1}...
Received timestamp {2=0} from replica 2
Sending Camera Tagus
```

Já neste exemplo enviar observações:

```
Replica 1 initiating gossip...

Contacting replica 2 at localhost:8082...
Sending timestamp {1=3}...
Received timestamp {1=1, 2=0} from replica 2
Sending Observation of PERSON 5638247
Sending Observation of CAR 20SD20
```

A réplica do outro lado recebe os novos dados:

```
Received a Camera Tagus from replica 1
```

```
Replica 2 initiating gossip...

Contacting replica 1 at localhost:8081...
Sending timestamp {1=1, 2=0}...
Received timestamp {1=1} from replica 1
Received a Observation of PERSON 5638247 from replica 1
Received a Observation of CAR 20SD20 from replica 1
Replica 2 initiating gossip...
```

### 3.3. Tolerância a faltas

As faltas toleradas e o seu respetivo comportamento são as seguintes:

#### 3.3.1 Réplica falhar entre mensagens de um cliente

Para testarmos isto antes de darmos report no *eye* fechamos um servidor.

Neste caso o cliente vai tentar até 3 vezes tendo cada pedido uma duração máxima de 5 segundos, se ao fim dessas 3 tentativas não conseguir muda de servidor e tenta de novo. No exemplo seguinte temos duas réplicas, a 1 e a 2 em que o cliente *eye* se conecta à 1, mas a 1 antes de o *eye* realizar um report vai abaixo.

```
Login success.
person,1
Added a person with id 1.

Retrying request.
Retrying request.
Retrying request.
Failed to retry request, changing server and trying again.
Connected to replica 2
Report was OK
```

Na réplica 2 aparece-nos uma mensagem a afirmar que recebemos o report:

```
Received a Observation of PERSON 1 from client
```

O mesmo acontece com o *spotter*:

```
spot car 20SD20
Retrying request.
Retrying request.
Retrying request.
Failed to retry request, changing server and trying again.
Connected to replica 2
car,20SD20,2020-05-02T10:44:56.741541300,Tagus,1.0,2.0
```

Na réplica 2 aparece-nos uma mensagem a afirmar que recebemos o report:

```
Received a Observation of PERSON 1 from client
```

#### 3.3.2. Uma réplica falhar entre mensagens de outra réplica

Se uma réplica falhar durante a troca de mensagens entre outra réplica, que pode ser replicado usando um software como o IntelliJ para parar uma das réplicas antes de ela responder ao pedido de gossip, a réplica que iniciou o diálogo vai tentar 3 vezes antes de abortar:

```
Replica 1 initiating gossip...

Contacting replica 2 at localhost:8082...
Sending timestamp {1=0}...
Response took too long sending request at replica 2

Replica 1 initiating gossip...

Contacting replica 2 at localhost:8082...
Sending timestamp {1=0}...
Received timestamp {2=0} from replica 2
```

Neste exemplo se entretanto a réplica parada resumir a atividade, como podemos ver, recebe a mensagem de gossip.

#### 3.3.3. Endereços de réplicas inválidos (no contexto das réplicas)

Se uma das réplicas tiver um path ou porta errada e outra se tentar conectar a ela, basta iniciar uma réplica com um path errado como `locahost`, manda mensagem afirmando que a réplica destino não está disponível:

```
Contacting replica 2 at locahost:8082...
Sending timestamp {1=0}...
Replica not available when trying to send request at replica 2
```

#### 3.3.4. Alteração dos endereços das réplicas

Pode acontecer a uma replica falhar e ir abaixo e aparecer com um endereço diferente, podemos ver o comportamento no exemplo seguinte:

```
Replica 1 initiating gossip...

Contacting replica 2 at localhost:8082...
Sending timestamp {1=1}...
Received timestamp {2=0} from replica 2
Sending Camera Tagus

Replica 1 initiating gossip...

Contacting replica 2 at localhost:8083...
Sending timestamp {1=1}...
Received timestamp {2=0} from replica 2
Sending Camera Tagus
```

Como podemos ver a replica 2 estava situada no `localhost:8082` e depois passou a estar no `localhost:8083`. Para simularmos isto basta fechar uma réplica a meio da execução e voltar a abri-la com um path diferente.

#### 3.3.5. Ordem de mensagens trocada

Em princípio toleramos esta falta, dizemos isto, visto que não encontramos nenhuma maneira de a verificar, visto que teríamos que trocar a ordem de como o código é executado, o que pelo nosso conhecimento não é possível, mesmo com um debugger não é possível saltar código, é sempre executável, ou seja se quisessemos enviar uma mensagem de gossip com uma câmara e uma observação reportada por essa mesma câmara, o nosso código permite, que a observação seja adicionada primeiro, mas a mensagem é sempre enviada com a câmara em primeiro lugar, por motivos óbvios, visto que como é a câmara que reporta, ela deve ser adicionada primeiro, mas como já referimos não temos maneira eficaz de trocar a ordem da mensagem.
