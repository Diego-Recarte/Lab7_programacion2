/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_progra2;

/**
 *
 * @author denam
 */
import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.swing.text.*;
public class Pantalla extends JFrame{
    private CardLayout cardLayout;
    private JPanel panelCards;
    private Editor editor;
    private WordNuevo nuevo;
    private WordGuardarComo guardarComo;
    public Pantalla  (){
        super("Word");


        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
      setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        setLocationRelativeTo(null);
        InitCardLayout();
        agregarCards();
        
        

        
        mostrarCard("nuevo");
        
        
        
        setVisible(true);
        
       

    }
     public void agregarCards(){
       editor = new Editor(this, cardLayout, panelCards);
        agregarCard(editor, "editor");
        
        nuevo = new WordNuevo (this, cardLayout, panelCards, editor);
        agregarCard(nuevo, "nuevo");
        
         guardarComo= new WordGuardarComo(this, cardLayout, panelCards,  editor);
         agregarCard(guardarComo, "guardarComo");
        
        
        
        
        

        
    }
    
    public void InitCardLayout(){
    cardLayout = new CardLayout(); 
    panelCards = new JPanel(cardLayout); 
    
    
    panelCards.setOpaque(false);
     
 
    getContentPane().add(panelCards, BorderLayout.CENTER);
    
    



    }
    private void agregarCard(JPanel panel, String nombre) {
        panelCards.add(panel, nombre);
    }
    public void mostrarCard(String nombreCard) { 
        cardLayout.show(panelCards, nombreCard); 
        panelCards.revalidate();
        panelCards.repaint(); 
    }
    
    
    public void cambiarGuardar(){
        if (editor.IsExistente){
            guardarComo.Guardar.setVisible(true);
            nuevo.Guardar.setVisible(true);
        }else{
            guardarComo.Guardar.setVisible(false);
            nuevo.Guardar.setVisible(false);
        }
    }
}

