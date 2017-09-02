/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package constran;

import DAO.connection.ScheduleDAO;
import DAO.connection.SteppedDAO;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 *
 * @author gabriel
 */
public class Schedule {
    
    //variavel para controle de operacoes no banco de dados
    static boolean have = true;

    //lista de operações do produtor ainda nao escalonada
    static ArrayList<Operation> listOperation = new ArrayList<>();

    //controle de qual dado esta bloqueado para escrita e para leitura e quais transações estao bloqueadas
    static ArrayList<String> lockedForWrite = new ArrayList<>();
    static ArrayList<String> lockedForRead = new ArrayList<>();
    static ArrayList<String> listOperationsBlocked = new ArrayList<>();

    //hash onde se encontra as operações e os respectivos dados que ela bloqueou
    static HashMap<String,ArrayList<String>> hash = new HashMap<>();

    //lista onde estara o time do inicio de cada transação para tratar o deadlock
    static HashMap<String,Long> hashDeadLock = new HashMap();

    //adiciona na hash os dados das operações, se for um begin ele só coloca o indice da op como chave e deixa uma lista vazia la
    private static void addHash(Operation e){

        ArrayList<String> s;
        if(e.getOp().equals("B")){
            s = new ArrayList<>();
            hash.put(e.getIndice(),s);
            return;
        }
            s = hash.get(e.getIndice());
            s.add(e.getItemDado());
    }

    // remove das listas de bloqueados os dados de uma determinada transação quando for um end/commit
    private static void removeHashAndAll(Operation e){

        ArrayList<String> s = hash.get(e.getIndice());
        for(String itemDado : s){
            if(lockedForRead.contains(itemDado))
                lockedForRead.remove(itemDado);

            if(lockedForWrite.contains(itemDado))
                lockedForWrite.remove(itemDado);
        }

        hash.remove(e.getIndice());
    }
    
    private static boolean treatDeadLock() throws SQLException, IOException, FileNotFoundException, ClassNotFoundException{

        Long tempAt = Long.valueOf(new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(Calendar.getInstance().getTime()).split("_")[1]);//pego o tempo atual

        Long k ; //auxiliar
        ArrayList<Operation> tempdead = new ArrayList<>();

        for(String indice : listOperationsBlocked) {
            k = hashDeadLock.get(indice); //pego o tempo da transação desta operação

            Operation e = new Operation();
            e.setIndice(indice);
           if(tempAt-k > 500){

               ArrayList<Operation> s = SteppedDAO.getOperationsInDeadLock(e);
                    for(int i=0; i< listOperation.size();i++){
                            if(listOperation.get(i).getIndice().equals(e.getIndice())){
                                    s.add(listOperation.get(i));
                                    tempdead.add(listOperation.get(i));
                            }
                           
                    }

            removeHashAndAll(e);
            for(Operation y : tempdead){
                listOperation.remove(y);
            }

            if(listOperationsBlocked.contains(e.getIndice()))
                listOperationsBlocked.remove(e.getIndice());

            listOperation.addAll(s);
            return true;
           }
        }
           return false;
    }
    
    private static boolean canExecute(Operation e){         // função que ira determinar se a operação poderá executar

        switch(e.getOp()){

            case "E" : 
                return !listOperationsBlocked.contains(e.getIndice());  // se a operaçao nao estiver na lista de bloqueio, entao pode executar

            case "R" :
                if(!listOperationsBlocked.contains(e.getIndice())){     //se a transacao nao estiver bloqueada, entra no if

                    if(lockedForWrite.contains(e.getItemDado())){       // se o dado esta sendo escrito por alguma transacao
                        
                        /* o dado está bloqueado, mas a propria transacao pode estar bloqueando.
                         *   por isso tem que ver se o dado não está bloqueado pela mesma
                         */

                        ArrayList<String> s = hash.get(e.getIndice());  // recuperando os dados bloqueados pela transacao
                        if(s.contains(e.getItemDado())){                // se a transacao esta bloqueando o dado, nao tem problema
                            return true;                                // entao pode executar
                        }

                        listOperationsBlocked.add(e.getIndice());       // transacao nao esta bloqueada. Mas dado bloquado para escrita, e nao é a mesma transacao que está bloqueando. Entao adiciona a lista de bloqueados
                        return false;                                   // retorna false, pois nao pode acessar o dado
                    }

                    /* a transação nao esta bloqueada e o dado não está bloqueado para escrita.
                     * então pode executar a leitura e a transação.
                     */
                    ArrayList<String> s = hash.get(e.getIndice());  // recuperando os dados bloqueados pela transacao
                    if(s.contains(e.getItemDado())){                // se ja tem o dado bloqueado para leitura retorna verdadeiro
                            return true;
                    }
                    lockedForRead.add(e.getItemDado());                 // bloqueio o dado para leitura
                    addHash(e);                                         // adiciono a transação que ela bloqueou aquele dado
                    return true;                                        // transacao nao esta bloqueada e dado nao esta sendo escrito, então pode executar

                } else {    // a transação está na lista de bloqueadas

                    if(!lockedForWrite.contains(e.getItemDado())){      // transacao bloqueada, mas o dado pode ter sido liberado.

                        /*é necessário esse "for", porque se uma transação está bloqueada, mas na lista de operações,
                         * uma operação que a transação está bloqueada, mas está tentando acessar um dado não bloqueado
                         * é preciso ver se não tem nenhuma operação da mesma transação antes dela esperando. Se houver, não pode executar
                         */

                        int i = 0;
                        for(Operation ant = listOperation.get(i); ant.getIndice() != e.getIndice(); ) {
                            if(ant.getIndice().equals(e.getIndice()))
                                return false;

                            ant = listOperation.get(++i);
                        }

                        // a transação está bloqueada, mas o dado já foi liberado. Então tira a transação da lista de bloqueio e bloqueia o dado
                        addHash(e);                                     // concede o acesso ao dado a transacao
//                      System.out.println("desbloqueado leitura: " + e);
                        listOperationsBlocked.remove(e.getIndice());    // o dado nao esta sendo escrito. Entao remove da lista de transacoes bloqueadas
                        lockedForRead.add(e.getItemDado());             // bloqueio o dado para leitura
                        return true;                                    // e pode executar
                    }

                    return false;       // transação está bloqueada e dado está sendo escrito por outra transação. Então nao pode executar
                }

                /* A operação de WRITE é analoga a anterior, por isso não vou comentar igual comentei ali em cima, com tantos detalhes */

            case "W" : 
                 if(!listOperationsBlocked.contains(e.getIndice())){    // a transacao nao está bloqueada

                    if(lockedForWrite.contains(e.getItemDado()) || lockedForRead.contains(e.getItemDado())){    // verifica se o dado esta bloqueado para leitura ou para escrita. Se houver um dos dois bloqueios, não será possivel executar. Se mais de uma transacao estiver bloqueando, tambem não sera possivel executar.

                        ArrayList<String> s = hash.get(e.getIndice());  // porem quem está bloqueando pode ser a propria transacao.
                        if(s.contains(e.getItemDado()) && lockedForWrite.contains(e.getItemDado())) { //se ele estiver bloqueado para escrita pela transação
                            return true;
                        }
                        if(s.contains(e.getItemDado()) &&  (lockedForRead.indexOf(e.getItemDado())) == lockedForRead.lastIndexOf(e.getItemDado())){ // se ele estiver bloqueado para leitura apenas por essa transação
                                lockedForRead.remove(e.getItemDado()); // removo da lista de leitura
                                lockedForWrite.add(e.getItemDado()); // adiciona na lista de escrita
                                return true;
                        
                        }
                        listOperationsBlocked.add(e.getIndice());   // a transacao não pode continuar. O dado está bloqueado e não é pela transacao.
                        return false;
                    }

                    lockedForWrite.add(e.getItemDado());            // bloqueio o dado para escrita
                    addHash(e);                                     // a transação não está bloqueada e o dado tambem não.
                    return true;
                } else {    // a transação está bloqueada
                     
                     /* esse if é dificil de entender. Até aqui, a transação está bloqueada, mas o dado pode ter sido liberado.
                      * Então tenho que ver se o dado já não foi bloqueado por outra transação. E tambem tenho que ver o dado estiver bloqueado pela propria transaçao.
                      * Então tenho que ver se somente a propria transação tem acesso ao dado.
                      */
                    
                        int i = 0;
                        for(Operation ant = listOperation.get(i); ant != e; ) {  // como a transação está na lista de bloqueadas, mas ela pode ter sido liberada
                           if(ant.getIndice().equals(e.getIndice()))                // ela pode estar tentando escrever um dado que não está bloqueado. Mas não pode, porque a transação está bloqueada.
                               return false;                                        // Então tem que ver se não tem mais nenhuma operação do mesmo indice antes dela, pois se tiver, ela não pode executar

                           ant = listOperation.get(++i);
                        }

                    if((!lockedForRead.contains(e.getItemDado()) && !lockedForWrite.contains(e.getItemDado())) || ( hash.get(e.getIndice()).contains(e.getItemDado()) &&  (lockedForRead.indexOf(e.getItemDado()) == lockedForRead.lastIndexOf(e.getItemDado())) )) { 

                        if(!hash.get(e.getIndice()).contains(e.getItemDado())){ //se minha transação nao tiver bloqueado esse dado
                            addHash(e);   // bloqueio o dado para a transacao
                        }

                        if(lockedForRead.contains(e.getItemDado()) && hash.get(e.getIndice()).contains(e.getItemDado())){ //como ja sei que se entrar nesse if ele estara bloqueado apenas para leitura e so por essa transação
                            lockedForRead.remove(e.getItemDado());    // removo da lista de bloqueado para read
                        }

                        lockedForWrite.add(e.getItemDado());       // bloqueio o dado para escrita        
                        listOperationsBlocked.remove(e.getIndice());        // removo a transacao da lista de bloqueadas.
                        return true;
                     }

                return false;   // a transação está bloqueada e o dado tambem.
                }

            default:
                return false; // nunca vai entrar aqui. Somente entrara se os dados forem inseridos errados no banco de dados
        }
    }

    public static void readSchedule() throws SQLException, IOException, FileNotFoundException, ClassNotFoundException{

            ArrayList<Operation> l = ScheduleDAO.SearchAllAfterId();
            if(l.isEmpty())
                return;

            listOperation.addAll(l);// pega sempre todas as operações depois da anterior ja recebida

            Operation e;
            int i = 0;
            for(int pos = 0; listOperation.size() > 0; ){

                pos = pos == listOperation.size() ? 0 : pos;

                e = listOperation.get(pos);

                switch(e.getOp()){
                    case "B" : 
                        System.out.println("0");
                        hashDeadLock.put(e.getIndice(), Long.valueOf(new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(Calendar.getInstance().getTime()).split("_")[1]));
                        addHash(e);
                        SteppedDAO.setOperationStepped(e);
                        listOperation.remove(e);
                        continue;


                    case "R" :
                        if(canExecute(e)){
                            System.out.println("1");
                            listOperation.remove(e);
                            SteppedDAO.setOperationStepped(e);
                            continue;
                        } else
                            pos ++;

                        break;

                    case "W" :  
                        if(canExecute(e)){
                            System.out.println("2");
                            listOperation.remove(e);
                            SteppedDAO.setOperationStepped(e);
                            continue;
                        } else
                            pos ++;
                        
                        break;

                    case "E":
                        if(canExecute(e)){
                            System.out.println("3");
                            pos = 0;
                            removeHashAndAll(e);
                            listOperation.remove(e);
                            SteppedDAO.setOperationStepped(e);
                            continue;
                        } else
                            pos ++;

                        break;

                }
                if(treatDeadLock())
                       pos = 0;

            }
        }
}
