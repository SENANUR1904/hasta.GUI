package hasta.gui;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class HastaGUI extends JFrame {
    private Heap heap = new Heap();
    private final JTextArea textArea = new JTextArea();
    private final JPanel treePanel = new TreePanel();
    private final JLabel saatLabel = new JLabel("Simülasyon Saati: 08:00");
    private int hastaSayaci = 1;
    private double simuleSaat = 480; // 08:00 = 480 dakika
    public Timer timer;
    private boolean muayeneDevamEdiyor = false;
    private Hasta aktifHasta;
    private double simuleHiz = 1.0;
    private List<Hasta> tumHastalar = new ArrayList<>();

    public HastaGUI() {
        setTitle("Hasta Takip Sistemi - Max Heap");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        JButton baslatBtn = new JButton("Başlat");
        JButton durdurBtn = new JButton("Durdur");
        JButton hizlandirBtn = new JButton("Hızlandır");
        JButton yavaslatBtn = new JButton("Yavaşlat");

        baslatBtn.addActionListener(e -> simulasyonBaslat());
        durdurBtn.addActionListener(e -> simulasyonDurdur());
        hizlandirBtn.addActionListener(e -> simuleHiz *= 2.0);
        yavaslatBtn.addActionListener(e -> simuleHiz /= 2.0);

        controlPanel.add(baslatBtn);
        controlPanel.add(durdurBtn);
        controlPanel.add(hizlandirBtn);
        controlPanel.add(yavaslatBtn);
        controlPanel.add(saatLabel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(treePanel));
        splitPane.setRightComponent(new JScrollPane(textArea));
        splitPane.setDividerLocation(800);

        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        dosyadanOtomatikOku();
    }

    private void dosyadanOtomatikOku() {
        String filePath = "C:\\Users\\SENANUR\\OneDrive\\Masaüstü\\Hasta.txt";
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            String satir;
            while ((satir = br.readLine()) != null) {
                satir = satir.trim();
                if (satir.isEmpty() || !satir.startsWith("*,"))
                    continue;

                String[] parcalar = satir.split("\\s*,\\s*");
                if (parcalar.length < 8) {
                    System.err.println("Eksik alan: " + satir + " (Atlanıyor)");
                    continue;
                }

                String ad = parcalar[1];
                int yas = Integer.parseInt(parcalar[2]);
                String cinsiyet = parcalar[3];
                boolean mahkum = parcalar[4].equalsIgnoreCase("true");
                int engel = Integer.parseInt(parcalar[5]);
                String kanama = parcalar[6];
                double kayitSaati = saatToDakika(parcalar[7]);

                Hasta hasta = new Hasta(hastaSayaci++, ad, yas, cinsiyet, mahkum, engel, kanama, kayitSaati);
                tumHastalar.add(hasta);
            }
            
            Collections.sort(tumHastalar, Comparator.comparingDouble(Hasta::getKayitSaati));
            textArea.append("Dosya okuma tamamlandı. Toplam hasta: " + tumHastalar.size() + "\n");
            
        } catch (Exception e) {
            textArea.append("Dosya okuma hatası: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private double saatToDakika(String saatStr) {
        String[] parcalar = saatStr.split("[:.]");
        int saat = Integer.parseInt(parcalar[0]);
        int dakika = parcalar.length > 1 ? Integer.parseInt(parcalar[1]) : 0;
        return saat * 60 + dakika;
    }

    private String dakikaToSaat(double dakika) {
        int saat = (int) (dakika / 60) % 24;
        int dk = (int) (dakika % 60);
        return String.format("%02d:%02d", saat, dk);
    }

    private void simulasyonBaslat() {
        if (timer != null) timer.stop();

        timer = new Timer(1000, e -> {
            simuleSaat += simuleHiz;
            saatLabel.setText("Simülasyon Saati: " + dakikaToSaat(simuleSaat));

            // Kayıt saati gelen hastaları heap'e ekle
            Iterator<Hasta> iterator = tumHastalar.iterator();
            while (iterator.hasNext()) {
                Hasta hasta = iterator.next();
                if (hasta.getKayitSaati() <= simuleSaat) {
                    heap.ekle(hasta);
                    iterator.remove();
                    textArea.append("Heap'e eklendi: " + hasta.getAd() + 
                                 " (Kayıt: " + dakikaToSaat(hasta.getKayitSaati()) + 
                                 ", Öncelik: " + hasta.getOncelikPuani() + ")\n");
                    guncelleGorunum();
                }
            }

            // Muayene başlatma (09:00'dan sonra)
            if (!muayeneDevamEdiyor && simuleSaat >= 540) {
                Hasta hasta = heap.cikart();
                if (hasta != null) {
                    hasta.setMuayeneZamani(simuleSaat);
                    aktifHasta = hasta;
                    muayeneDevamEdiyor = true;
                    textArea.append("MUAYENE BAŞLADI: " + hasta + "\n");
                    guncelleGorunum();
                }
            }

            // Muayene bitiş kontrolü
            if (muayeneDevamEdiyor && aktifHasta != null && 
                simuleSaat >= aktifHasta.getMuayeneBitis()) {
                textArea.append("MUAYENE BİTTİ: " + aktifHasta + "\n");
                muayeneDevamEdiyor = false;
                aktifHasta = null;
                guncelleGorunum();
            }
        });
        timer.start();
    }

    private void simulasyonDurdur() {
        if (timer != null) timer.stop();
    }

    private void guncelleGorunum() {
        StringBuilder sb = new StringBuilder();
        List<Hasta> heapList = heap.getHeapList();

        sb.append("BEKLEYEN HASTALAR (").append(heapList.size()).append("):\n");
        for (Hasta h : heapList) {
            sb.append(h.toString2()).append("\n");
        }

        if (aktifHasta != null) {
            sb.append("\nAKTİF MUAYENE:\n").append(aktifHasta.toString()).append("\n");
        }

        sb.append("\nKAYIT BEKLEYEN HASTALAR (").append(tumHastalar.size()).append("):\n");
        for (Hasta h : tumHastalar) {
            sb.append(h.getAd()).append(" (Kayıt: ").append(dakikaToSaat(h.getKayitSaati()))
              .append(", Öncelik: ").append(h.getOncelikPuani()).append(")\n");
        }

        textArea.setText(sb.toString());
        treePanel.repaint();
    }

    class TreePanel extends JPanel {
        private static final int NODE_DIAMETER = 60;
        private static final int Y_OFFSET = 100;

        public TreePanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(800, 600));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            List<Hasta> heapList = heap.getHeapList();
            if (!heapList.isEmpty()) {
                drawTree(g2d, heapList, 0, getWidth() / 2, 50, getWidth() / 4);
            }
        }

        private void drawTree(Graphics2D g2d, List<Hasta> heapList,
                            int index, int x, int y, int xOffset) {
            if (index >= heapList.size()) return;

            drawNode(g2d, heapList.get(index), x, y);

            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;

            if (leftChild < heapList.size()) {
                g2d.drawLine(x + NODE_DIAMETER / 2, y + NODE_DIAMETER/2,
                        x - xOffset + NODE_DIAMETER / 2, y + Y_OFFSET + NODE_DIAMETER/2);
                drawTree(g2d, heapList, leftChild, x - xOffset, y + Y_OFFSET, xOffset / 2);
            }

            if (rightChild < heapList.size()) {
                g2d.drawLine(x + NODE_DIAMETER / 2, y + NODE_DIAMETER/2,
                        x + xOffset + NODE_DIAMETER / 2, y + Y_OFFSET + NODE_DIAMETER/2);
                drawTree(g2d, heapList, rightChild, x + xOffset, y + Y_OFFSET, xOffset / 2);
            }
        }

        private void drawNode(Graphics2D g2d, Hasta hasta, int x, int y) {
            // Aktif hasta pembe, diğerleri açık mavi
            g2d.setColor((aktifHasta != null && hasta.equals(aktifHasta)) ? 
                        new Color(255, 182, 193) : new Color(255, 182, 193));
            g2d.fillOval(x, y, NODE_DIAMETER, NODE_DIAMETER);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x, y, NODE_DIAMETER, NODE_DIAMETER);

            String adText = hasta.getAd().length() > 6 ? 
                          hasta.getAd().substring(0, 6) + "..." : hasta.getAd();
            String oncelikText = String.valueOf(hasta.getOncelikPuani());

            FontMetrics fm = g2d.getFontMetrics();
            int adWidth = fm.stringWidth(adText);
            int oncelikWidth = fm.stringWidth(oncelikText);
            int textHeight = fm.getHeight();

            g2d.drawString(adText, x + (NODE_DIAMETER - adWidth) / 2, 
                          y + (NODE_DIAMETER / 2) - (textHeight / 2) + fm.getAscent()/2);
            g2d.drawString(oncelikText, x + (NODE_DIAMETER - oncelikWidth) / 2, 
                          y + (NODE_DIAMETER / 2) + (textHeight / 2) + fm.getAscent()/2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HastaGUI gui = new HastaGUI();
            gui.setVisible(true);
        });
    }
}