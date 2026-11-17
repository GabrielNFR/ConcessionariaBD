package br.cesar.bd.concessionaria.main;

import javax.swing.SwingUtilities;
import br.cesar.bd.concessionaria.controller.AppController;
import br.cesar.bd.concessionaria.util.ConnectionFactory;

public class Exec {
    public static void main(String[] args) {
    	SwingUtilities.invokeLater(() -> new AppController().iniciar());
    }
}