/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package constran;

/**
 *
 * @author gabriel
 */

// classe responsavel por guardar os atributos do registro.
public class Operation {

    private String idOp ;
    private String indice;
    private String op;
    private String itemDado;
    private String timeStamp;

    public String getIdOp() {
        return idOp;
    }

    public void setIdOp(String idOp) {
        this.idOp = idOp;
    }

    public String getIndice() {
        return indice;
    }

    public void setIndice(String indice) {
        this.indice = indice;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getItemDado() {
        return itemDado;
    }

    public void setItemDado(String itemDado) {
        this.itemDado = itemDado;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    @Override
    public String toString(){
        return op+indice+"("+ itemDado +"); ";
    }
 
}
