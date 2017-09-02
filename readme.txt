===================================================================
|		APS DE BANCO DE DADOS				  |
===================================================================

O programa ConsTrans implementado na linguagem JAVA, utiliza do banco de dados para a sua execu��o. O seu objetivo � escalonar e executar do banco de dados. Por isso ele n�o para enquanto n�o atingir sua meta.

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

Para conectar ao banco de dados usando o ConsTrans, � necess�rio trocar o usu�rio, senha e a tabela. Para fazer essas opera��es, tem-se que ir pelo seguinte caminho:
	- ConsTran->DAO.connection->ScheduleDAO.java->SearchAllAfterId().
	- ConsTran->DAO.connection->SteppedDAO.java->setOperationStepped().
	- ConsTran->DAO.connection->SteppedDAO.java->getOperationsInDeadLock().

O funcionamento do programa � simples. Ele funciona guardando o idoperacao da ultima opera��o processada. Quando o programa � inicializado, ele tem por padr�o 0 - no caso o idoperacao � guardado em um arquivo - e recupera do banco de dados todas as opera��es que contenham o idoperacao maior do que 0 e assim por diante.

Quando o escalonador termina de executar uma opera��o, ele guarda o resultado na tabela stepped do banco de dados. O ConTrans possui tres fun��es pricinpais para realizar o escalonamento e o processamento das opera��es. Uma pega todas as opera��es e ve qual que �, depois manda para a fun��o que ir� ver o indice da transa��o e o dado que a opera��o est� querendo acessar e vai retornar se aquela opera��o poder� executar. Se n�o, � chamada que, olhando na lista de transa��es bloqueadas, ir� determinar se uma transa��o entrou em deadlock. Quando uma opera��o � B, quer dizer que est� iniciando uma transa��o, ent�o guardamos o horario que a transa��o come�ou, e � subtraido do horario atual, e se passar de um certo tempo, sup�e se que a fun��o entrou em deadlock.
