package hasta.gui;

public class Hasta implements Comparable<Hasta> {
    private final int hastaNo;
    private final String ad;
    private final int yas;
    private final String cinsiyet;
    private final boolean mahkum;
    private final int engel;
    private final String kanama;
    private final double kayitSaati;
    private final int oncelikPuani;
    private final int muayeneSuresi;
    private double muayeneBaslangic = -1;
    private double muayeneBitis = -1;

    public Hasta(int hastaNo, String ad, int yas, String cinsiyet,
                boolean mahkum, int engel, String kanama, double kayitSaati) {
        this.hastaNo = hastaNo;
        this.ad = ad;
        this.yas = yas;
        this.cinsiyet = cinsiyet;
        this.mahkum = mahkum;
        this.engel = engel;
        this.kanama = kanama;
        this.kayitSaati = kayitSaati;
        this.oncelikPuani = hesaplaOncelikPuani();
        this.muayeneSuresi = hesaplaMuayeneSuresi();
    }

    private int hesaplaOncelikPuani() {
        int yasPuani = (yas < 5) ? 20 : (yas < 45 ? 0 : (yas < 65 ? 15 : 25));
        int engelPuani = engel / 4;
        int mahkumPuani = mahkum ? 50 : 0;
        int kanamaPuani = kanama.equalsIgnoreCase("agirKanama") ? 50 :
                         kanama.equalsIgnoreCase("kanama") ? 20 : 0;
        return yasPuani + engelPuani + mahkumPuani + kanamaPuani;
    }

    private int hesaplaMuayeneSuresi() {
        int sure = 10;
        sure += (yas < 65) ? 15 : 0;
        sure += engel / 5;
        sure += kanama.equalsIgnoreCase("agirKanama") ? 20 :
               kanama.equalsIgnoreCase("kanama") ? 10 : 0;
        return sure;
    }

    public void setMuayeneZamani(double baslangic) {
        this.muayeneBaslangic = baslangic;
        this.muayeneBitis = baslangic + muayeneSuresi;
    }

    @Override
    public int compareTo(Hasta other) {
        // Öncelik puanı yüksek olan önce gelir
        int oncelikKarsilastirma = Integer.compare(this.oncelikPuani, other.oncelikPuani);
        if (oncelikKarsilastirma == 0) {
            // Aynı öncelikte ise kayıt saati erken olan önce gelir
            return Double.compare(other.kayitSaati, this.kayitSaati);
        }
        return oncelikKarsilastirma;
    }

    @Override
    public String toString() {
        return String.format("%d - %-15s | Öncelik: %3d | Kayıt: %s | Süre: %3d dk | Başlangıç: %s | Bitiş: %s",
                hastaNo, ad, oncelikPuani, 
                formatSaat(kayitSaati), 
                muayeneSuresi,
                muayeneBaslangic > 0 ? formatSaat(muayeneBaslangic) : "-",
                muayeneBitis > 0 ? formatSaat(muayeneBitis) : "-");
    }
    
    public String toString2() {
        return String.format("%d - %-15s | Öncelik: %3d | Kayıt: %s | Süre: %3d dk ",
                hastaNo, ad, oncelikPuani, 
                formatSaat(kayitSaati), 
                muayeneSuresi);
                
                
    }

    private String formatSaat(double dakika) {
        int saat = (int) (dakika / 60) % 24;
        int dk = (int) (dakika % 60);
        return String.format("%02d:%02d", saat, dk);
    }

    // Getter'lar
    public String getAd() { return ad; }
    public int getOncelikPuani() { return oncelikPuani; }
    public double getKayitSaati() { return kayitSaati; }
    public double getMuayeneBitis() { return muayeneBitis; }
    public boolean isMuayeneEdildi() { return muayeneBaslangic > 0; }
    public int getMuayeneSuresi() { return muayeneSuresi; }
}