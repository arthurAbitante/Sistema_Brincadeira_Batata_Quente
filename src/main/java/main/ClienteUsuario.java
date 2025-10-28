/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

/**
 *
 * @author arthur
 */
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class ClienteUsuario extends JFrame {
    private JLabel lblStatus, lblNome, lblUsuariosConectados;
    private JPanel painelAguardando, painelBatata, painelSorteado, painelCentral;
    private CardLayout cardLayout;
    private JLabel lblImagemBatata;
    private JButton btnPassar, btnConcluido;
    private JTextArea txtAtividade;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nomeUsuario;
    
    public ClienteUsuario() {
        setTitle("Batata Quente - Participante");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        conectarServidor();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel superior - Status
        JPanel painelStatus = new JPanel(new GridLayout(3, 1));
        painelStatus.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        lblStatus = new JLabel("Status: Desconectado", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        
        lblNome = new JLabel("", SwingConstants.CENTER);
        lblNome.setFont(new Font("Arial", Font.PLAIN, 12));
        
        lblUsuariosConectados = new JLabel("Usuários conectados: 0", SwingConstants.CENTER);
        lblUsuariosConectados.setFont(new Font("Arial", Font.PLAIN, 12));
        
        painelStatus.add(lblStatus);
        painelStatus.add(lblNome);
        painelStatus.add(lblUsuariosConectados);
        add(painelStatus, BorderLayout.NORTH);
        
        // Painel central - card layout para alternar entre estados
        cardLayout = new CardLayout();
        painelCentral = new JPanel(cardLayout);
        
        // Card 1: Aguardando jogo iniciar
        painelAguardando = new JPanel(new BorderLayout());
        painelAguardando.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblAguardando = new JLabel("Aguardando o jogo iniciar...", SwingConstants.CENTER);
        lblAguardando.setFont(new Font("Arial", Font.BOLD, 24));
        
        JLabel lblIcone = new JLabel("⏳", SwingConstants.CENTER);
        lblIcone.setFont(new Font("Arial", Font.PLAIN, 80));
        
        JPanel painelAguardandoConteudo = new JPanel(new GridLayout(2, 1, 10, 10));
        painelAguardandoConteudo.add(lblIcone);
        painelAguardandoConteudo.add(lblAguardando);
        
        painelAguardando.add(painelAguardandoConteudo, BorderLayout.CENTER);
        
        // Card 2: Com a batata
        painelBatata = new JPanel(new BorderLayout(10, 10));
        painelBatata.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblTituloBatata = new JLabel("🔥 VOCÊ ESTÁ COM A BATATA QUENTE! 🔥", SwingConstants.CENTER);
        lblTituloBatata.setFont(new Font("Arial", Font.BOLD, 20));
        lblTituloBatata.setForeground(new Color(220, 20, 20));
        
        // Carrega imagem da batata (usando texto temporariamente)
        lblImagemBatata = new JLabel();
        lblImagemBatata.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagemBatata.setPreferredSize(new Dimension(300, 200));
        
        // Tenta carregar imagem da URL ou usa emoji
        try {
            // URL da imagem fornecida
            URL url = new URL("https://cdn.pixabay.com/photo/2016/08/11/08/04/vegetables-1584999_960_720.png");
            BufferedImage img = ImageIO.read(url);
            Image scaledImg = img.getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            lblImagemBatata.setIcon(new ImageIcon(scaledImg));
        } catch (Exception e) {
            // Se falhar, usa emoji grande
            lblImagemBatata.setText("🥔");
            lblImagemBatata.setFont(new Font("Arial", Font.PLAIN, 150));
        }
        
        btnPassar = new JButton("👉 PASSAR A BATATA");
        btnPassar.setFont(new Font("Arial", Font.BOLD, 18));
        btnPassar.setBackground(new Color(255, 140, 0));
        btnPassar.setForeground(Color.WHITE);
        btnPassar.setPreferredSize(new Dimension(200, 60));
        btnPassar.addActionListener(e -> passarBatata());
        
        JPanel painelBotaoBatata = new JPanel();
        painelBotaoBatata.add(btnPassar);
        
        painelBatata.add(lblTituloBatata, BorderLayout.NORTH);
        painelBatata.add(lblImagemBatata, BorderLayout.CENTER);
        painelBatata.add(painelBotaoBatata, BorderLayout.SOUTH);
        
        // Card 3: Sorteado
        painelSorteado = new JPanel(new BorderLayout(10, 10));
        painelSorteado.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblTituloSorteado = new JLabel("💥 VOCÊ FOI SORTEADO! 💥", SwingConstants.CENTER);
        lblTituloSorteado.setFont(new Font("Arial", Font.BOLD, 22));
        lblTituloSorteado.setForeground(new Color(220, 20, 20));
        
        JLabel lblSubtitulo = new JLabel("Sua atividade/mico:", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtAtividade = new JTextArea();
        txtAtividade.setEditable(false);
        txtAtividade.setLineWrap(true);
        txtAtividade.setWrapStyleWord(true);
        txtAtividade.setFont(new Font("Arial", Font.BOLD, 16));
        txtAtividade.setBackground(new Color(255, 255, 200));
        txtAtividade.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.ORANGE, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JScrollPane scrollAtividade = new JScrollPane(txtAtividade);
        
        btnConcluido = new JButton("✓ ATIVIDADE CONCLUÍDA");
        btnConcluido.setFont(new Font("Arial", Font.BOLD, 16));
        btnConcluido.setBackground(new Color(34, 139, 34));
        btnConcluido.setForeground(Color.WHITE);
        btnConcluido.setPreferredSize(new Dimension(200, 50));
        btnConcluido.addActionListener(e -> marcarConcluido());
        
        JPanel painelBotaoConcluido = new JPanel();
        painelBotaoConcluido.add(btnConcluido);
        
        JPanel painelTitulos = new JPanel(new GridLayout(2, 1));
        painelTitulos.add(lblTituloSorteado);
        painelTitulos.add(lblSubtitulo);
        
        painelSorteado.add(painelTitulos, BorderLayout.NORTH);
        painelSorteado.add(scrollAtividade, BorderLayout.CENTER);
        painelSorteado.add(painelBotaoConcluido, BorderLayout.SOUTH);
        
        // Adicionar cards
        painelCentral.add(painelAguardando, "AGUARDANDO");
        painelCentral.add(painelBatata, "BATATA");
        painelCentral.add(painelSorteado, "SORTEADO");
        
        add(painelCentral, BorderLayout.CENTER);
    }
    
    private void conectarServidor() {
        try {
            nomeUsuario = JOptionPane.showInputDialog(this, "Digite seu nome:");
            if (nomeUsuario == null || nomeUsuario.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome é obrigatório!");
                System.exit(0);
            }
            
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Identificar como usuário
            out.println("USER:" + nomeUsuario);
            
            String resposta = in.readLine();
            if (resposta.equals("CONECTADO")) {
                lblStatus.setText("Status: Conectado ao servidor");
                lblStatus.setForeground(new Color(0, 150, 0));
                lblNome.setText("Participante: " + nomeUsuario);
                
                // Thread para receber mensagens
                new Thread(() -> receberMensagens()).start();
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao conectar ao servidor: " + e.getMessage(), 
                "Erro de Conexão", 
                JOptionPane.ERROR_MESSAGE);
            lblStatus.setText("Status: Erro de conexão");
            lblStatus.setForeground(Color.RED);
        }
    }
    
    private void receberMensagens() {
        try {
            String mensagem;
            while ((mensagem = in.readLine()) != null) {
                processarMensagem(mensagem);
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                lblStatus.setText("Status: Desconectado");
                lblStatus.setForeground(Color.RED);
            });
        }
    }
    
    private void processarMensagem(String mensagem) {
        System.out.println("Mensagem recebida: " + mensagem);
        
        if (mensagem.equals("RECEBER_BATATA")) {
            SwingUtilities.invokeLater(() -> {
                cardLayout.show(painelCentral, "BATATA");
                Toolkit.getDefaultToolkit().beep();
                setTitle("Batata Quente - VOCÊ ESTÁ COM A BATATA!");
            });
            
        } else if (mensagem.equals("REMOVER_BATATA")) {
            SwingUtilities.invokeLater(() -> {
                cardLayout.show(painelCentral, "AGUARDANDO");
                setTitle("Batata Quente - Participante");
            });
            
        } else if (mensagem.startsWith("SORTEADO:")) {
            String atividade = mensagem.substring(9);
            SwingUtilities.invokeLater(() -> {
                txtAtividade.setText(atividade);
                cardLayout.show(painelCentral, "SORTEADO");
                
                Toolkit.getDefaultToolkit().beep();
                
                JOptionPane.showMessageDialog(this,
                    "VOCÊ FOI SORTEADO!\n\nRealize a atividade e clique em 'Concluído'",
                    "Sorteio!",
                    JOptionPane.WARNING_MESSAGE);
            });
            
        } else if (mensagem.startsWith("ATUALIZAR_USUARIOS:")) {
            String quantidade = mensagem.substring(19);
            SwingUtilities.invokeLater(() -> 
                lblUsuariosConectados.setText("Usuários conectados: " + quantidade));
            
        } else if (mensagem.equals("DESCONECTADO")) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                    "Atividade concluída!\nVocê será desconectado.",
                    "Até logo!",
                    JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            });
        }
    }
    
    private void passarBatata() {
        out.println("PASSAR_BATATA");
    }
    
    private void marcarConcluido() {
        int resposta = JOptionPane.showConfirmDialog(this,
            "Confirma que a atividade foi concluída?\nVocê será desconectado do servidor.",
            "Confirmar",
            JOptionPane.YES_NO_OPTION);
        
        if (resposta == JOptionPane.YES_OPTION) {
            out.println("CONCLUIDO");
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClienteUsuario().setVisible(true);
        });
    }
}