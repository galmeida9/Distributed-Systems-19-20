

## Demo

Primeiro é necessário instalar todos os módulos, para isso realizamos o comando `mvn clean install -DskipTests` na root do projeto.

### Server
Para correr o servidor realiza-se o comando `mvn clean compile exec:java` no diretório do servidor `silo-server/`.

#### Utilização
Para sair do servidor envia-se um `SIGINT`, ou seja, `CTRL+C`. Os dados guardados no servidor não são permanentes, ou seja, quando este é desligado todos os dados são apagados.

### Eye

Para correr, é necessário primeiro iniciar o silo-server.
De seguida, na pasta `eye` corre-se o comando `target/appassembler/bin/eye localhost 8080 Alameda 1 1 `.

#### Utilização

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

### Spotter

Para correr, é necessário primeiro iniciar o silo-server. De seguida, na pasta spotter corre-se o comando `target/appassembler/bin/spotter localhost 8080`.

#### Utilização

Para procurar a observação do carro ou pessoa com o identificador ou fragmento de identificador executar o comando `spot [tipo] [id/fragmentoId]`, obtendo como resultado observações com o formato: `Tipo,Identificador,Data-Hora,Nome-Câmera,Latitude-Câmera,Longitude-Câmera`.

Para procurar o caminho percorrido pelo carro ou pessoa com o identificador exato executar o comando `trail [tipo] [id]`, obtendo como resultado observações com o formato: Tipo,Identificador,Data-Hora,Nome-Câmera,Latitude-Câmera,Longitude-Câmera.

Para saber o estado do servidor executar o comando `ctrl_ping [mensagem]`, recebe como resposta Hello [mensagem]!</br>
Para limpar o servidor executar o comando `ctrl_clear`.</br>
Para inicializar parâmetros executar o `comando ctrl_init`.</br>
Tanto o comando ctrl_clear como o ctrl_init recebem como resposta do servidor o `[Status]`, sendo `OK` se tudo correu bem e `NOK` se ocorreram erros.

Para sair do cliente Spotter executar o comando0 `exit`.

Para obter informaçao sobre os comandos executar o comando `help`.

### Teste das operações

#### cam_join

Dando como argumentos o `zoohost zooport nome_da_camâra latitude longitude` e ainda tendo como argumento opcional a réplica a que se vai tentar conectar, a camâra tem de ter um id entre  3 e 15 (inclusive), por exemplo `$ eye localhost 2181 Alameda 1 2 1`, é rejeitado:

```
Login success.
```

Dando como argumento uma camâra com id menor que 3 ou maior que 15, como por exemplo `$ eye localhost 2181 ab 1 2 1` ou `% eye localhost 2181 aaaaaaaaaaaaaaaaaaab 1 2 1`, é rejeitado:

```
Login failure.
```

#### cam_info

#### report

Para demonstrar a utilização, temos o ficheiro `eye_1.txt`, que demonstra o Eye a receber variadas observações e a enviá-las no final:

```
person,5638247
Added a person with id 5638247.
car,20SD20
Added a car with id 20SD20.

Report was OK
```
No ficheiro `eye_2.txt`, envia-se observações do tipo `person` e envia-se para o servidor com sucesso:
```
person,5638246
Added a person with id 5638246.
person,6428365
Added a person with id 6428365.

Report was OK
```
No ficheiro `eye_3.txt`, envia-se observações do tipo `car` e envia-se para o servidor com sucesso:

```
car,20SD24
Added a car with id 20SD24.
car,20SD25
Added a car with id 20SD25.

Report was OK
```

No ficheiro `eye_4.txt`, demonstra-se a utilização de comentários e do comando `zzz` com 1000 milisegundos (1 segundo), enviando depois uma observação:

```
# Comentario
person,1
Added a person with id 1.
zzz,1000

Report was OK
```

No ficheiro `eye_5.txt`, demonstra-se a tentativa de enviar observações de um carro com ID inválido:

```
car,1
Added a car with id 1.

Report was NOK
```

No ficheiro `eye_6.txt`, demonstra-se a tentativa de enviar observações de uma pessoa com ID inválido:

```
person,abc
Added a person with id abc.

Report was NOK
```

#### track

Para as seguintes demonstrações foram usados alguns dos exemplos da secção `report`:
```
$ eye localhost 2181 Tagus 1 2 1 < eye1.txt
$ eye localhost 2181 Tagus 1 2 1 < eye2.txt
$ eye localhost 2181 Tagus 1 2 1 < eye3.txt
$ eye localhost 2181 Tagus 1 2 1 < eye4.txt
```

No ficheiro `spotter_1.txt`, procura a observação do carro usando o identificador do carro, que terá como resultado: 
```
spot car 20SD24
car,20SD24,2020-05-01T22:25:34.025821500,Tagus,1.0,2.0
```

No ficheiro `spotter_2.txt`, procura a observação da pessoa usando o identificador da pessoa, que terá como resultado: 
```
spot person 6428365
person,6428365,2020-05-01T22:25:23.682830600,Tagus,1.0,2.0
```

#### track_match

No ficheiro `spotter_3.txt`, procura a observação do carro usando um fragmento do identificador do carro, que terá como resultado:
```
spot car 20SD*
car,20SD20,2020-05-01T22:25:15.161571700,Tagus,1.0,2.0
car,20SD24,2020-05-01T22:25:34.025821500,Tagus,1.0,2.0
car,20SD25,2020-05-01T22:25:34.028785700,Tagus,1.0,2.0
```

#### trace

No ficheiro `spotter_4.txt`, procura o caminho percorrido pelo carro usando o identificador do carro, que terá como resultado: 
```
trail car 20SD24
car,20SD24,2020-05-01T22:25:34.025821500,Tagus,1.0,2.0
```
