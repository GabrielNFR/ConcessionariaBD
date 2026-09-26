package br.cesar.bd.concessionaria.main;

import javax.swing.SwingUtilities;
import br.cesar.bd.concessionaria.controller.AppController;

public class Exec {
    public static void main(String[] args) {
    	SwingUtilities.invokeLater(() -> new AppController().iniciar());
    }
}