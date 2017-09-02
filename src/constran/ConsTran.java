/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package constran;

import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author gabriel
 */
public class ConsTran {
        
    /**
     * @param args the command line arguments
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {

            Schedule.readSchedule();

    }
    
}
