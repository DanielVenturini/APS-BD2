/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openAndCloseFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;



/**
 *
 * @author gabriel
 */

// classe responsavel por abrir e fechar o arquivo aonde está o idoperacao do ultimo registro recuperado do banco de dados
public class OpenAndCloseFile {
    File f = new File("ultimo.txt");
    
    
    // função aonde se le o arquivo e retorna o idoperacao
    public String readFile() throws FileNotFoundException{
            Scanner arq = new Scanner(f);
            
             String a = arq.nextLine();
             arq.close();
             return a;
    }
    
    // função que gravao o idOp no arquivo, onde idOp é o idoperacao do ultimo registro lido do banco de dados
    public void writheFile(String idOp) throws IOException{
        FileWriter fi = new FileWriter(f, false);   // o argumento para o construtor é false porque nao é acrescentado ao arquivo, mas sim substituido
        fi.write(idOp);
        fi.close();
    }
    
    
}
