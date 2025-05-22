package org.traccar.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class LocationTree {
    private Item root;

    public static class Item {
        private Item left;
        private Item right;
        private float x;
        private float y;
        private String data;

        public Item(float x, float y) {
            this(x, y, null);
        }

        public Item(float x, float y, String data) {
            this.x = x;
            this.y = y;
            this.data = data;
        }

        public String getData() {
            return this.data;
        }

        private float squaredDistance(Item item) {
            return (this.x - item.x) * (this.x - item.x) + (this.y - item.y) * (this.y - item.y);
        }

        private float axisSquaredDistance(Item item, int axis) {
            if (axis == 0) {
                return (this.x - item.x) * (this.x - item.x);
            }
            return (this.y - item.y) * (this.y - item.y);
        }
    }


    private ArrayList<Comparator<Item>> comparators = new ArrayList<>();

    public LocationTree(List<Item> items) {
        this.comparators.add(new Comparator<Item>() {
            public int compare(LocationTree.Item o1, LocationTree.Item o2) {
                return Float.compare(o1.x, o2.x);
            }
        });
        this.comparators.add(new Comparator<Item>() {
            public int compare(LocationTree.Item o1, LocationTree.Item o2) {
                return Float.compare(o1.y, o2.y);
            }
        });
        this.root = createTree(items, 0);
    }

    private Item createTree(List<Item> items, int depth) {
        if (items.isEmpty()) {
            return null;
        }
        Collections.sort(items, this.comparators.get(depth % 2));
        int currentIndex = items.size() / 2;
        Item median = items.get(currentIndex);
        median.left = createTree(new ArrayList<>(items.subList(0, currentIndex)), depth + 1);
        median.right = createTree(new ArrayList<>(items.subList(currentIndex + 1, items.size())), depth + 1);
        return median;
    }

    public Item findNearest(Item search) {
        return findNearest(this.root, search, 0);
    }

    private Item findNearest(Item current, Item search, int depth) {
        Item next, other;
        int direction = ((Comparator<Item>) this.comparators.get(depth % 2)).compare(search, current);


        if (direction < 0) {
            next = current.left;
            other = current.right;
        } else {
            next = current.right;
            other = current.left;
        }

        Item best = current;
        if (next != null) {
            best = findNearest(next, search, depth + 1);
        }

        if (current.squaredDistance(search) < best.squaredDistance(search)) {
            best = current;
        }
        if (other != null && current.axisSquaredDistance(search, depth % 2) < best.squaredDistance(search)) {
            Item possibleBest = findNearest(other, search, depth + 1);
            if (possibleBest.squaredDistance(search) < best.squaredDistance(search)) {
                best = possibleBest;
            }
        }

        return best;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\LocationTree.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */