package hasta.gui;

import java.util.ArrayList;
import java.util.List;

public class Heap {
    private final List<Hasta> heapList;

    public Heap() {
        this.heapList = new ArrayList<>();
    }

    public void ekle(Hasta hasta) {
        heapList.add(hasta);
        heapifyUp(heapList.size() - 1);
    }

    public Hasta cikart() {
        if (heapList.isEmpty()) return null;

        Hasta enOncelikli = heapList.get(0);
        int sonIndex = heapList.size() - 1;
        heapList.set(0, heapList.get(sonIndex));
        heapList.remove(sonIndex);
        heapifyDown(0);
        return enOncelikli;
    }

    public boolean bosMu() {
        return heapList.isEmpty();
    }

    public List<Hasta> getHeapList() {
        return new ArrayList<>(heapList);
    }

    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (heapList.get(index).compareTo(heapList.get(parentIndex)) > 0) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    private void heapifyDown(int index) {
        int enBuyukIndex = index;
        int solCocukIndex = 2 * index + 1;
        int sagCocukIndex = 2 * index + 2;

        if (solCocukIndex < heapList.size() && 
            heapList.get(solCocukIndex).compareTo(heapList.get(enBuyukIndex)) > 0) {
            enBuyukIndex = solCocukIndex;
        }

        if (sagCocukIndex < heapList.size() && 
            heapList.get(sagCocukIndex).compareTo(heapList.get(enBuyukIndex)) > 0) {
            enBuyukIndex = sagCocukIndex;
        }

        if (enBuyukIndex != index) {
            swap(index, enBuyukIndex);
            heapifyDown(enBuyukIndex);
        }
    }

    private void swap(int i, int j) {
        Hasta temp = heapList.get(i);
        heapList.set(i, heapList.get(j));
        heapList.set(j, temp);
    }
}