===================================================================
|		APS DE BANCO DE DADOS				  |
===================================================================

O programa ConsTrans implementado na linguagem JAVA, utiliza do banco de dados para a sua execução. O seu objetivo é escalonar e executar do banco de dados. Por isso ele não para enquanto não atingir sua meta.

Para realizar seu trabalho, ele usa uma segunda tabela para guardar os resultados de sua escalonagem.

Para criar a inferida tabela, use o seguinte comando:

CREATE TABLE stepped(

  idoperacao integer NOT NULL DEFAULT nextval,
  indicetransacao integer,
  operacao character varying(10),
  itemdado character varying(10),
  timestampj character varying(15),
  PRIMARY KEY (idoperacao)
)

Para conectar ao banco de dados usando o ConsTrans, é necessário trocar o usuário, senha e a tabela. Para fazer essas operações, tem-se que ir pelo seguinte caminho:
	- ConsTran->DAO.connection->ScheduleDAO.java->SearchAllAfterId().
	- ConsTran->DAO.connection->SteppedDAO.java->setOperationStepped().
	- ConsTran->DAO.connection->SteppedDAO.java->getOperationsInDeadLock().

O funcionamento do programa é simples. Ele funciona guardando o idoperacao da ultima operação processada. Quando o programa é inicializado, ele tem por padrão 0 - no caso o idoperacao é guardado em um arquivo - e recupera do banco de dados todas as operações que contenham o idoperacao maior do que 0 e assim por diante.

Quando o escalonador termina de executar uma operação, ele guarda o resultado na tabela stepped do banco de dados. O ConTrans possui tres funções pricinpais para realizar o escalonamento e o processamento das operações. Uma pega todas as operações e ve qual que é, depois manda para a função que irá ver o indice da transação e o dado que a operação está querendo acessar e vai retornar se aquela operação poderá executar. Se não, é chamada que, olhando na lista de transações bloqueadas, irá determinar se uma transação entrou em deadlock. Quando uma operação é B, quer dizer que está iniciando uma transação, então guardamos o horario que a transação começou, e é subtraido do horario atual, e se passar de um certo tempo, supõe se que a função entrou em deadlock.
