/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO.connection;

import constran.Operation;
import constran.Schedule;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import openAndCloseFile.OpenAndCloseFile;

/**
 *
 * @author gabriel
 */
public class ScheduleDAO {
     private static Connection con; 
     private static Statement comando;
     private static OpenAndCloseFile oc;
     
     // faço a conecção passando os parâmetros necessários para tal operação
     private static void  Connect(String database,String user, String password) throws SQLException, ClassNotFoundException{
       ConnectionFactory fact = new ConnectionPsql(database,user,password);
       con = fact.getConection();
       comando = con.createStatement();
       oc = new OpenAndCloseFile();
       //System.out.println(fact.statusConnection()); 
   }
     
     /* função para buscar 30 registro do banco de dados. Recupera os 30 e depois grava o ioperacao do ultimo.
      essa função não apaga os registros que já foram lidos. Apenas guarda o idoperacao do ultimo para depois pegar mais 
      30 registros a partir daquele*/
     public static  ArrayList<Operation>  SearchAllAfterId() throws SQLException, FileNotFoundException, IOException, ClassNotFoundException{
        Connect("jensen2", "postgres", "root");
        
        ArrayList<Operation> temp = new ArrayList<>(); // Lista que será guardado os registros. Cada registro é do tipo Operation

         String idOp = oc.readFile();   // lendo do arquivo o idoperacao do ultimo registro lido. Por padrão, idOp seria 0 para a primeira operação

         ResultSet rs = comando.executeQuery("Select * FROM schedule s WHERE s.idoperacao > "+"'"+idOp+"'");    // recuperando registro com idoperacao maior do que o ultimo registro lido, visto que os idoperacao estão sempre em sequencia.
            while(rs.next()){
                Operation op = new Operation();
                op.setIdOp(rs.getString("idoperacao"));
                op.setIndice(rs.getString("indicetransacao"));
                op.setItemDado(rs.getString("itemdado"));
                op.setOp(rs.getString("operacao"));
                op.setTimeStamp(rs.getString("timestampj"));
                temp.add(op);  
            }
            
            //caso acabe a produção ele volta ao inicio e escalona desde o inicio novamente
        if(temp.size() > 0 && temp.get((temp.size() -1)) != null)
            oc.writheFile( temp.get((temp.size() -1)).getIdOp() );// gravando no arquivo o idoperacao.
        else{
             System.out.println("execute mais uma vez para voltar ao inicio!");
             oc.writheFile("0");
        }

        con.close();
         return temp;
     }
}
