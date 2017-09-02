/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO.connection;

import constran.Operation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author gabriel
 */ 
public class SteppedDAO {
    private static Connection con; 
     private static Statement comando;
    
     
     // faço a conecção passando os parâmetros necessários para tal operação
     private static void  Connect(String database,String user, String password) throws SQLException, ClassNotFoundException{
       ConnectionFactory fact = new ConnectionPsql(database,user,password);
       con = fact.getConection();
       comando = con.createStatement();
       //System.out.println(fact.statusConnection()); 
   }
     
    public static void setOperationStepped(Operation o) throws SQLException, ClassNotFoundException{

           Connect("jensen2", "postgres", "root");
           String sql = "insert into stepped (indicetransacao, operacao, itemdado, timestampj) VALUES (?, ?, ?, ?)";
           PreparedStatement stm = con.prepareStatement(sql);

                stm.setInt(1,Integer.parseInt(o.getIndice()));

                if(o.getOp().equals("E") || o.getOp().equals("B"))
                    stm.setString(2, o.getOp());
                else
                    stm.setString(2, o.getOp());

                stm.setString(3, o.getItemDado());
                stm.setString(4,o.getTimeStamp());

                stm.executeUpdate();

          con.close();
    }
    // recupero todos as operações que estao causando deadlock e excluo elas do banco
            public static ArrayList<Operation> getOperationsInDeadLock(Operation e) throws SQLException, ClassNotFoundException{
        Connect("jensen2", "postgres", "root");
        ArrayList<Operation> temp = new ArrayList<>(); // Lista que será guardado os registros. Cada registro é do tipo Operation

            String id = e.getIndice();

         ResultSet rs = comando.executeQuery("Select * FROM stepped s WHERE s.indicetransacao = "+""+id+"");    // recuperando registro com idoperacao maior do que o ultimo registro lido, visto que os idoperacao estão sempre em sequencia.
            while(rs.next()){
                Operation op = new Operation();
                op.setIdOp(rs.getString("idoperacao"));
                op.setIndice(rs.getString("indicetransacao"));
                op.setItemDado(rs.getString("itemdado"));
                op.setOp(rs.getString("operacao"));
                op.setTimeStamp(rs.getString("timestampj"));
                temp.add(op);  
            }

         comando.executeUpdate("Delete from stepped s where s.indicetransacao = "+"'"+id+"'");
         
         con.close();
          return temp;
    }
}
