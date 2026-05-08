import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SimuladorSubstituicaoPaginas extends JFrame {

    private JTextField campoPaginas;
    private JTextField campoQuadros;
    private JTextArea areaResultado;
    private PainelGrafico painelGrafico;

    public SimuladorSubstituicaoPaginas() {
        setTitle("Simulador de Substituicao de Paginas");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel painelEntrada = new JPanel(new GridLayout(3, 2, 10, 10));
        painelEntrada.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel labelPaginas = new JLabel("Sequencia de paginas:");
        campoPaginas = new JTextField("7,0,1,2,0,3,0,4,2,3,0,3,2");

        JLabel labelQuadros = new JLabel("Quantidade de quadros:");
        campoQuadros = new JTextField("3");

        JButton botaoSimular = new JButton("Simular");
        JButton botaoLimpar = new JButton("Limpar");

        painelEntrada.add(labelPaginas);
        painelEntrada.add(campoPaginas);
        painelEntrada.add(labelQuadros);
        painelEntrada.add(campoQuadros);
        painelEntrada.add(botaoSimular);
        painelEntrada.add(botaoLimpar);

        areaResultado = new JTextArea();
        areaResultado.setEditable(false);
        areaResultado.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollResultado = new JScrollPane(areaResultado);

        painelGrafico = new PainelGrafico();

        JSplitPane divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollResultado, painelGrafico);
        divisao.setDividerLocation(250);

        add(painelEntrada, BorderLayout.NORTH);
        add(divisao, BorderLayout.CENTER);

        botaoSimular.addActionListener(e -> simular());
        botaoLimpar.addActionListener(e -> limpar());
    }

    private void simular() {
        try {
            int[] paginas = converterPaginas(campoPaginas.getText());
            int capacidade = Integer.parseInt(campoQuadros.getText().trim());

            if (paginas.length == 0) {
                JOptionPane.showMessageDialog(this, "Digite pelo menos uma pagina.");
                return;
            }

            if (capacidade <= 0) {
                JOptionPane.showMessageDialog(this, "A quantidade de quadros deve ser maior que zero.");
                return;
            }

            ResultadoSimulacao resultadoSimulacao = executarSimulacao(paginas, capacidade);

            areaResultado.setText(montarTextoResultado(paginas, capacidade, resultadoSimulacao));

            String[] nomes = {"FIFO", "LRU", "Relogio", "Otimo"};
            int[] valores = {
                    resultadoSimulacao.faltasFIFO,
                    resultadoSimulacao.faltasLRU,
                    resultadoSimulacao.faltasRelogio,
                    resultadoSimulacao.faltasOtimo
            };

            painelGrafico.atualizarDados(nomes, valores);

        } catch (NumberFormatException erro) {
            JOptionPane.showMessageDialog(
                    this,
                    "Entrada invalida. Use apenas numeros inteiros separados por virgula.\nExemplo: 7,0,1,2,0,3"
            );
        }
    }

    private void limpar() {
        campoPaginas.setText("");
        campoQuadros.setText("");
        areaResultado.setText("");
        painelGrafico.limpar();
    }

    private int[] converterPaginas(String texto) {
        texto = texto.trim();

        if (texto.isEmpty()) {
            return new int[0];
        }

        String[] partes = texto.split(",");
        int[] paginas = new int[partes.length];

        for (int i = 0; i < partes.length; i++) {
            paginas[i] = Integer.parseInt(partes[i].trim());
        }

        return paginas;
    }

    private static ResultadoSimulacao executarSimulacao(int[] paginas, int capacidade) {
        int faltasFIFO = fifo(paginas, capacidade);
        int faltasLRU = lru(paginas, capacidade);
        int faltasRelogio = relogio(paginas, capacidade);
        int faltasOtimo = otimo(paginas, capacidade);

        return new ResultadoSimulacao(faltasFIFO, faltasLRU, faltasRelogio, faltasOtimo);
    }

    private static String montarTextoResultado(int[] paginas, int capacidade, ResultadoSimulacao resultado) {
        StringBuilder texto = new StringBuilder();

        texto.append("Sequencia de paginas: ");
        texto.append(Arrays.toString(paginas));
        texto.append("\n");

        texto.append("Quantidade de quadros na memoria: ");
        texto.append(capacidade);
        texto.append("\n\n");

        texto.append("Resultado das faltas de pagina:\n");
        texto.append("- Metodo 1 - FIFO: ");
        texto.append(resultado.faltasFIFO);
        texto.append(" faltas de pagina\n");

        texto.append("- Metodo 2 - LRU: ");
        texto.append(resultado.faltasLRU);
        texto.append(" faltas de pagina\n");

        texto.append("- Metodo 3 - Relogio: ");
        texto.append(resultado.faltasRelogio);
        texto.append(" faltas de pagina\n");

        texto.append("- Metodo 4 - Otimo: ");
        texto.append(resultado.faltasOtimo);
        texto.append(" faltas de pagina\n");

        return texto.toString();
    }

    public static int fifo(int[] paginas, int capacidade) {
        Queue<Integer> fila = new LinkedList<>();
        Set<Integer> memoria = new HashSet<>();
        int faltas = 0;

        for (int pagina : paginas) {
            if (!memoria.contains(pagina)) {
                faltas++;

                if (memoria.size() == capacidade) {
                    int removida = fila.poll();
                    memoria.remove(removida);
                }

                memoria.add(pagina);
                fila.add(pagina);
            }
        }

        return faltas;
    }

    public static int lru(int[] paginas, int capacidade) {
        List<Integer> memoria = new ArrayList<>();
        int faltas = 0;

        for (int pagina : paginas) {
            if (!memoria.contains(pagina)) {
                faltas++;

                if (memoria.size() == capacidade) {
                    memoria.remove(0);
                }

                memoria.add(pagina);
            } else {
                memoria.remove(Integer.valueOf(pagina));
                memoria.add(pagina);
            }
        }

        return faltas;
    }

    public static int relogio(int[] paginas, int capacidade) {
        int[] memoria = new int[capacidade];
        boolean[] bitUso = new boolean[capacidade];

        Arrays.fill(memoria, -1);

        int ponteiro = 0;
        int faltas = 0;

        for (int pagina : paginas) {
            boolean encontrada = false;

            for (int i = 0; i < capacidade; i++) {
                if (memoria[i] == pagina) {
                    encontrada = true;
                    bitUso[i] = true;
                    break;
                }
            }

            if (!encontrada) {
                faltas++;

                while (true) {
                    if (memoria[ponteiro] == -1) {
                        memoria[ponteiro] = pagina;
                        bitUso[ponteiro] = true;
                        ponteiro = (ponteiro + 1) % capacidade;
                        break;
                    }

                    if (!bitUso[ponteiro]) {
                        memoria[ponteiro] = pagina;
                        bitUso[ponteiro] = true;
                        ponteiro = (ponteiro + 1) % capacidade;
                        break;
                    } else {
                        bitUso[ponteiro] = false;
                        ponteiro = (ponteiro + 1) % capacidade;
                    }
                }
            }
        }

        return faltas;
    }

    public static int otimo(int[] paginas, int capacidade) {
        List<Integer> memoria = new ArrayList<>();
        int faltas = 0;

        for (int i = 0; i < paginas.length; i++) {
            int paginaAtual = paginas[i];

            if (!memoria.contains(paginaAtual)) {
                faltas++;

                if (memoria.size() < capacidade) {
                    memoria.add(paginaAtual);
                } else {
                    int indiceParaRemover = encontrarPaginaMaisDistante(paginas, memoria, i);
                    memoria.remove(indiceParaRemover);
                    memoria.add(paginaAtual);
                }
            }
        }

        return faltas;
    }

    private static int encontrarPaginaMaisDistante(int[] paginas, List<Integer> memoria, int posicaoAtual) {
        int indiceMaisDistante = -1;
        int maiorDistancia = -1;

        for (int i = 0; i < memoria.size(); i++) {
            int paginaNaMemoria = memoria.get(i);
            int proximoUso = Integer.MAX_VALUE;

            for (int j = posicaoAtual + 1; j < paginas.length; j++) {
                if (paginas[j] == paginaNaMemoria) {
                    proximoUso = j;
                    break;
                }
            }

            if (proximoUso > maiorDistancia) {
                maiorDistancia = proximoUso;
                indiceMaisDistante = i;
            }
        }

        return indiceMaisDistante;
    }

    private static void executarNoTerminal() {
        int[] paginas = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2};
        int capacidadeMemoria = 3;

        ResultadoSimulacao resultado = executarSimulacao(paginas, capacidadeMemoria);

        System.out.println(montarTextoResultado(paginas, capacidadeMemoria, resultado));
    }

    public static void main(String[] args) {
        executarNoTerminal();

        SwingUtilities.invokeLater(() -> {
            SimuladorSubstituicaoPaginas janela = new SimuladorSubstituicaoPaginas();
            janela.setVisible(true);
        });
    }
}

class ResultadoSimulacao {
    int faltasFIFO;
    int faltasLRU;
    int faltasRelogio;
    int faltasOtimo;

    ResultadoSimulacao(int faltasFIFO, int faltasLRU, int faltasRelogio, int faltasOtimo) {
        this.faltasFIFO = faltasFIFO;
        this.faltasLRU = faltasLRU;
        this.faltasRelogio = faltasRelogio;
        this.faltasOtimo = faltasOtimo;
    }
}

class PainelGrafico extends JPanel {

    private String[] nomes = {};
    private int[] valores = {};

    public PainelGrafico() {
        setBackground(Color.WHITE);
    }

    public void atualizarDados(String[] nomes, int[] valores) {
        this.nomes = nomes;
        this.valores = valores;
        repaint();
    }

    public void limpar() {
        this.nomes = new String[]{};
        this.valores = new int[]{};
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D grafico = (Graphics2D) g;
        grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int largura = getWidth();
        int altura = getHeight();

        grafico.setColor(Color.BLACK);
        grafico.setFont(new Font("Arial", Font.BOLD, 18));
        grafico.drawString("Grafico Comparativo de Faltas de Pagina", 230, 30);

        if (valores.length == 0) {
            grafico.setFont(new Font("Arial", Font.PLAIN, 14));
            grafico.drawString("Execute a simulacao para visualizar o grafico.", 270, altura / 2);
            return;
        }

        int margemEsquerda = 80;
        int margemInferior = 60;
        int margemSuperior = 60;
        int larguraBarra = 90;
        int espacoEntreBarras = 80;
        int alturaGrafico = altura - margemSuperior - margemInferior;

        int maiorValor = 0;

        for (int valor : valores) {
            if (valor > maiorValor) {
                maiorValor = valor;
            }
        }

        if (maiorValor == 0) {
            maiorValor = 1;
        }

        grafico.setColor(Color.BLACK);
        grafico.drawLine(margemEsquerda, margemSuperior, margemEsquerda, altura - margemInferior);
        grafico.drawLine(margemEsquerda, altura - margemInferior, largura - 40, altura - margemInferior);

        for (int i = 0; i < valores.length; i++) {
            int alturaBarra = (int) ((double) valores[i] / maiorValor * (alturaGrafico - 20));

            int x = margemEsquerda + 60 + i * (larguraBarra + espacoEntreBarras);
            int y = altura - margemInferior - alturaBarra;

            Color corBarra;

            if (i == 0) {
                corBarra = new Color(66, 135, 245);
            } else if (i == 1) {
                corBarra = new Color(87, 190, 120);
            } else if (i == 2) {
                corBarra = new Color(245, 166, 35);
            } else {
                corBarra = new Color(180, 90, 220);
            }

            grafico.setColor(corBarra);
            grafico.fillRect(x, y, larguraBarra, alturaBarra);

            grafico.setColor(Color.BLACK);
            grafico.drawRect(x, y, larguraBarra, alturaBarra);

            grafico.setFont(new Font("Arial", Font.BOLD, 14));
            grafico.drawString(String.valueOf(valores[i]), x + 35, y - 8);

            grafico.setFont(new Font("Arial", Font.PLAIN, 14));
            grafico.drawString(nomes[i], x + 18, altura - 35);
        }

        grafico.setFont(new Font("Arial", Font.PLAIN, 13));
        grafico.drawString("Algoritmos", largura / 2 - 30, altura - 10);

        grafico.rotate(-Math.PI / 2);
        grafico.drawString("Faltas de pagina", -altura / 2 - 45, 25);
        grafico.rotate(Math.PI / 2);
    }
}
