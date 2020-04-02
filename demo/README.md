
## Demo

### Eye

Para correr, é necessário antes fazer `mvn clean install` na pasta root do projeto e iniciar o silo-server.
De seguida, na pasta `eye` corre-se o comando `target/appassembler/bin/eye localhost 8080 Alameda 1 1 `.

#### Utilização

Para utilizar o cliente Eye, podemos enviar observações do tipo `person` com o comando `person,[id]`, observações do tipo `car` com o comando `car,[id]`, é possível também usar o comando `zzz,[tempo]`, em que tempo corresponde ao tempo que vai esperar em milissegundos até enviar as observações.
É possível também enviar comentários usando o comando `# [...]`.

#### Exemplos

Para demonstrar a utilização, temos o ficheiro `eye_1.txt`, que demonstra o Eye a receber variadas observações e a enviá-las no final.
No ficheiro `eye_2.txt`, envia-se observações do tipo `person` e envia-se para o servidor com sucesso.
No ficheiro `eye_3.txt`, envia-se observações do tipo `car` e envia-se para o servidor com sucesso.

No ficheiro `eye_4.txt`, demonstra-se a utilização de comentários e do comando `zzz` com 1000 milisegundos (1 segundo), enviando depois uma observação.


  