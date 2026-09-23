package main;

import dao.MarcaDAO;
import dao.ModeloDAO;
import model.Marca;
import model.Modelo;

import java.util.List;

public class Exec {
    public static void main(String[] args) {
        MarcaDAO marcaDAO = new MarcaDAO();
        ModeloDAO modeloDAO = new ModeloDAO();

        try {

            modeloDAO.inserir(new Modelo(0, "Corolla", 1));

            System.out.println("Modelos cadastrados:");
            List<Modelo> modelos = modeloDAO.listar();
            modelos.forEach(m -> System.out.println(m.getId() + " - " + m.getNome()));

            System.out.println("\nMarcas cadastradas:");
            List<Marca> marcas = marcaDAO.listar();
            marcas.forEach(m -> System.out.println(m.getId() + " - " + m.getNome()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
