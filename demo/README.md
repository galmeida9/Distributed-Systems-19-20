
## Demo

Primeiro é necessário instalar o contrato, para isso realizamos o comando `mvn clean install` na root do projeto.

### Server
Para correr o servidor realiza-se o comando `mvn clean compile exec:java` no diretório do servidor `silo-server/`.

#### Utilização
Para sair do servidor envia-se um `SIGINT`, ou seja, `CTRL+C`. Os dados guardados no servidor não são permanentes, ou seja, quando este é desligado todos os dados são apagados.

### Eye

Para correr, é necessário primeiro iniciar o silo-server.
De seguida, na pasta `eye` corre-se o comando `target/appassembler/bin/eye localhost 8080 Alameda 1 1 `.

#### Utilização

Para utilizar o cliente Eye, podemos enviar observações do tipo `person` com o comando `person,[id]`
 e observações do tipo `car` com o comando `car,[id]`. O cliente deve imprimir uma linha confirmando
 o registo local da observação.
 
É possível também usar o comando `zzz,[tempo]`, em que `[tempo]` corresponde ao tempo que vai esperar 
em milissegundos após o envio do comando. Este comando não imprime nada.

É possível também enviar comentários usando o comando `# [...]`.

Ao enviar uma linha vazia, o cliente envia as observações para o servidor e responde `Report was [Status]`,
 em que `[Status]` corresponde ao estado que o servidor devolve, sendo `OK` se tudo correu bem e 
 `NOK` se ocorreram erros ou até uma mensagem diferente caso tenha sido registada uma observação com
 argumentos errados.

#### Exemplos

Para demonstrar a utilização, temos o ficheiro `eye_1.txt`, que demonstra o Eye a receber variadas observações e a enviá-las no final.
No ficheiro `eye_2.txt`, envia-se observações do tipo `person` e envia-se para o servidor com sucesso.
No ficheiro `eye_3.txt`, envia-se observações do tipo `car` e envia-se para o servidor com sucesso.

No ficheiro `eye_4.txt`, demonstra-se a utilização de comentários e do comando `zzz` com 1000 milisegundos (1 segundo), enviando depois uma observação.

### Spotter

Para correr, é necessário primeiro iniciar o silo-server.
De seguida, na pasta `spotter` corre-se o comando `target/appassembler/bin/spotter localhost 8080`.

#### Utilização

Para procurar a observação do carro ou pessoa com o identificador ou fragmento de identificador executar o comando `spot [tipo] [id/fragmentoId]`, obtendo como resultado observações com o formato: Tipo,Identificador,Data-Hora,Nome-Câmera,Latitude-Câmera,Longitude-Câmera. 

Para procurar o caminho percorrido pelo carro ou pessoa com o identificador exato executar o comando `trail [tipo] [id]` , obtendo como resultado observações com o formato: Tipo,Identificador,Data-Hora,Nome-Câmera,Latitude-Câmera,Longitude-Câmera.

Para saber o estado do servidor executar o comando `ctrl_ping`, recebe como resposta `Hello [mensagem]!`<br/>
Para limpar o servidor executar o comando `ctrl_clear`.<br/>
Para inicializar parâmetros executar o comando `ctrl_init`.<br/>
Tanto o comando `ctrl_clear` como o `ctrl_init`recebem como resposta do servidor o `[Status]`, sendo `OK` se tudo correu bem e `NOK` se ocorreram erros.<br/>

Para sair do cliente Spotter executar o comando `exit`.<br/>

Para obter informaçao sobre os comandos executar o comando `help`.<br/>

#### Exemplos

No ficheiro `spotter_1.txt`, procura a observação do carro usando o identificador do carro, que terá como resultado: car,7013LL,2019-10-04T10:02:07,Tagus,38.737613,-9.303164 <br/>
No ficheiro `spotter_2.txt`, procura a observação da pessoa usando o identificador da pessoa, que terá como resultado:
person,111,2019-10-04T10:02:07,Tagus,38.737613,-9.303164<br/>
No ficheiro `spotter_3.txt`, procura a observação do carro usando um fragmento do identificador do carro, que terá como resultado:
car,5759LL,2019-10-22T09:07:51,Tagus,38.737613,-9.303164<br/>
car,7013LL,2019-10-04T11:02:07,Tagus,38.737613,-9.303164<br/>
No ficheiro `spotter_4.txt`, procura o caminho percorrido pelo carro usando o identificador do carro, que terá como resultado:
car,7013LL,2019-10-04T10:02:07,Tagus,38.737613,-9.303164<br/>
car,7013LL,2019-10-03T08:10:20,Alameda,38.736748,-9.138908<br/>
car,7013LL,2019-10-02T22:33:01,Tagus,38.737613,-9.303164<br/>
No ficheiro `spotter_5.txt`, utiliza-se os comandos de controlo para saber o estado do servidor e de seguida limpá-lo que devolve: Hello hello! e de seguida `CtlrClear was OK`.



