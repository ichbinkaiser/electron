package ichbinkaiser.electron.entity;

import android.graphics.Point;

import java.util.Random;

public class Popup {
    Point position = new Point();
    int life = 255; // animation index life
    PopupType type; // popups message type
    int index = 0; // text message index

    Popup(Point position, PopupType type, int indexSize) {
        Random rnd = new Random();
        if (indexSize > 0) {
        }
        index = rnd.nextInt(indexSize);

        this.type = type;
        this.position.x = position.x;

        if (type == PopupType.SCORE_UP) {
            this.position.y = position.y + 255;
        } else {
            this.position.y = position.y - 255;
        }
    }

    public int getLife() {
        return life--;
    }

    public Point getPosition() {
        return position;
    }

    public PopupType getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }
}
