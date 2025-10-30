package main;

/**
 *
 * @author arthur
 */

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.List;

public class ClienteAdministrador extends JFrame {
    private JTextField txtAtividade;
    private JButton btnAdicionar, btnSortear, btnImportar, btnAtualizar, btnIniciar, btnRemoverTodas;
    private JTable tabelaAtividades, tabelaUsuarios;
    private DefaultTableModel modeloAtividades, modeloUsuarios;
    private JLabel lblStatus, lblUsuariosConectados, lblBatataAtual;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    public ClienteAdministrador() {
        setTitle("Batata Quente - Administrador");
        setSize(800, 600);
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
        lblUsuariosConectados = new JLabel("Usuários conectados: 0", SwingConstants.CENTER);
        lblBatataAtual = new JLabel("Batata com: ---", SwingConstants.CENTER);
        lblBatataAtual.setFont(new Font("Arial", Font.BOLD, 12));
        lblBatataAtual.setForeground(new Color(255, 140, 0));
        painelStatus.add(lblStatus);
        painelStatus.add(lblUsuariosConectados);
        painelStatus.add(lblBatataAtual);
        add(painelStatus, BorderLayout.NORTH);
        
        // Painel central - dividido em duas partes
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        // Painel de atividades
        JPanel painelAtividades = new JPanel(new BorderLayout(5, 5));
        painelAtividades.setBorder(BorderFactory.createTitledBorder("Cadastro de Atividades/Micos"));
        
        JPanel painelCadastro = new JPanel(new BorderLayout(5, 5));
        painelCadastro.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        txtAtividade = new JTextField();
        btnAdicionar = new JButton("Adicionar");
        btnAdicionar.addActionListener(e -> adicionarAtividade());
        
        btnImportar = new JButton("Importar CSV");
        btnImportar.addActionListener(e -> {
            try {
                importarAtividades();
            } catch (InterruptedException ex) {
                System.getLogger(ClienteAdministrador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        });
        
        JPanel painelBotoesAtividade = new JPanel(new GridLayout(1, 2, 5, 5));
        painelBotoesAtividade.add(btnAdicionar);
        painelBotoesAtividade.add(btnImportar);
        
        painelCadastro.add(new JLabel("Atividade/Mico:"), BorderLayout.WEST);
        painelCadastro.add(txtAtividade, BorderLayout.CENTER);
        painelCadastro.add(painelBotoesAtividade, BorderLayout.EAST);
        
        modeloAtividades = new DefaultTableModel(new String[]{"Atividades/Micos Cadastrados"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaAtividades = new JTable(modeloAtividades);
        JScrollPane scrollAtividades = new JScrollPane(tabelaAtividades);
        
        btnRemoverTodas = new JButton("🗑️ Remover Todas as Atividades");
        btnRemoverTodas.setBackground(new Color(220, 53, 69));
        btnRemoverTodas.setForeground(Color.WHITE);
        btnRemoverTodas.addActionListener(e -> removerTodasAtividades());
        JPanel painelBotaoRemover = new JPanel();
        painelBotaoRemover.add(btnRemoverTodas);
        
        painelAtividades.add(painelCadastro, BorderLayout.NORTH);
        painelAtividades.add(scrollAtividades, BorderLayout.CENTER);
        painelAtividades.add(painelBotaoRemover, BorderLayout.SOUTH);
        
        // Painel de usuários
        JPanel painelUsuarios = new JPanel(new BorderLayout(5, 5));
        painelUsuarios.setBorder(BorderFactory.createTitledBorder("Usuários Conectados"));
        
        modeloUsuarios = new DefaultTableModel(new String[]{"Nome do Usuário"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaUsuarios = new JTable(modeloUsuarios);
        JScrollPane scrollUsuarios = new JScrollPane(tabelaUsuarios);
        
        btnAtualizar = new JButton("🔄 Atualizar Lista");
        btnAtualizar.addActionListener(e -> atualizarListas());
        JPanel painelBotaoAtualizar = new JPanel();
        painelBotaoAtualizar.add(btnAtualizar);
        
        painelUsuarios.add(scrollUsuarios, BorderLayout.CENTER);
        painelUsuarios.add(painelBotaoAtualizar, BorderLayout.SOUTH);
        
        splitPane.setTopComponent(painelAtividades);
        splitPane.setBottomComponent(painelUsuarios);
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);
        
        // Painel inferior - botões
        JPanel painelBotoes = new JPanel(new GridLayout(1, 2, 10, 10));
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        btnIniciar = new JButton("▶ INICIAR JOGO");
        btnIniciar.setFont(new Font("Arial", Font.BOLD, 16));
        btnIniciar.setBackground(new Color(34, 139, 34));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.addActionListener(e -> iniciarJogo());
        
        btnSortear = new JButton("🎯 SORTEAR USUÁRIO");
        btnSortear.setFont(new Font("Arial", Font.BOLD, 16));
        btnSortear.setBackground(new Color(255, 140, 0));
        btnSortear.setForeground(Color.WHITE);
        btnSortear.addActionListener(e -> sortearUsuario());
        
        painelBotoes.add(btnIniciar);
        painelBotoes.add(btnSortear);
        add(painelBotoes, BorderLayout.SOUTH);
    }
    
    private void conectarServidor() {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Identificar como admin
            String nome = JOptionPane.showInputDialog(this, "Digite seu nome de administrador:");
            if (nome == null || nome.trim().isEmpty()) {
                nome = "Admin";
            }
            out.println("ADMIN:" + nome);
            
            String resposta = in.readLine();
            if (resposta.equals("ADMIN_OK")) {
                lblStatus.setText("Status: Conectado ao servidor");
                lblStatus.setForeground(new Color(0, 150, 0));
                
                // Thread para receber mensagens
                new Thread(() -> receberMensagens()).start();
                
                // Atualizar listas
                atualizarListas();
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
        
        if (mensagem.equals("ATIVIDADE_ADICIONADA")) {
            SwingUtilities.invokeLater(() -> {
                txtAtividade.setText("");
                atualizarListas();
            });
            
        } else if (mensagem.startsWith("LISTA_ATIVIDADES:")) {
            String[] atividades = mensagem.substring(17).split("\\|");
            SwingUtilities.invokeLater(() -> {
                modeloAtividades.setRowCount(0);
                for (String ativ : atividades) {
                    if (!ativ.isEmpty()) {
                        modeloAtividades.addRow(new Object[]{ativ});
                    }
                }
            });
            
        } else if (mensagem.startsWith("LISTA_USUARIOS:")) {
            String usuariosStr = mensagem.substring(15);
            String[] usuarios = usuariosStr.split(",");
            SwingUtilities.invokeLater(() -> {
                modeloUsuarios.setRowCount(0);
                int count = 0;
                for (String user : usuarios) {
                    if (!user.isEmpty()) {
                        modeloUsuarios.addRow(new Object[]{user});
                        count++;
                    }
                }
                lblUsuariosConectados.setText("Usuários conectados: " + count);
            });
            
        } else if (mensagem.startsWith("SORTEIO_REALIZADO:")) {
            String[] info = mensagem.substring(18).split(":");
            String usuario = info[0];
            String atividade = info[1];
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                    "Usuário sorteado: " + usuario + "\nAtividade: " + atividade,
                    "Sorteio Realizado",
                    JOptionPane.INFORMATION_MESSAGE);
                lblBatataAtual.setText("Batata com: ---");
                atualizarListas();
            });
            
        } else if (mensagem.startsWith("JOGO_INICIADO:")) {
            String usuario = mensagem.substring(14);
            SwingUtilities.invokeLater(() -> {
                lblBatataAtual.setText("🥔 Batata com: " + usuario);
                JOptionPane.showMessageDialog(this,
                    "Jogo iniciado!\nA batata começou com: " + usuario,
                    "Jogo Iniciado",
                    JOptionPane.INFORMATION_MESSAGE);
            });
            
        } else if (mensagem.startsWith("ERRO:")) {
            String erro = mensagem.substring(5);
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(this, erro, "Erro", JOptionPane.ERROR_MESSAGE));
        }
    }
    
    private void adicionarAtividade() {
        String atividade = txtAtividade.getText().trim();
        if (atividade.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Digite uma atividade ou mico!", 
                "Campo vazio", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        out.println("ADD_ATIVIDADE:" + atividade);
    }
    
    private void sortearUsuario() {
        out.println("SORTEAR");
    }
    
    private void iniciarJogo() {
        int resposta = JOptionPane.showConfirmDialog(this,
            "Iniciar o jogo?\nA batata começará com o primeiro usuário conectado.",
            "Confirmar Início",
            JOptionPane.YES_NO_OPTION);
        
        if (resposta == JOptionPane.YES_OPTION) {
            out.println("INICIAR_JOGO");
        }
    }
    
    private void removerTodasAtividades() {
        if (modeloAtividades.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Não há atividades para remover.",
                "Lista Vazia",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int resposta = JOptionPane.showConfirmDialog(this,
            "Tem certeza que deseja remover TODAS as atividades?\nEsta ação não pode ser desfeita!",
            "Confirmar Remoção",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (resposta == JOptionPane.YES_OPTION) {
            System.out.println("Enviando comando: REMOVER_TODAS_ATIVIDADES");
            out.println("REMOVER_TODAS_ATIVIDADES");
            out.flush(); // Garante que a mensagem seja enviada imediatamente
            modeloAtividades.getDataVector().removeAllElements();
            modeloAtividades.fireTableDataChanged();
            //tabelaAtividades.removeRowSelectionInterval(0, tabelaAtividades.getRowCount());
            
        }
    }
    
    private void atualizarListas() {
        out.println("LISTAR_ATIVIDADES");
        out.println("LISTAR_USUARIOS");
    }
    
    private void importarAtividades() throws InterruptedException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione o arquivo CSV com as atividades");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Arquivos CSV (*.csv)", "csv");
        fileChooser.setFileFilter(filter);
        
        int resultado = fileChooser.showOpenDialog(this);
        
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            try {
                importarCSV(arquivo);
                JOptionPane.showMessageDialog(this,
                    "Atividades importadas com sucesso!",
                    "Importação Concluída",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao importar arquivo: " + e.getMessage(),
                    "Erro de Importação",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void importarCSV(File arquivo) throws IOException, InterruptedException {
        List<String> linhas = Files.readAllLines(arquivo.toPath());
        int importadas = 0;
        
        for (String linha : linhas) {
            linha = linha.trim();
            
            // Ignora linhas vazias e cabeçalhos comuns
            if (linha.isEmpty() || 
                linha.equalsIgnoreCase("atividade") || 
                linha.equalsIgnoreCase("atividades") ||
                linha.equalsIgnoreCase("mico") ||
                linha.equalsIgnoreCase("micos")) {
                continue;
            }
            
            // Remove aspas se houver
            linha = linha.replaceAll("^\"|\"$", "");
            
            // Envia para o servidor
            out.println("ADD_ATIVIDADE:" + linha);
            importadas++;
            
            // Pequena pausa para não sobrecarregar o servidor
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Atualiza a lista após importação
        Thread.sleep(200);
        atualizarListas();
        
        System.out.println("Importadas " + importadas + " atividades");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClienteAdministrador().setVisible(true);
        });
    }
}