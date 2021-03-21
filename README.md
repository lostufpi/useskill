# UseSkill
UseSkill: uma ferramenta de apoio à avaliação de usabilidade de sistemas Web.

## Índice
- [Configuração do ambiente para desenvolvimento](#configuração-do-ambiente-para-desenvolvimento)
  - [Aplicação principal: UseSkill Control e UseSkill On The Fly](#aplicação-principal-useskill-control-e-useskill-on-the-fly)
    - [Requisitos](#requisitos)
    - [Configurando Java, Maven e Tomcat](#configurando-java-maven-e-tomcat)
    - [Configurando o banco de dados](#configurando-o-banco-de-dados)
      - [Instalação e configuração do MySQL](#instalação-e-configuração-do-mysql)
      - [Criação das tabelas para a aplicação](#criação-das-tabelas-para-a-aplicação)
    - [Deploy da aplicação](#deploy-da-aplicação)
    - [Problemas comuns e possíveis soluções](#problemas-comuns-e-possíveis-soluções)
    - [Alternativa ao Tomcat: Jetty](#alternativa-ao-tomcat-jetty)
  - [UseSkill Capture](#useskill-capture)
    - [Requisitos](#requisitos-1)
    - [Configuração para o ambiente de desenvolvimento](#configuração-para-o-ambiente-de-desenvolvimento)
    - [Instalação de dependências e execução](#instalação-de-dependências-e-execução)

## Configuração do ambiente para desenvolvimento
Algumas alterações e ferramentas são necessárias para preparar a UseSkill para um ambiente de desenvolvimento. Esta seção mostra os requisitos e passos para esta configuração.

### Aplicação principal: UseSkill Control e UseSkill On The Fly

#### Requisitos
* [JDK (Java Development Kit)](https://www.oracle.com/br/java/technologies/javase/javase-jdk8-downloads.html)
* [Apache Tomcat 7](https://tomcat.apache.org/download-70.cgi)
* [Apache Maven 3](https://maven.apache.org/download.cgi)
* [MySQL Server e MySQL Workbench](https://dev.mysql.com/downloads/installer/)
* Uma IDE de sua preferência, como Eclipse, Netbeans ou Visual Studio Code

#### Configurando Java, Maven e Tomcat
Após a conclusão do download, faça a instalação do JDK 8. Em seguida, extraia o conteúdo do Apache Tomcat 7 e o Apache Maven para uma pasta de sua preferência (Ex.: `C:\apache-tomcat-7.0.107` e `C:\apache-maven-3.6.3`).
Em seguida, devem ser adicionadas variáveis de ambiente no sistema para que possamos executar o Java, o Maven e o Tomcat a partir de qualquer caminho na linha de comando.

| Nome da variável | Valor da variável                                                      |
| ---------------- | ---------------------------------------------------------------------- |
| JAVA_HOME        | `caminho_do_java` (Geralmente em `C:\Program Files\Java\jdk1.8.0_211`) |
| MAVEN_HOME       | `caminho_do_maven` (Ex.: `C:\apache-maven-3.6.3`)                      |
| M2_HOME          | `caminho_do_maven` (Ex.: `C:\apache-maven-3.6.3`)                      |
| CATALINA_HOME    | `caminho_do_tomcat` (Ex.: `C:\apache-tomcat-7.0.107`)                  |

Depois, adicione esses três caminhos no PATH:
```
%JAVA_HOME%\bin
%M2_HOME%\bin
%CATALINA_HOME%\bin
```
Para verificar se os requisitos foram corretamente configurados, teste os seguintes comandos no terminal e observe se a saídas ficam semelhantes:
* JDK
```
C:\WINDOWS\System32>java -version
java version "1.8.0_211"
Java(TM) SE Runtime Environment (build 1.8.0_211-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.211-b12, mixed mode)
```
* Maven
```
C:\WINDOWS\System32>mvn -v
Apache Maven 3.6.3 (cecba232b5843edd3a6002696d04abb50b1b883f)
Maven home: C:\apache-maven-3.6.3\bin\..
Java version: 1.8.0_211, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk1.8.0_211\jre
Default locale: pt_BR, platform encoding: Cp1252
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
```
* Tomcat
```
C:\WINDOWS\System32>catalina version
Using CATALINA_BASE:   "C:\apache-tomcat-7.0.107"
Using CATALINA_HOME:   "C:\apache-tomcat-7.0.107"
Using CATALINA_TMPDIR: "C:\apache-tomcat-7.0.107\temp"
Using JRE_HOME:        "C:\Program Files\Java\jdk1.8.0_281"
Using CLASSPATH:       "C:\apache-tomcat-7.0.107\bin\bootstrap.jar;C:\apache-tomcat-7.0.107\bin\tomcat-juli.jar"
Using CATALINA_OPTS:   ""
Server version: Apache Tomcat/7.0.108
Server built:   Jan 28 2021 09:12:57 UTC
Server number:  7.0.108.0
OS Name:        Windows 10
OS Version:     10.0
Architecture:   amd64
JVM Version:    1.8.0_281-b09
JVM Vendor:     Oracle Corporation
```
Em caso de erro de comando desconhecido, algum dos passos não foi configurado com sucesso.
Para finalizar, é necessário fazer algumas configurações no Maven e no Tomcat para que o deploy da aplicação possa ser feito com sucesso.
* Maven: vá até a pasta onde está o Apache Maven (Ex.: `C:\apache-maven-3.6.3`) e dentro da pasta `conf` abra o arquivo `settings.xml` com o editor de texto de sua preferência. Adicione dentro da tag *servers*:
```xml
<servers>
  ...
  <server>
    <id>tomcatserver</id>
    <username>deployer</username>
    <password>deployer</password>
  </server>
  ...
</servers>
```
Salve o arquivo.
* Tomcat: vá até a pasta onde está o Apache Tomcat (Ex.: `C:\apache-tomcat-7.0.107`) e dentro da pasta `conf` abra o arquivo `tomcat-users.xml` com o editor de texto de sua preferência. Adicione dentro da tag *tomcat-users*:
```xml
<tomcat-users [...]>
  ...
  <role rolename="admin-gui"/>
  <role rolename="manager-gui"/>
  <role rolename="manager-script"/>
  <user username="admin" password="admin" roles="admin-gui,manager-gui"/>
  <user username="deployer" password="deployer" roles="manager-script"/>
  ...
</tomcat-users>
```
Com isso, estão criados os cargos `admin` (para usar o manager) e o cargo `deployer` (para o deploy usando o plugin do Tomcat 7 para o Maven).

#### Configurando o banco de dados

##### Instalação e configuração do MySQL
> **Obs.:** O MySQL Installer só funciona no Microsoft Windows. Em outros sistemas, faça a instalação e configuração do MySQL Server e MySQL Workbench separadamente.

Abra o MySQL Installer e aguarde o carregamento.
* Na primeira etapa, *Choosing a Setup Type*, escolha **Custom**;
* Em seguida, em *Select Products*, selecione um MySQL Server 5 (Ex.: `MySQL Server 5.6.51 - X64`) e clique na seta verde para adicionar;
* Em *Applications*, selecione um MySQL Workbench (Ex.: `MySQL Workbench 8.0.23 - X64`) e adicione;
* [Opcional para uso na linha de comando] Ainda em *Applications*, selecione um MySQL Shell (Ex.: `MySQL Shell 8.0.23 - X64`) e adicione;
* Na etapa seguinte, *Check Requeriments*, se houver alguma pendência, clique em um item da lista e em *Execute*. Repita para todas as que houverem;
* Na etapa *Downloads*, você confere o que vai ser baixado. Clique em *Execute* para iniciar;
* Na etata *Installation*, clique em *Execute* para iniciar a instalação. Aguarde e finalize;
* Vá até o menu Iniciar e abra novamente o *MySQL Installer - Community*;
* Em MySQL Server, na coluna *Quick Action* clique em *Reconfigure*;
* Na etapa *Type and Networking*, mantenha as configurações e clique em *Next*;
* Em *Authentication Method* selecione **Use Legacy Authentication Method...** para manter a compatibilidade. Clique em *Next*;
* Em *Accounts and Roles* defina uma senha (geralmente `root`). Clique em *Next*;
* Em *Windows Service* mantenha selecionadas as caixas **Configure MySQL as a Windows Service** e **Start the MySQL Server at System Startup**. Em *Run Windows Service as...* mantenha a opção selecionada. Clique em *Next*;
* Para finalizar, em *Apply Configuration*, clique em *Execute*. Ao conclur, clique em *Finish*.

Assim, o serviço do MySQL estará configurado e executando para realizar as conexões.

##### Criação das tabelas para a aplicação
Abra o MySQL Workbench e crie uma conexão. Crie as tabelas de acordo com a configuração em `Usabilidade/src/main/resources/hibernate.properties` e `Usabilidade/src/main/resources/META-INF/persistence.xml`. É recomendado comentar as configurações de produção e descomentar/criar configurações para execução local.

#### Deploy da aplicação
Com este repositório já clonado, abra um terminal na pasta Usabilidade. Inicie o servidor Tomcat com o comando
```
catalina start
```
e aguarde a inicialização.

> **⚠ IMPORTANTE:** Permita completamente o Firewall caso peça permissão ao iniciar o servidor.

Em seguida, execute o comando
```
mvn package clean tomcat7:deploy
```
para iniciar o deploy da aplicação. A primeira execução leva um tempo maior devido ao download das dependências.

> **Obs.:** Caso precise pular a execução dos testes, execute o comando acima com a flag `-DskipTests`

Se todos os passos estiverem OK, o deploy será feito com sucesso, exibindo um resultado `BUILD SUCCESS`. Quando fizer alterações, para poder aplicá-las na aplicação implantada, execute
```
mvn tomcat7:redeploy
```
Para parar o servidor Tomcat em execução, execute
```
catalina stop
```
no terminal e aguarde.

#### Problemas comuns e possíveis soluções
Às vezes, ao fazer um redeploy, a aplicação poderá retornar status 404. Para isso, pare a execução do servidor executando o comando
```
catalina stop
```
e o reinicie com o comando
```
catalina start
```
Caso a versão que deseja implantar ainda não tenha ido para o servidor, abra o Tomcat Manager no endereço `localhost:8080/manager/html`, digite o usuário e senha configurados anteriormente (sugerido como `admin` e `admin`) e na aplicação de caminho `/UseSkill` clique em `Undeploy`. Assim a aplicação poderá ser implantada novamente com o comando
```
mvn package clean tomcat7:deploy
```

#### Alternativa ao Tomcat: Jetty
Com o Maven e o banco de dados MySQL configurados, você pode usar o Jetty como uma alternativa ao deploy via Tomcat. Para isso, use o comando:

```
mvn jetty:run
```

O servidor irá iniciar em alguns segundos e ficará disponível na URL `localhost:8080/`.

### UseSkill Capture
Ferramenta auxiliar para gerenciar ações capturadas construída em Node.js.

#### Requisitos
* [Node.js](https://nodejs.org/en/)


#### Configuração para o ambiente de desenvolvimento
Em `Usabilidade/useskill-capture/app.js` atribua à variável `subdomain` uma string vazia.

```javascript
subdomain = '';
```

No caminho `Usabilidade/useskill-capture/models/index.js`, comente o trecho correspondente ao banco de dados de produção e descomente o de testes.

```javascript
...
, sequelize = new Sequelize('usabilidade_testdatamining', 'root', 'root', {
/*, sequelize = new Sequelize('useskill_datamining', 'root', 'root', {
  logging: false
}) */
...
```
Coloque nome do banco, usuário e senha correspondentes ao banco de dados configurado localmente.

#### Instalação de dependências e execução
Dentro do diretório `Usabilidade/useskill-capture/` digite no terminal o comando

```
npm install
```

e aguarde o processo de instalação das dependências. Em seguida, execute o comando

```
npm start
```
e após alguns segundos, a aplicação deverá estar em execução em `localhost:3000`, caso tenha deixado o `subdomain` vazio como sugerido anteriormente.